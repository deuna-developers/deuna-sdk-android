#!/bin/bash
set -e

echo "🚀 Starting DEUNA Android SDK Integration Tests (Explore Module)"
echo "================================================"

# Configuration
AVD_NAME="test_avd"
EMULATOR_WAIT_TIME="${EMULATOR_WAIT_TIME:-300}"  # 5 minutes max wait
ADB_PORT=5037
EMULATOR_PORT=5554

# Function to check if emulator is ready
wait_for_emulator() {
    echo "⏳ Waiting for emulator to be ready..."
    local timeout=$EMULATOR_WAIT_TIME
    local elapsed=0

    while [ $elapsed -lt $timeout ]; do
        # Check if device is online
        if adb devices | grep -q "emulator-$EMULATOR_PORT.*device"; then
            # Check if boot is completed
            local boot_completed=$(adb -s emulator-$EMULATOR_PORT shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')
            if [ "$boot_completed" = "1" ]; then
                echo "✅ Emulator is ready!"
                return 0
            fi
        fi

        echo "   Still waiting... ($elapsed/$timeout seconds)"
        sleep 5
        elapsed=$((elapsed + 5))
    done

    echo "❌ Timeout waiting for emulator to be ready"
    return 1
}

# Function to cleanup
cleanup() {
    echo "🧹 Cleaning up..."

    # Stop logcat capture and copy it onto the mounted /test-results volume,
    # regardless of how we got here.
    if [ -n "$LOGCAT_PID" ]; then
        kill "$LOGCAT_PID" 2>/dev/null || true
        wait "$LOGCAT_PID" 2>/dev/null || true
        mkdir -p /test-results || true
        cp "$LOGCAT_RAW" /test-results/logcat-full.txt 2>/dev/null || true
        echo "   📝 Full logcat saved to /test-results/logcat-full.txt"
    fi

    # Stop any in-progress screenrecord (e.g. if killed mid-test by CI timeout)
    adb -s emulator-$EMULATOR_PORT shell pkill -INT screenrecord >/dev/null 2>&1 || true
    sleep 2
    adb -s emulator-$EMULATOR_PORT shell pkill -9 screenrecord >/dev/null 2>&1 || true
    if [ -n "${CURRENT_RECORDING_REMOTE:-}" ]; then
        sleep 1
        mkdir -p "${VIDEO_DIR:-/test-results/videos}" || true
        adb -s emulator-$EMULATOR_PORT pull "${CURRENT_RECORDING_REMOTE}" \
            "${VIDEO_DIR:-/test-results/videos}/interrupted.mp4" >/dev/null 2>&1 || true
        echo "   📹 Interrupted recording saved"
    fi

    if [ -n "$EMULATOR_PID" ]; then
        echo "   Killing emulator (PID: $EMULATOR_PID)"
        kill $EMULATOR_PID 2>/dev/null || true
    fi
    if [ -n "$EMULATOR_PORT" ]; then
        adb -s emulator-$EMULATOR_PORT reverse --remove-all 2>/dev/null || true
    fi
    pkill socat 2>/dev/null || true
    adb kill-server 2>/dev/null || true
    if [ -f /tmp/socat-8888.log ]; then
        echo "📋 Socat 8888 (apigw) logs:"
        cat /tmp/socat-8888.log || true
    fi
    if [ -f /tmp/socat-5003.log ]; then
        echo "📋 Socat 5003 (checkout-base) logs:"
        cat /tmp/socat-5003.log || true
    fi
    if [ -f /tmp/socat-5001.log ]; then
        echo "📋 Socat 5001 (elements-link) logs:"
        cat /tmp/socat-5001.log || true
    fi
    if [ -f /tmp/socat-8080.log ]; then
        echo "📋 Socat 8080 (deuna-squid-nginx proxy) logs:"
        cat /tmp/socat-8080.log || true
    fi
}

# Set trap to cleanup on exit
trap cleanup EXIT INT TERM

# Start ADB server
echo "🔧 Starting ADB server..."
adb start-server

# Start emulator in background
echo "📱 Starting Android emulator..."

# Check if KVM is available
if [ -e /dev/kvm ]; then
    echo "   ✅ KVM detected - using hardware acceleration"
    KVM_ACCEL=""
else
    echo "   ⚠️  KVM not available - using software acceleration (slower)"
    KVM_ACCEL="-accel off"
fi

# Disabling virtual GSM modem (hw.gsmModem=no) to avoid QEMU IPv6 loopback binding crash
# Creating/patching config.ini for AVD
AVD_CONFIG_PATH="$HOME/.android/avd/${AVD_NAME}.avd/config.ini"
if [ -f "$AVD_CONFIG_PATH" ]; then
    echo "   🔧 Disabling virtual GSM modem in AVD config.ini..."
    sed -i 's/hw.gsmModem=.*/hw.gsmModem=no/g' "$AVD_CONFIG_PATH" || echo "hw.gsmModem=no" >> "$AVD_CONFIG_PATH"
fi

# -http-proxy gives the emulator guest OS internet egress via deuna-squid-nginx.
# Container-level http_proxy/https_proxy vars don't affect the emulator's virtualized network.
emulator \
    -avd $AVD_NAME \
    -no-window \
    -gpu swiftshader_indirect \
    -noaudio \
    -no-boot-anim \
    -camera-back none \
    -no-snapshot-save \
    -memory 1536 \
    -partition-size 4096 \
    -port $EMULATOR_PORT \
    -http-proxy http://deuna-squid-nginx:8080 \
    $KVM_ACCEL \
    > /tmp/emulator.log 2>&1 &

EMULATOR_PID=$!
echo "   Emulator started with PID: $EMULATOR_PID"

# Wait for emulator to be ready
if ! wait_for_emulator; then
    echo "❌ Failed to start emulator"
    echo "📋 Emulator logs:"
    cat /tmp/emulator.log
    exit 1
fi

# Give System UI extra time to stabilize after boot_completed=1.
# swiftshader_indirect is slow; System UI can ANR in the first few seconds
# even after the boot property flips, blocking all test interactions.
echo "⏳ Waiting 15s for System UI to fully stabilize..."
sleep 15

# Dismiss any "System UI isn't responding" or app ANR dialogs.
# CLOSE_SYSTEM_DIALOGS is the canonical way to clear all blocking overlays.
echo "🔇 Dismissing system dialogs (ANR / crash overlays)..."
adb -s emulator-$EMULATOR_PORT shell am broadcast \
    -a android.intent.action.CLOSE_SYSTEM_DIALOGS || true
sleep 2
# Belt-and-suspenders: also send BACK key to clear any leftover modal
adb -s emulator-$EMULATOR_PORT shell input keyevent KEYCODE_BACK || true
sleep 1

# Start port forwarding for local services in the background
echo "🔀 Starting port forwarding to backend services..."
socat -d -d TCP-LISTEN:8888,fork TCP:apigw:8080 > /tmp/socat-8888.log 2>&1 &
socat -d -d TCP-LISTEN:5003,fork TCP:checkout-base:5003 > /tmp/socat-5003.log 2>&1 &
socat -d -d TCP-LISTEN:5001,fork TCP:elements-link:5001 > /tmp/socat-5001.log 2>&1 &
# Port 8080 bridges the emulator's -http-proxy target (10.0.2.2:8080) to deuna-squid-nginx
socat -d -d TCP-LISTEN:8080,fork TCP:deuna-squid-nginx:8080 > /tmp/socat-8080.log 2>&1 &

# Set environment variables inside the emulator via Android wrap properties
echo "⚙️ Configuring environment variables inside the emulator..."
adb -s emulator-$EMULATOR_PORT shell setprop wrap.com.deuna.explore "\"env DEUNA_API_ENDPOINT=http://localhost:8888 DEUNA_ENV=preprod DEUNA_CHECKOUT_BASE_DOMAIN=http://localhost:5003 DEUNA_ELEMENTS_LINK_DOMAIN=http://localhost:5001 ADMIN_USERNAME=${ADMIN_USERNAME:-developers@getduna.com} ADMIN_PASSWORD=${ADMIN_PASSWORD:-superadmin}\"" || true
adb -s emulator-$EMULATOR_PORT shell setprop wrap.com.deuna.explore.test "\"env DEUNA_API_ENDPOINT=http://localhost:8888 DEUNA_ENV=preprod DEUNA_CHECKOUT_BASE_DOMAIN=http://localhost:5003 DEUNA_ELEMENTS_LINK_DOMAIN=http://localhost:5001 ADMIN_USERNAME=${ADMIN_USERNAME:-developers@getduna.com} ADMIN_PASSWORD=${ADMIN_PASSWORD:-superadmin}\"" || true

# Set up ADB reverse port forwarding to bridge localhost ports of the emulator to the container host
echo "🔄 Setting up ADB reverse port forwarding..."
adb -s emulator-$EMULATOR_PORT reverse tcp:8888 tcp:8888 || true
adb -s emulator-$EMULATOR_PORT reverse tcp:5003 tcp:5003 || true
adb -s emulator-$EMULATOR_PORT reverse tcp:5001 tcp:5001 || true

# Disable animations
echo "🎨 Disabling animations..."
adb -s emulator-$EMULATOR_PORT shell settings put global window_animation_scale 0
adb -s emulator-$EMULATOR_PORT shell settings put global transition_animation_scale 0
adb -s emulator-$EMULATOR_PORT shell settings put global animator_duration_scale 0

# Grant permissions (explore app package is com.deuna.explore)
echo "🔓 Granting permissions..."
adb -s emulator-$EMULATOR_PORT shell pm grant com.deuna.explore android.permission.INTERNET || true
adb -s emulator-$EMULATOR_PORT shell pm grant com.deuna.explore android.permission.ACCESS_NETWORK_STATE || true

# Display environment info
echo ""
echo "📊 Environment Information:"
echo "   DEUNA_API_ENDPOINT: ${DEUNA_API_ENDPOINT:-not set}"
echo "   DEUNA_ENV: ${DEUNA_ENV:-not set}"
echo "   ADMIN_USERNAME: ${ADMIN_USERNAME:-not set}"
echo ""

# Run tests
echo "🧪 Running integration tests..."
echo "================================================"

cd /app

# Pre-build APKs before recording starts; the connectedAndroidTest call below reuses UP-TO-DATE outputs.
echo "🏗️ Pre-building app and test APKs (not recorded)..."
./gradlew :explore:assembleDebug :explore:assembleDebugAndroidTest \
    -PdeunaWidgetHardwareAccelerated=false \
    -PdeunaWidgetForceSoftwareRendering=true \
    --no-daemon --stacktrace > /tmp/prebuild.log 2>&1 || {
    echo "⚠️  Prebuild failed — continuing anyway, the real run below will retry it. Last 50 lines:"
    tail -50 /tmp/prebuild.log
}

echo "📝 Starting logcat capture..."
LOGCAT_RAW="/tmp/logcat-raw.txt"
adb -s emulator-$EMULATOR_PORT logcat -c || true
adb -s emulator-$EMULATOR_PORT logcat -v threadtime > "$LOGCAT_RAW" 2>&1 &
LOGCAT_PID=$!

VIDEO_DIR="/test-results/videos"
mkdir -p "$VIDEO_DIR"
CURRENT_RECORDING_REMOTE=""
GRADLE_RAW_LOG="/tmp/gradle-test-raw.log"
TEST_RUNNER="com.deuna.explore.test/androidx.test.runner.AndroidJUnitRunner"

adb -s emulator-$EMULATOR_PORT shell rm -f /sdcard/test-*.mp4 || true

safe_video_name() { echo "${1}" | sed 's/[^A-Za-z0-9_-]/_/g' | sed 's/__*/_/g'; }

start_recording() {
    local label="${1}"
    local remote="/sdcard/test-$(safe_video_name "${label}").mp4"
    adb -s emulator-$EMULATOR_PORT shell rm -f "${remote}" || true
    adb -s emulator-$EMULATOR_PORT shell "screenrecord --bit-rate 6000000 ${remote}" >/dev/null 2>&1 &
    CURRENT_RECORDING_REMOTE="${remote}"
    sleep 1
}

stop_recording_and_pull() {
    local label="${1}"
    local status="${2:-}"
    adb -s emulator-$EMULATOR_PORT shell pkill -INT screenrecord >/dev/null 2>&1 || true
    sleep 2
    adb -s emulator-$EMULATOR_PORT shell pkill -9 screenrecord >/dev/null 2>&1 || true
    if [ -n "${CURRENT_RECORDING_REMOTE}" ]; then
        sleep 1
        local suffix=""
        [ -n "${status}" ] && suffix="_${status}"
        local local_path="${VIDEO_DIR}/$(safe_video_name "${label}")${suffix}.mp4"
        adb -s emulator-$EMULATOR_PORT pull "${CURRENT_RECORDING_REMOTE}" "${local_path}" >/dev/null 2>&1 || true
        adb -s emulator-$EMULATOR_PORT shell rm -f "${CURRENT_RECORDING_REMOTE}" || true
        echo "   📹 Saved: ${local_path}"
        CURRENT_RECORDING_REMOTE=""
    fi
}

# Discover tests from source files, optionally filtered by annotation
echo "🔍 Discovering tests from source files..."
mapfile -t ALL_TESTS < <(
    find examples/explore/src/androidTest -name "*.kt" \
    | xargs grep -l "@Test" \
    | sort \
    | while IFS= read -r file; do
        # If annotation filter set, skip files whose class lacks that annotation
        if [ -n "${ANDROID_TEST_ANNOTATION:-}" ]; then
            short_annotation="${ANDROID_TEST_ANNOTATION##*.}"
            grep -q "@${short_annotation}" "${file}" || continue
        fi
        pkg=$(grep -m1 "^package " "${file}" | awk '{print $2}' | tr -d '\r')
        class=$(grep -m1 "^class " "${file}" | awk '{print $2}' | sed 's/[:(].*//')
        grep -E "^\s+fun (test[A-Za-z0-9_]+)\s*\(" "${file}" \
            | sed 's/.*fun \([A-Za-z0-9_]*\).*/\1/' \
            | while IFS= read -r method; do
                echo "${pkg}.${class}#${method}"
              done
    done
)

echo "Discovered ${#ALL_TESTS[@]} tests:"
printf '  %s\n' "${ALL_TESTS[@]}"

GRADLE_COMMON_ARGS=(
    -PdeunaWidgetHardwareAccelerated=false
    -PdeunaWidgetForceSoftwareRendering=true
    -Pandroid.testInstrumentationRunnerArguments.DEUNA_API_ENDPOINT=http://localhost:8888
    -Pandroid.testInstrumentationRunnerArguments.DEUNA_ENV=preprod
    -Pandroid.testInstrumentationRunnerArguments.DEUNA_CHECKOUT_BASE_DOMAIN=http://localhost:5003
    -Pandroid.testInstrumentationRunnerArguments.DEUNA_ELEMENTS_LINK_DOMAIN=http://localhost:5001
    --no-daemon --stacktrace --max-workers=1
)

if [ -n "${ANDROID_TEST_ANNOTATION:-}" ]; then
    echo "🏷️  Running only tests annotated with: $ANDROID_TEST_ANNOTATION"
    GRADLE_COMMON_ARGS+=("-Pandroid.testInstrumentationRunnerArguments.annotation=$ANDROID_TEST_ANNOTATION")
fi

run_single_test() {
    local full_id="${1}"
    local attempt="${2:-1}"
    local method="${full_id##*#}"
    local label="${method}"
    [ "${attempt}" -gt 1 ] && label="${method}_attempt${attempt}"

    echo ""
    echo "▶ [attempt ${attempt}] ${full_id}"

    start_recording "${label}"

    set +e
    ./gradlew :explore:connectedAndroidTest \
        "${GRADLE_COMMON_ARGS[@]}" \
        -Pandroid.testInstrumentationRunnerArguments.class="${full_id}" \
        2>&1 | tee -a "$GRADLE_RAW_LOG"
    local result=${PIPESTATUS[0]}
    set -e

    if [ "${result}" -eq 0 ]; then
        local pass_status="passed"
        [ "${attempt}" -gt 1 ] && pass_status="retry_passed"
        stop_recording_and_pull "${label}" "${pass_status}"
        return 0
    else
        local fail_status="failed"
        [ "${attempt}" -gt 1 ] && fail_status="retry_failed"
        stop_recording_and_pull "${label}" "${fail_status}"
        return 1
    fi
}

FAILED_TESTS=()
TEST_EXIT_CODE=0

for test_id in "${ALL_TESTS[@]}"; do
    if run_single_test "${test_id}" 1; then
        echo "✅ PASSED: ${test_id}"
    else
        echo "❌ FAILED: ${test_id}"
        TEST_EXIT_CODE=1
        FAILED_TESTS+=("${test_id}")
    fi
done

if [ "${#FAILED_TESTS[@]}" -gt 0 ]; then
    STILL_FAILING=()
    for test_id in "${FAILED_TESTS[@]}"; do
        passed=false
        for attempt in 2 3; do
            echo "Retrying (attempt ${attempt}/3): ${test_id}"
            if run_single_test "${test_id}" "${attempt}"; then
                passed=true
                break
            fi
        done
        [ "${passed}" = "false" ] && STILL_FAILING+=("${test_id}")
    done
    if [ "${#STILL_FAILING[@]}" -gt 0 ]; then
        TEST_EXIT_CODE=1
    else
        TEST_EXIT_CODE=0
    fi
fi

# Copy test results
echo ""
echo "📄 Copying test results..."
if [ -d "examples/explore/build/reports/androidTests" ]; then
    mkdir -p /test-results || true
    cp -r examples/explore/build/reports/androidTests/* /test-results/ 2>/dev/null || true
    echo "   Test results copied to /test-results/"
else
    echo "   ⚠️  No test results found"
fi

# Copy test artifacts
if [ -d "examples/explore/build/outputs/androidTest-results" ]; then
    mkdir -p /test-results/artifacts || true
    cp -r examples/explore/build/outputs/androidTest-results/* /test-results/artifacts/ 2>/dev/null || true
    echo "   Test artifacts copied to /test-results/artifacts/"
fi

# Print summary
echo ""
echo "================================================"
if [ "$TEST_EXIT_CODE" -eq 0 ]; then
    echo "✅ All tests passed!"
else
    echo "❌ Tests failed with exit code: $TEST_EXIT_CODE"
    echo ""
    echo "🔎 Failed tests:"
    grep -E "^[A-Za-z0-9_.]+ > .* FAILED" "$GRADLE_RAW_LOG" | sort -u || echo "   (no per-test FAILED lines found — check BUILD FAILED reason below)"
    echo ""
    echo "🔎 Build failure reason:"
    grep -A 5 "^\* What went wrong:" "$GRADLE_RAW_LOG" || true
    echo ""
    echo "🔎 App crashes/exceptions during the run (com.deuna.explore only):"
    grep -A 25 "FATAL EXCEPTION" "$LOGCAT_RAW" || echo "   (no FATAL EXCEPTION found in logcat)"
    echo ""
    echo "📋 Full uncut Gradle log: /test-results/gradle-raw.log"
    echo "📋 Full logcat: /test-results/logcat-full.txt"
fi
echo "================================================"

mkdir -p /test-results || true
cp "$GRADLE_RAW_LOG" /test-results/gradle-raw.log 2>/dev/null || true

exit $TEST_EXIT_CODE

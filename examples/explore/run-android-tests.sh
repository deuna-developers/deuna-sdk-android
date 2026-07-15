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

    # Stop the test-boundary monitor first so it can't kill screenrecord again
    # while we're finalizing the last segment below.
    if [ -n "$TEST_BOUNDARY_MONITOR_PID" ]; then
        kill "$TEST_BOUNDARY_MONITOR_PID" 2>/dev/null || true
        wait "$TEST_BOUNDARY_MONITOR_PID" 2>/dev/null || true
    fi

    # Stop the screenrecord loop (if started) and pull every recorded segment,
    # one video per test (named after the test that was running when it was
    # recorded — see the boundary monitor below), onto the mounted
    # /test-results volume, regardless of how we got here (pass, fail, or
    # killed by the outer CI timeout).
    if [ -n "$SCREENRECORD_LOOP_PID" ]; then
        touch "$SCREENRECORD_STOP_FILE" 2>/dev/null || true
        adb -s emulator-$EMULATOR_PORT shell pkill -INT screenrecord >/dev/null 2>&1 || true
        wait "$SCREENRECORD_LOOP_PID" 2>/dev/null || true
        mkdir -p "$VIDEO_DIR" || true
        for remote_path in $(adb -s emulator-$EMULATOR_PORT shell ls /sdcard/*.mp4 2>/dev/null | tr -d '\r'); do
            file_name=$(basename "$remote_path")
            adb -s emulator-$EMULATOR_PORT pull "${remote_path}" "${VIDEO_DIR}/${file_name}" >/dev/null 2>&1 || true
            adb -s emulator-$EMULATOR_PORT shell rm -f "${remote_path}" || true
        done
        echo "   📹 Test videos saved to ${VIDEO_DIR}"
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
./gradlew :explore:assembleDebug :explore:assembleDebugAndroidTest --no-daemon --stacktrace > /tmp/prebuild.log 2>&1 || {
    echo "⚠️  Prebuild failed — continuing anyway, the real run below will retry it. Last 50 lines:"
    tail -50 /tmp/prebuild.log
}

echo "📝 Starting logcat capture..."
LOGCAT_RAW="/tmp/logcat-raw.txt"
adb -s emulator-$EMULATOR_PORT logcat -c || true
adb -s emulator-$EMULATOR_PORT logcat -v threadtime > "$LOGCAT_RAW" 2>&1 &
LOGCAT_PID=$!

# Record the emulator screen, one video per test instead of arbitrary rolling
# segments — much easier to review than a handful of videos that each splice
# together whatever tests happened to run in the same ~170s window.
echo "🎥 Starting screen recording..."
VIDEO_DIR="/test-results/videos"
SCREENRECORD_STOP_FILE="/tmp/stop_screenrecord_segments"
CURRENT_TEST_NAME_FILE="/tmp/current_test_name"
rm -f "$SCREENRECORD_STOP_FILE"
echo "00-before-first-test" > "$CURRENT_TEST_NAME_FILE"
adb -s emulator-$EMULATOR_PORT shell rm -f /sdcard/*.mp4 || true

(
    while true; do
        test_name=$(cat "$CURRENT_TEST_NAME_FILE" 2>/dev/null || echo "unknown")
        safe_name=$(echo "$test_name" | tr -c 'A-Za-z0-9._-' '_')
        remote_path="/sdcard/${safe_name}.mp4"
        adb -s emulator-$EMULATOR_PORT shell rm -f "${remote_path}" || true
        adb -s emulator-$EMULATOR_PORT shell "screenrecord --time-limit 170 --bit-rate 6000000 ${remote_path}" >/dev/null 2>&1 || true
        if [[ -f "$SCREENRECORD_STOP_FILE" ]]; then
            break
        fi
    done
) &
SCREENRECORD_LOOP_PID=$!

# Watch logcat for test start markers; cut a new recording segment per test (SIGINT finalizes valid mp4).
(
    tail -n0 -F "$LOGCAT_RAW" 2>/dev/null | while IFS= read -r line; do
        if [[ "$line" =~ TestRunner:\ started:\ ([A-Za-z0-9_]+)\(([A-Za-z0-9_.]+)\) ]]; then
            method="${BASH_REMATCH[1]}"
            class="${BASH_REMATCH[2]##*.}"
            echo "${class}-${method}" > "$CURRENT_TEST_NAME_FILE"
            adb -s emulator-$EMULATOR_PORT shell pkill -INT screenrecord >/dev/null 2>&1 || true
        fi
    done
) &
TEST_BOUNDARY_MONITOR_PID=$!

GRADLE_NOISE_FILTER='^(Transforming |Caching disabled for |  Build cache is disabled$|  Caching not enabled\.$|  Caching has been disabled for the task$|  Simple merging task$|  No history is available\.$|Task .* is not up-to-date because:$|Resolve mutations for |:[A-Za-z].*\(Thread\[.*\]\) (started|completed)\.?$|Skipping task .* as it has no source files|The input changes require a full rebuild for incremental task|INFO: .*D8: Malformed inner-class attribute:$|	outerTypeInternal:|	innerTypeInternal:|	innerName:|Resource missing\. \[HTTP GET:|Downloading https://.*\.(pom|module)( |$))'
GRADLE_RAW_LOG="/tmp/gradle-test-raw.log"

ANNOTATION_ARG=""
if [ -n "${ANDROID_TEST_ANNOTATION:-}" ]; then
    echo "🏷️  Running only tests annotated with: $ANDROID_TEST_ANNOTATION"
    ANNOTATION_ARG="-Pandroid.testInstrumentationRunnerArguments.annotation=$ANDROID_TEST_ANNOTATION"
fi

set +e
./gradlew :explore:connectedAndroidTest \
    -Pandroid.testInstrumentationRunnerArguments.DEUNA_API_ENDPOINT=http://localhost:8888 \
    -Pandroid.testInstrumentationRunnerArguments.DEUNA_ENV=preprod \
    -Pandroid.testInstrumentationRunnerArguments.DEUNA_CHECKOUT_BASE_DOMAIN=http://localhost:5003 \
    -Pandroid.testInstrumentationRunnerArguments.DEUNA_ELEMENTS_LINK_DOMAIN=http://localhost:5001 \
    $ANNOTATION_ARG \
    --no-daemon \
    --stacktrace \
    --max-workers=1 2>&1 | tee "$GRADLE_RAW_LOG" | grep -vE "$GRADLE_NOISE_FILTER"

TEST_EXIT_CODE=${PIPESTATUS[0]}
set -e

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

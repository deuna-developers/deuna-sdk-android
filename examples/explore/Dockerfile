# Dockerfile for DEUNA Android SDK Integration Tests (Explore Module)
# Optimized for Ubuntu/Linux with KVM acceleration

# Use budtmo/docker-android - popular image with Android SDK pre-installed
FROM budtmo/docker-android:emulator_13.0

# Switch to root for installations
USER root

# Install JDK 17 and other required tools (including socat for port forwarding)
RUN apt-get update && apt-get install -y \
    openjdk-17-jdk \
    curl \
    wget \
    unzip \
    socat \
    && rm -rf /var/lib/apt/lists/*

# Set JAVA_HOME for JDK 17
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV PATH=$JAVA_HOME/bin:$PATH

# Fixed GRADLE_USER_HOME so the dependency cache lands in the same place
# whether gradlew runs as root (image build) or androidusr (entrypoint, runtime).
# Without this, the build-time cache and the runtime user's default
# (~/.gradle) point at different paths and every run re-downloads everything.
ENV GRADLE_USER_HOME=/home/androidusr/.gradle
RUN mkdir -p "$GRADLE_USER_HOME" && chown -R androidusr:androidusr "$GRADLE_USER_HOME"

# Verify installations and show Android SDK info
RUN java -version
RUN echo "ANDROID_HOME: $ANDROID_HOME"
RUN echo "ANDROID_SDK_ROOT: $ANDROID_SDK_ROOT"

# Install required Android SDK components (x86_64 for KVM acceleration)
RUN yes | sdkmanager --licenses
RUN sdkmanager "system-images;android-31;google_apis;x86_64"
RUN sdkmanager "platforms;android-31"
RUN sdkmanager "platforms;android-32"
RUN sdkmanager "platforms;android-34"
RUN sdkmanager "build-tools;31.0.0"
RUN sdkmanager "build-tools;34.0.0"

# Switch to androidusr to create AVD in the correct user context
USER androidusr

# Create AVD for testing (API 31 x86_64 - optimized for KVM on Ubuntu)
# Use a modern, larger phone profile (pixel_6, 1080x2400) instead of
# avdmanager's tiny default skin. On the default profile the on-screen
# keyboard covers roughly half the screen, hiding fields further down a
# payment form (e.g. identity document) no matter how much you scroll —
# scrolling can't reveal content the keyboard is physically covering.
RUN echo "no" | avdmanager create avd \
    --force \
    --name test_avd \
    --package "system-images;android-31;google_apis;x86_64" \
    --device "pixel_6" \
    --abi google_apis/x86_64

# Verify AVD was created
RUN avdmanager list avd

# Switch back to root for project setup
USER root

# Set working directory
WORKDIR /app

# Copy project files
COPY . /app

# Enable explore module (same as GitHub Actions)
RUN sed -i 's|//include( "explore")|include("explore")|' settings.gradle.kts && \
    sed -i 's|//project(":explore").projectDir|project(":explore").projectDir|' settings.gradle.kts

# Make gradlew executable
RUN chmod +x ./gradlew

# Download Gradle dependencies (with better error handling)
RUN ./gradlew --no-daemon --stacktrace dependencies || echo "Dependencies download completed with warnings"

# gradlew above ran as root and wrote into GRADLE_USER_HOME as root; hand it back
# to androidusr so the runtime entrypoint (USER androidusr) can read/write the cache.
RUN chown -R androidusr:androidusr "$GRADLE_USER_HOME"

# Copy and make test execution script executable
COPY examples/explore/run-android-tests.sh /app/run-android-tests.sh
RUN chmod +x /app/run-android-tests.sh

# Create test results directory and set permissions
RUN mkdir -p /test-results && chown -R androidusr:androidusr /test-results

# Set ownership of /app to androidusr so Gradle can write its build cache (.gradle/)
RUN chown -R androidusr:androidusr /app

# Run the entrypoint as root instead of androidusr. The e2e-pre-production
# compose stack bind-mounts a fresh host directory over /test-results on every
# run (owned by whoever the Docker daemon runs as, not androidusr), so a
# non-root entrypoint can never reliably write results/videos there.
# HOME stays pointed at androidusr's home so avdmanager/emulator/gradle still
# find the AVD and caches created for that user at build time.
ENV HOME=/home/androidusr

# Expose ADB ports
EXPOSE 5037
EXPOSE 5554
EXPOSE 5555

# Set entry point
ENTRYPOINT ["/app/run-android-tests.sh"]

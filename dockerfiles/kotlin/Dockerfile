# Build environment for ARPCalc (Android): JDK, Android SDK and the Python
# tooling the unit tests need.  Use it via docker-build.sh or docker-compose.yml.
FROM eclipse-temurin:21-jdk-noble

LABEL maintainer="A. S. Budden <abudden@gmail.com>"

# Keep these in step with compileSdk in app/phone/build.gradle
ARG ANDROID_CMDLINE_TOOLS=16111833
ARG ANDROID_PLATFORM=android-35
ARG ANDROID_BUILD_TOOLS=35.0.0

# mercurial is for build_command.sh (keystore clone) and changeset.py
RUN apt-get update && apt-get install -y --no-install-recommends \
		ca-certificates \
		curl \
		git \
		mercurial \
		python3 \
		python3-venv \
		unzip \
	&& rm -rf /var/lib/apt/lists/*

ENV ANDROID_HOME=/opt/android-sdk
RUN mkdir -p ${ANDROID_HOME}/cmdline-tools \
	&& curl -fsSLo /tmp/cmdline-tools.zip \
		https://dl.google.com/android/repository/commandlinetools-linux-${ANDROID_CMDLINE_TOOLS}_latest.zip \
	&& unzip -q /tmp/cmdline-tools.zip -d ${ANDROID_HOME}/cmdline-tools \
	&& mv ${ANDROID_HOME}/cmdline-tools/cmdline-tools ${ANDROID_HOME}/cmdline-tools/latest \
	&& rm /tmp/cmdline-tools.zip \
	&& yes | ${ANDROID_HOME}/cmdline-tools/latest/bin/sdkmanager --licenses > /dev/null \
	&& ${ANDROID_HOME}/cmdline-tools/latest/bin/sdkmanager \
		"platforms;${ANDROID_PLATFORM}" "build-tools;${ANDROID_BUILD_TOOLS}" \
	&& chmod -R a+rX ${ANDROID_HOME}

# ConversionTest runs src/test/generate_conversion_tests.py, which needs an old
# pint: newer releases dropped some of the unit names it uses (e.g. "btu").
RUN python3 -m venv /opt/pyenv \
	&& /opt/pyenv/bin/pip install --no-cache-dir "pint==0.9" "setuptools<70"
ENV PATH=/opt/pyenv/bin:${PATH}

# The container runs as the host user (see docker-build.sh), who may not own
# the checkout as far as git is concerned.
RUN git config --system --add safe.directory '*'

# Writable by whichever user the container runs as.  /cache is a named volume
# (see docker-compose.yml) so Gradle downloads and the debug signing key
# survive between builds.
RUN mkdir -p -m 1777 /home/al /cache
ENV HOME=/home/al \
	GRADLE_USER_HOME=/cache/gradle \
	ANDROID_USER_HOME=/cache/android

COPY build-in-container.sh /usr/local/bin/arpcalc-build
WORKDIR /home/al/project
ENTRYPOINT ["arpcalc-build"]

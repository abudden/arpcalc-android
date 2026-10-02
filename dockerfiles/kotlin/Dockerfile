FROM eclipse-temurin:21-jdk-noble

LABEL maintainer="A. S. Budden <abudden@gmail.com>"

ENV PROJECT=/home/al/project
ENV ANDROID_HOME=/opt/android-sdk
ENV PATH=${PATH}:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools

# Android command-line tools release (see
# https://developer.android.com/studio#command-line-tools-only)
ARG CMDLINE_TOOLS_VERSION=13114758
ARG ANDROID_PLATFORM=android-36
ARG ANDROID_BUILD_TOOLS=36.0.0

# python & pint are used by the unit conversion tests; mercurial is used
# to fetch the keystore and to determine the changeset being built.
RUN apt-get update && apt-get install -y --no-install-recommends \
	curl \
	unzip \
	mercurial \
	python-is-python3 \
	python3-pint \
	&& rm -rf /var/lib/apt/lists/*

RUN mkdir -p ${ANDROID_HOME}/cmdline-tools \
	&& curl -fsSL -o /tmp/cmdline-tools.zip \
		https://dl.google.com/android/repository/commandlinetools-linux-${CMDLINE_TOOLS_VERSION}_latest.zip \
	&& unzip -q /tmp/cmdline-tools.zip -d ${ANDROID_HOME}/cmdline-tools \
	&& mv ${ANDROID_HOME}/cmdline-tools/cmdline-tools ${ANDROID_HOME}/cmdline-tools/latest \
	&& rm /tmp/cmdline-tools.zip \
	&& yes | sdkmanager --licenses > /dev/null \
	&& sdkmanager \
		"platform-tools" \
		"platforms;${ANDROID_PLATFORM}" \
		"build-tools;${ANDROID_BUILD_TOOLS}"

# The base image already has a user with UID 1000 ("ubuntu"): replace it.
RUN (userdel -r ubuntu || true) \
	&& useradd -s /bin/bash -m -u 1000 al \
	&& mkdir -p /home/al/.gradle \
	&& chown -R al /home/al ${ANDROID_HOME}
USER al
WORKDIR /home/al/project
CMD ["bash", "build_command.sh"]

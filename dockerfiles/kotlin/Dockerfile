FROM eneim/docker-android-project

MAINTAINER A. S. Budden <abudden@gmail.com>

ENV PROJECT /home/al/project

RUN apt-get update && apt-get install -y \
	python \
	python-imaging \
	python-pip \
	openjfx
RUN pip install pint
RUN useradd -s /bin/bash -m -u 1000 al
RUN chown -R al /home/al
RUN chown -R al /usr/local/android-sdk-linux
USER al
WORKDIR /home/al/project
CMD bash build_command.sh

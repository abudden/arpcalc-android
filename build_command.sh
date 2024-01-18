#!/bin/bash
cd /home/al
if [ -e keystore ]
then
	(cd keystore && hg pull -u)
else
	hg clone /vcs/keystore
fi
cd /home/al/project
bash /home/al/keystore/make_keystore_props.bash
echo "sdk.dir=$ANDROID_HOME" > local.properties
./gradlew --stacktrace -c settings-phone.gradle androidDependencies && \
	./gradlew --stacktrace -c settings-phone.gradle clean build assembleRelease
RESULT=$?
rm keystore.properties
exit ${RESULT}

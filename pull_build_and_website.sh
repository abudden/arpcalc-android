#!/bin/bash
# Now also automatically build-able on push to default.
webdir=/zfs.mount/website/files
hg pull -u
# "up" only fails if Compose itself does (e.g. a network problem building the
# image), so retry those; a failed Gradle build shows in the exit code below.
# --build picks up any changes to the Dockerfile.
until docker compose up --build kotlin_arpcalc_build
do
	:
done
# -a: the container has exited by now, and plain "ps" only lists running ones
exitcode=$(docker compose ps -a -q kotlin_arpcalc_build | xargs docker inspect -f '{{ .State.ExitCode }}')
if [ "${exitcode}" == "0" ]
then
	revid=$(hg id -i)
	datestr=$(date +%F)

	rm -f ${webdir}/kotlin-arpcalc-*.apk
	rm -f ${webdir}/karpcalc.apk
	cp app/phone/build/outputs/apk/release/phone-release.apk ${webdir}/kotlin-arpcalc-release-${datestr}-${revid}.apk
	cp ${webdir}/kotlin-arpcalc-release-${datestr}-${revid}.apk ${webdir}/karpcalc.apk

	echo "APK copied to website - ${datestr} ; ${revid}"
fi

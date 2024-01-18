#!/bin/bash
# Now also automatically build-able on push to default.
webdir=/zfs.mount/website/files
hg pull -u
docker-compose up kotlin_arpcalc_build
while [ "$?" != "0" ]
do
	docker-compose up kotlin_arpcalc_build
done
if [ "$(docker-compose ps -q | xargs docker inspect -f '{{ .State.ExitCode }}')" == "0" ]
then
	revid=$(hg id -i)
	datestr=$(date +%F)

	rm -f ${webdir}/kotlin-arpcalc-*.apk
	rm -f ${webdir}/karpcalc.apk
	cp app/phone/build/outputs/apk/phone-release.apk ${webdir}/kotlin-arpcalc-release-${datestr}-${revid}.apk
	cp ${webdir}/kotlin-arpcalc-release-${datestr}-${revid}.apk ${webdir}/karpcalc.apk

	echo "APK copied to website - ${datestr} ; ${revid}"
fi

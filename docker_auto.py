# Artifacts to store when performing automatic build.
artifacts = [
        {
            'source': 'app/phone/build/outputs/apk/phone-release.apk',
            'dest': 'karpcalc.apk',
            },
        {
            'source': 'app/phone/build/outputs/apk/phone-release.apk',
            'dest': 'kotlin-arpcalc-release-{date}-{changeset}.apk',
            },
        {
            'source': 'app/pc/build/libs/karpcalc.jar',
            'dest': 'karpcalc.jar',
            },
        {
            'source': 'app/pc/build/libs/karpcalc.jar',
            'dest': 'kotlin-arpcalc-release-{date}-{changeset}.jar',
            },
        {
            'source': 'app/server/build/libs/karpcalc-server.jar',
            'dest': 'karpcalc-server.jar',
            },
        {
            'source': 'app/server/build/libs/karpcalc-server.jar',
            'dest': 'kotlin-arpcalc-server-release-{date}-{changeset}.jar',
            },
        {
            'source': 'app/phone/build/reports/lint-results.html',
            'dest': 'android-lint-results.html',
            },
        ]

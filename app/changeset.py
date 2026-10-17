#!/usr/bin/env python3
"""
Works out the version from the repository, as the desktop version's
get_changeset.py does, writes it to src/main/RevisionInfo.kt (shown by a long
press on the base indicator) and prints the Android versionName and
versionCode for app/phone/build.gradle, one per line.

The version comes from the latest tag, e.g. "v1.4.0-1-d605d6a7f44b" for one
commit after the v1.4.0 tag, with Mercurial (as for the formal releases) or
otherwise git.  From that:

    versionName  1.4.0, or 1.4.0-1-d605d6a7f44b when not on the tag
    versionCode  major * 10000 + minor * 100 + patch (10400 for 1.4.0)

Play (and installing an APK over an older one) needs versionCode to go up
with every release, so each release needs a new tag.  Without a tag, or with
neither Mercurial nor git (e.g. an unofficial build from a copy of the
source), the build still works, with versionCode 1 and the changeset (if any)
as the versionName.
"""

import datetime
import os
import re
import subprocess

this_dir = os.path.abspath(os.path.dirname(__file__))
outfile = os.path.join(this_dir, 'src/main/RevisionInfo.kt')


def run(command):
    """The command's output, or None if it couldn't be run or failed"""
    try:
        p = subprocess.run(command, stdout=subprocess.PIPE, stderr=subprocess.DEVNULL,
                check=True, encoding='utf8', cwd=this_dir)
    except (subprocess.CalledProcessError, FileNotFoundError):
        return None
    return p.stdout.strip() or None


changeset = run(['hg', 'id', '-i'])
if changeset is not None:
    # e.g. v1.4.0-1-d605d6a7f44b (or null-5-... with no tags)
    version = run(['hg', 'log', '-r', '.', '--template',
            '{latesttag}-{latesttagdistance}-{node|short}'])
else:
    commit = run(['git', 'rev-parse', 'HEAD'])
    if commit is not None:
        changeset = 'git-' + commit
        # e.g. v1.4.0-1-gd605d6a (git puts a g before the commit)
        version = run(['git', 'describe', '--tags', '--long', '--match', 'v[0-9]*'])
        if version is not None:
            version = re.sub(r'-g([0-9a-f]+)$', r'-\1', version)
    else:
        changeset = 'unknown'
        version = None

match = re.match(r'^v?(\d+)\.(\d+)\.(\d+)-(\d+)-([0-9a-f]+)$', version or '')
if match and (int(match.group(2)) < 100) and (int(match.group(3)) < 100):
    major, minor, patch, distance = [int(match.group(i)) for i in range(1, 5)]
    version_name = '%d.%d.%d' % (major, minor, patch)
    if distance > 0:
        version_name += '-%d-%s' % (distance, match.group(5))
    version_code = (major * 10000) + (minor * 100) + patch
else:
    # Not tagged (or not a version we understand)
    version = changeset
    version_name = changeset
    version_code = 1

dtn = datetime.datetime.now(datetime.timezone.utc)

kotlin_string = """package uk.co.cgtk.karpcalc

object RevisionInfo {
\tconst val version = "%(version)s"
\tconst val changeset = "%(changeset)s"
\tconst val build_date = "%(date)s"
\tconst val build_datetime = "%(dt)s"
}
""" % {'version': version,
        'changeset': changeset,
        'date': dtn.strftime("%Y-%m-%d"),
        'dt': dtn.isoformat()
        }

with open(outfile, 'w', encoding='utf8') as fh:
    fh.write(kotlin_string)

print(version_name)
print(version_code)

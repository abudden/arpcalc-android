#!/usr/bin/python

from __future__ import print_function

import datetime
import os
import subprocess
import sys

if sys.hexversion >= 0x03000000:
    s = lambda x: x.decode("utf8")
    utc = datetime.timezone.utc
else:
    s = lambda x: x
    class UTC(datetime.tzinfo):
        def utcoffset(self, dt):
            return datetime.timedelta(0)
        def tzname(self, dt):
            return "UTC"
        def dst(self, dt):
            return datetime.timedelta(0)
    utc = UTC()

this_dir = os.path.abspath(os.path.dirname(__file__))
outfile = os.path.join(this_dir, 'src/main/RevisionInfo.kt')

p = subprocess.Popen(['hg', 'id', '-i'], stdout=subprocess.PIPE)
(stdout, _) = p.communicate()

changeset = s(stdout).strip()
dtn = datetime.datetime.now(utc)

kotlin_string = """package uk.co.cgtk.karpcalc

object RevisionInfo {
\tconst val changeset = "%(changeset)s"
\tconst val build_date = "%(date)s"
\tconst val build_datetime = "%(dt)s"
}
""" % {'changeset':changeset,
        'date':dtn.strftime("%Y-%m-%d"),
        'dt':dtn.isoformat()
        }

with open(outfile, "w") as fh:
    fh.write(kotlin_string)

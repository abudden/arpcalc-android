#!/usr/bin/python

from __future__ import print_function

import datetime
import os
import subprocess
import sys

if sys.hexversion <= 0x03050000:
    raise NotImplementedError("This script requires python 3.5+")

s = lambda x: x.decode("utf8")
utc = datetime.timezone.utc

this_dir = os.path.abspath(os.path.dirname(__file__))
outfile = os.path.join(this_dir, 'src/main/RevisionInfo.kt')

try:
    p = subprocess.run(['hg', 'id', '-i'], stdout=subprocess.PIPE)
    changeset = s(p.stdout).strip()
except (subprocess.CalledProcessError, FileNotFoundError):
    # hg wasn't found or didn't work.  Try git
    p = subprocess.run(['git', 'rev-parse', 'HEAD'],
                stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
                check=True, encoding='utf8')
    changeset = 'git-' + p.stdout.strip()

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

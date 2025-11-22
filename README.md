# MorphAdorner
Github mirror of the official MorphAdorner repo found here: https://bitbucket.org/eplib/morphadorner.


## Original README

Downloading MorphAdorner
------------------------

The file

    morphadorner-2.0.1.zip

contains the ready-to-use MorphAdorner client source code, data, and
libraries.  For most users this is all you need.

Current version: 2.0.1
Last update: September 25, 2013

The Mercurial repository

    http://bitbucket.org/pibburns/morphadorner

contains the source code, data files, and build configuration files
for generating the MorphAdorner release from scratch.  The repository
is intended for use by programmers who wish to modify the MorphAdorner
code.


Quick Setup
-----------

If you downloaded the MorphAdorner release from the Mercurial repository
on bitbucket.org, please go to the section "Installing and
building MorphAdorner."

If you downloaded the ready-to-use morphadorner-2.0.1.zip file,
proceed as follows.  Expand the contents of the morphadorner-2.0.1.zip
file into an empty directory.  Make sure you retain the existing directory
structure.

You must have the Java run-time environment installed on your machine
to run MorphAdorner.  If you do not, go to the section "Installing and
Building MorphAdorner" for information on where to get a copy of the
Java runtime.

Once you have Java installed you can proceed with running MorphAdorner.


File Layout of Morphadorner Release
-----------------------------------

File or Directory                          Contents
=========================  ================================================
README.txt                 Printable copy of this file in Windows text
                           format (lines terminated by Ascii cr/lf).
build.xml                  Apache Ant build file used to compile MorphAdorner.
data/                      Data files used by MorphAdorner.
dist/                      Holds generated morphadorner.jar program file.
documentation/             MorphAdorner documentation.
gatelib/                   Java libraries used by Gate.
ivy.xml                    Apache Ivy dependencies definitions.
ivysettings.xml            Apache Ivy settings.
javadoc/                   Javadoc (internal documentation).
lib/                       Java library files.
misc/                      Miscellaneous configuration files.
morphadornerlog.config     MorphAdorner logging configuration file.
src/                       MorphAdorner client source code.
xslt/                      XSLT stylesheets used by utilities.


Installing and Building MorphAdorner
------------------------------------

Extract the files from morphadorner-2.0.1.zip, retaining the
directory structure, to an empty directory. The zip file contains
precompiled (with Java 1.6) versions of all of the code as well as the
javadoc.

You do not need to rebuild the code unless you want to make
changes. If you do want to rebuild the code, make sure you have
installed recent working copies of Sun's Java Development Kit and
Apache Ant on your system.  The Java development kits for Windows, Mac OS X,
and Linux systems may be obtained from

    http://www.oracle.com/technetwork/java/javase/downloads/index.html

Alternatively, OpenJDK may be obtained from

    http://openjdk.java.net/install/index.html

You must use a Java compiler which is compatible with Java 1.6 or higher.

Apache Ant may be obtained from

    http://ant.apache.org

Move to the directory in which you extracted morphadorner-2.0.1.zip,
and type:

    ant

This should build MorphAdorner successfully.  The morphadorner.jar
file will be placed in the "dist" subdirectory.

Type

    ant javadoc

to generate the javadoc (internal documentation) into subdirectory
"javadoc".

Type

    ant clean

to remove the effects of compilation.  This does not remove the
downloaded files in the lib and gatelib subdirectories.  To remove those
as well, type

    ant cleanlib


Documentation
-------------

Printable documentation, in Adobe Acrobat PDF format, is available in
the documentation/morphadorner.pdf file in the MorphAdorner release.

MorphAdorner documentation is also available online. The online version
is generally more up-to-date than the printable version included in
the release materials. The javadoc (internal documentation) is also
available online as well as in the release materials in the javadoc/
directory. The online MorphAdorner modification history describes what has
changed from one release of MorphAdorner to the next.


Running MorphAdorner
--------------------

MorphAdorner has run successfully on Windows, Mac OS X, and various flavors
of Linux.

Before running MorphAdorner on Unix-like systems you will need to mark the
Unix script files as executable before using them.  You can use the chmod
command to do this, e.g.:

    chmod 755 adornncfa

The MorphAdorner release contains a script makescriptsexecutable which
applies chmod to each of the scripts in the release. On most Unix-like
systems you can execute makescriptsexecutable by moving to the MorphAdorner
installation directory and entering

    chmod 755 makescriptsexecutable
    ./makescriptsexecutable

or

    /bin/sh <makescriptsexecutable

The sample batch file adornncf.bat and the corresponding Linux script
adornncf shows how to run MorphAdorner to adorn simple TEI format XML
files for 19th century and later works in which quote marks are not
distinguished from apostrophes.  Use the sample batch file adornncfa.bat and
the script adornncfa for works in which quote marks are distinguished from
apostrophes.

For example, to adorn TEI XML files in directory /myfiles into the
output directory /myoutputfiles on Unix-like systems, open a terminal
window in the MorphAdorner directory and type

    ./adornncf /myoutputfiles /myfiles/*.xml

On Windows you would open a console window in the MorphAdorner directory
and type

    adornncf \myoutputfiles \myfiles\*.xml

Please see the documentation section "Adorning a Text" in the online
web site or the printable PDF for more information on these and other
sample batch files and scripts in the MorphAdorner release.

There are presumably lots of warts, misfeatures, bugs, missing items, and
whatnot.  Use MorphAdorner with caution.


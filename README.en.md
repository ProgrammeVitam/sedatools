Pour uns présentation en français, suivre [ce lien](README.md).


![logo](logo_vitam.png)

sedatools project
=================

The project contains tools useful for developpers and testers for construction and manipulation of SEDA SIP.
It's a maven project with three trois modules:

* ``sedalib``: the SEDA library code
* ``sedalib-samples``: some usage examples demonstrating complex SIP construction in a few lines
* ``resip``: the SIP creation and manipulation GUI

Build
-----

With JDK 1.8, git, maven and gpg installed, the build sequence is:

    mkdir test-sedatools
    cd test-sedatools
    git clone https://github.com/ProgrammeVitam/java-libpst.git
    cd java-libpst/
    # mvn parameters to skip javadoc errors in 1.8
    mvn clean install -Dadditionalparam=-Xdoclint:none
    cd ..
    git clone https://github.com/ProgrammeVitam/mailextract.git
    cd mailextract/
    mvn clean install
    cd ..
    git clone https://github.com/digital-preservation/droid.git
    cd droid
    # mvn parameters to skip a test failure in non english environement
    mvn clean install -DskipTests
    cd ..
    git clone https://github.com/ProgrammeVitam/sedatools.git
    cd sedatools/
    mvn clean install

The sedalib library
===================

This library is intended to manage archives structures and metadata confirming to the SEDA Standard (Standard d’échange de données pour l’archivage – SEDA – v. 2.1).

Sample application execution
----------------------------

    cd sedalib-samples
    java -jar target/sedalib-samples-{VERSION}-shaded.jar

The TestSipGenerator tool
=============================
This command line tool generate SIP used for injection tests.
You can choose depth of ArchiveUnit tree, number ans size of standard objects
distributed in the tree, number and size of big objects...

It has been developed with sedalib, and is so a sedalib usage sample code.


Execution
---------
To get the generation options, you can use --help ou -h argument.

    cd ../testsipgenerator
    java -jar target/testsipgenerator-{VERSION}-shaded.jar -h

On Windows, it's also possible to execute: windows/TestSipGenerator.exe -h


The Resip application
=====================

This is the SIP creation and manipulation GUI.

Execution
---------

    cd ../resip
    java -jar target/resip-{VERSION}-shaded.jar

On Windows, it's also possible to execute: windows/Resip.exe


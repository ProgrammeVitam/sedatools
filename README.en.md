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

    mkdir test-sedatools
    cd test-sedatools
    git clone https://gitlab.dev.programmevitam.fr/jslair/mailextract.git
    cd mailextract/
    mvn install
    cd ..
    git clone https://github.com/rjohnsondev/java-libpst.git
    cd java-libpst/
    mvn install
    cd ..
    git clone https://github.com/digital-preservation/droid.git
    cd droid/
    mvn install
    cd ..
    git clone https://gitlab.dev.programmevitam.fr/jslair/sedatools.git
    cd sedatools/
    mvn install

The sedalib library
===================

This library is intended to manage archives structures and metadata confirming to the SEDA Standard (Standard d’échange de données pour l’archivage – SEDA – v. 2.1).

Sample application execution
----------------------------

    cd sedalib-samples
    java -jar target/sedalib-samples-0.9-SNAPSHOT-shaded.jar

L'application Resip
====================

This is the SIP creation and manipulation GUI.

Execution
---------

    cd ../resip
    java -jar target/resip-0.9-SNAPSHOT-shaded.jar

On Windows, it's also possible to execute: windows/Resip.exe


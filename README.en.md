Pour uns présentation en français, suivre [ce lien](README.md).


![logo](logo_vitam.png)

The SEDA library
================

This library is intended to manage archives structures and metadata confirming to the SEDA Standard (Standard d’échange de données pour l’archivage – SEDA – v. 2.1).

Sructure du projet
==================

Modules content:

* ``sedalib``: the SEDA library code
* ``sedalib-samples``: some usage examples demonstrating complex SIP construction in a few lines

Build
=====

    mkdir test
    cd test
    git clone https://github.com/digital-preservation/droid.git
    cd droid/
    mvn install
    cd ..
    git clone https://gitlab.dev.programmevitam.fr/jslair/sedatools.git
    cd sedatools/
    mvn install
    java -jar target/sedalib-samples-0.9-SNAPSHOT-shaded.jar

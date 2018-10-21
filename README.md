For a quick presentation in english, please follow [this link](README.en.md).


![logo](logo_vitam.png)

Projet sedatools
================

Le projet contient les outils utiles aux développeurs et testeurs pour la construction et manipulation des SIP conforme au SEDA.
Il s'agit d'un projet Maven avec trois modules qui contiennent:

* ``sedalib``: le code de la bibliothèque SEDA
* ``sedalib-samples``: le code d'exemples d'usage pour construire des SIP complexes en peu de lignes
* ``resip``: le code de l'application de création et manipulation des SIP s'appuyant sur la bibliothèque SEDA

Build
-----

Avec un JDK 1.8, git et maven installés, la séquence de build est la suivante:

    mkdir test-sedatools
    cd test-sedatools
    git clone https://github.com/rjohnsondev/java-libpst.git
    cd java-libpst/
    mvn install
    cd ..
    git clone https://gitlab.dev.programmevitam.fr/vitam/mailextract.git
    cd mailextract/
    mvn install
    cd ..
    git clone https://github.com/digital-preservation/droid.git
    cd droid/
    mvn install
    cd ..
    git clone https://gitlab.dev.programmevitam.fr/jslair/sedatools.git
    cd sedatools/
    mvn install

La bibliothèque sedalib et ses exemples
=======================================

Cette bibliothèque permet de manipuler les structures et métadonnées du
standard SEDA (Standard d’échange de données pour l’archivage – SEDA – v. 2.1).

Execution de l'application d'exemple
------------------------------------

    cd sedalib-samples/
    java -jar target/sedalib-samples-0.9-SNAPSHOT-shaded.jar

A noter les paquets générés, peuvent être entrés dans une plateforme VITAM
sur le tenant de test 0. Les valeurs de référentiels sont prises parmi celles
des Tests de Non-Regression exécuté sur ce même tenant.

Première approche
-----------------

La bibliothèque permet de manipuler d'une part les structures d'archives
(dans le SEDA DataObjectPackage, ArchiveUnit, DataObjectGroup...) et les
métadonnées de ces objets de structures qu'elles soient descriptives (Content,
Title, Writer, Event...) ou de gestion (Management, AccessRule...).

Elle permet en un minimum d'appel de former des structures, y compris complexe
avec des liens multiples, et de définir leurs métadonnées, mais pour pleinement
comprendre ce que l'on manipule il convient de prendre connaissance du standard
[SEDA](https://francearchives.fr/seda/documentation.html). A voir plus
particulièrement la forme structurée du manifest d'un SIP à savoir le message
[ArchiveTransfer](https://francearchives.fr/seda/api_v2-1/seda-2_1-main_xsd.html#ArchiveTransfer),
le [DataObjectPackage](https://francearchives.fr/seda/api_v2-1/seda-2_1-main_xsd.html#BusinessMessageType_DataObjectPackage)
qui contient toutes les métadonnées et la structure du lot d'archives et
l'[ArchiveUnit](https://francearchives.fr/seda/api_v2-1/seda-2_1-main_xsd.html#DescriptiveMetadataType_ArchiveUnit)
qui est la brique élémentaire de structuration.

La classe principale pour construire un SIP avec des commandes de haut niveau
est **SIPBuilder** dans le package fr.gouv.vitam.sedalib.inout. Les autres
classes utiles en première approche sont celles des métadonnées au premier
rang desquelles toutes celles de métadonnées des ArchiveUnit à savoir:

* **Content**, qui contient toutes les métadonnées descriptives,
* **Management** qui contient toutes les métadonnées de gestion et
* **ArchiveUnitProfiles** qui défini un profil d'unité archivistique...

Pour voir comment utiliser ces classes le plus simple est de commencer par
voir les applications d'exemples et leurs commentaires, puis d'élargir le
champ de possibles au travers de la JavaDoc et du code de la bibliothèque.

Les différents exemples démontrent:

* Sample1: la construction d'un SIP par simple réplication de la structure
sur disque d'un enesmeble de fichiers
* Sample2: la construction d'un SIP à partir d'une part une arborescence sur
disque et d'autre part en construisant une arborescence de dossiers avec des
métadonnées dans des fichiers
* Sample3: la construction d'un SIP sensiblement équivalent mais en prenant
les métadonnées dans un csv et en les injectant dans les archives des dossiers
* Sample3plus: la construction d'un SIP ajoutant des dossiers à un archivage
précédent fait avec Sample3

L'application Resip
====================

Cette application permet toutes sortes de manipulations de structures d'archives que cela soit sous forme
SIP, DIP ou hiérarchie sur disque. Elle a été créée pour "Réaliser et Editer des SIP" pour les recettes d'où RESIP.

Execution
---------

    cd ../resip
    java -jar target/resip-0.9-SNAPSHOT-shaded.jar

Sous Windows, il est aussi possible de lancer l'exécutable: windows/Resip.exe


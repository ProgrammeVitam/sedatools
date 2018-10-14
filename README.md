For a quick presentation in english, please follow [this link](README.en.md).


![logo](logo_vitam.png)

La bibliothèque Seda
====================

Cette bibliothèque permet de manipuler les structures et métadonnées du
standard SEDA (Standard d’échange de données pour l’archivage – SEDA – v. 2.1).

Sructure du projet
==================

Les modules contiennent:

* ``sedalib``: le code de la bibliothèque SEDA
* ``sedalib-samples``: le code d'exemples d'usage pour construire des SIP complexes en peu de lignes

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

Première approche
=================

La bibliothèque permet de manipuler d'une part les structures d'archives (dans le SEDA DataObjectPackage, ArchiveUnit, DataObjectGroup...) et les métadonnées de ces objets de structures qu'elles soient descriptives (Content, Title, Writer, Event...) ou de gestion (Management, AccessRule...).

Elle permet en un minimum d'appel de former des structures, y compris complexe avec des liens multiples, et de définir leurs métadonnées, mais pour pleinement comprendre ce que l'on manipule il convient de prendre connaissance du standard [SEDA](https://francearchives.fr/seda/documentation.html). A voir plus particulièrement la forme structurée du manifest d'un SIP à savoir le message [ArchiveTransfer](https://francearchives.fr/seda/api_v2-1/seda-2_1-main_xsd.html#ArchiveTransfer), le [DataObjectPackage](https://francearchives.fr/seda/api_v2-1/seda-2_1-main_xsd.html#BusinessMessageType_DataObjectPackage) qui contient toutes les métadonnées et la structure du lot d'archives et l'[ArchiveUnit](https://francearchives.fr/seda/api_v2-1/seda-2_1-main_xsd.html#DescriptiveMetadataType_ArchiveUnit) qui est la brique élémentaire de structuration.

La classe principale pour construire un SIP avec des commandes de haut niveau est **SIPBuilder** dans le package fr.gouv.vitam.sedalib.inout. Les autres classes utiles en première approche sont celles des métadonnées au premier rang desquelles toutes celles de métadonnées des ArchiveUnit à savoir:

* **Content**, qui contient toutes les métadonnées descriptives,
* **Management** qui contient toutes les métadonnées de gestion et
* **ArchiveUnitProfiles** qui défini un profil d'unité archivistique...

Pour voir comment utiliser ces classes le plus simple est de commencer par voir les applications d'exemples et leurs commentaires, puis d'élargir le champ de possibles au travers de la JavaDoc et du code de la bibliothèque.

Les différents exemples démontrent:

* Sample1: la construction d'un SIP par simple réplication de la structure sur disque d'un enesmeble de fichiers
* Sample2: la construction d'un SIP à partir d'une part une arborescence sur disque et d'autre part en construisant une arborescence de dossiers avec des métadonnées dans des fichiers
* Sample3: la construction d'un SIP sensiblement équivalent mais en prenant les métadonnées dans un csv et en les injectant dans les archives des dossiers
* Sample3plus: la construction d'un SIP ajoutant des dossiers à un archivage précédent fait avec Sample3
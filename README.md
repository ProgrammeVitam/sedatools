For a quick presentation in english, please follow [this link](README.en.md).


![logo](logo_vitam.png)

Projet sedatools
================

Le projet contient les outils utiles aux développeurs et testeurs pour la construction et manipulation des SIP conforme au SEDA.
Il s'agit d'un projet Maven avec six modules qui contiennent:

* ``sedalib``: le code de la bibliothèque SEDA (manipulation de paquets SEDA)
* ``sedalib-samples``: le code d'exemples d'usage pour construire des SIP complexes en peu de lignes
* ``mailextractlib``: le code de la bibliothèque mailextract (extraction conforme SEDA de messagerie)

et
* ``resip``: le code de l'application de création et manipulation des SIP s'appuyant sur les bibliothèques SEDA et mailextract
* ``mailextract``: le code de l'application permettant toutes les extractions de messagerie s'appuyant sur la bibliothèque mailextract
* ``testsipgenerator``: le code de l'application permettant de générer des paquets SIP simulés pour test

Build
-----

Avec un JDK 1.8, git, maven et gpg installés, la séquence de build est la suivante:

    mkdir test-sedatools
    cd test-sedatools
    git clone https://github.com/ProgrammeVitam/java-libpst-origin.git
    cd java-libpst-origin/
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

La bibliothèque sedalib et ses exemples
=======================================

Cette bibliothèque permet de manipuler les structures et métadonnées du
standard SEDA (Standard d’échange de données pour l’archivage – SEDA – v. 2.1).

Execution de l'application d'exemple
------------------------------------

    cd sedalib-samples/
    java -jar target/sedalib-samples-{VERSION}-shaded.jar

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
sur disque d'un ensemble de fichiers
* Sample2: la construction d'un SIP à partir d'une part une arborescence sur
disque et d'autre part en construisant une arborescence de dossiers avec des
métadonnées dans des fichiers
* Sample3: la construction d'un SIP sensiblement équivalent mais en prenant
les métadonnées dans un csv et en les injectant dans les archives des dossiers
* Sample3plus: la construction d'un SIP ajoutant des dossiers à un archivage
précédent fait avec Sample3

La bibliothèque mailextract 
===========================
Cette bibliothèque permet de réaliser l'extraction ou l'édition de la structure de 
boites de messageries de différentes sources:
* serveur *IMAP* or *IMAPS* avec identifiant/mot de passe (et tous standards connus de JavaMail pop3, gmail...)
* *répertoire Thunderbird* contenant des fichiers mbox et des hiérarchies en .sbd
* fichier *Outlook pst*, *Microsoft msg*, *RFC-4155 mbox* et *RFC-822 eml*

L'extraction génère une structure de répertoire et fichiers (format "sur disque") représentant une structure 
d'Archive Unit au sens du SEDA (NF Z44-022) et directement utilisable par sedalib. Elle normalise aussi l'extraction, quelqu'en soit la source même pst, en eml (RFC 822). 
Pour plus d'information voir le javadoc de la classe StoreExtractor.
 
Elle utilise :
* la bibliothèque JavaMail pour l'extraction de boites distantes (imap,imaps...) et dans les fichiers Thunderbird,
* la bibliothèque java-libpst pour l'extraction de boites dans les fichiers Outlook (merci à Richard Johnson http://github.com/rjohnsondev),
* la bibliothèque POI HSMF library pour l'extraction des messages .msg de Microsoft.

Note: Pour l'instant elle ne permet pas d'extraire de messages S/MIME (chiffrés ou signés).

l'application MailExtract
=========================
Cette application s'appuyant sur mailextractlib fourni une capacité en ligne de commande et une IHM d'extraction 
de toutes sortes de messageries (pst, thunderbird, imap, msg, mbox...) sous forme d'arborescence disque conforme au SEDA et directement utilisables par sedalib.

Execution
---------

    cd mailextract
    java -jar target/mailextract-{VERSION}-shaded.jar

Sous Windows, il est aussi possible de lancer l'exécutable: windows/MailExtract.exe

Elle peut être lancée avec des arguments en ligne de commande (voir ci-dessous), ou sans option pour avoir directement l'interface graphique.
Pour avoir toutes les possibilités d'options, il suffit d'utiliser l'argument --help ou -h.

Pour information la syntaxe des arguments est:

|Option|Description|
|--------|----------|
|*help*|aide|
|*type TYPE*|type de conteneur local à extraire (thunderbird|pst|eml|mbox) ou protocole d'accès distant (imap|imaps|pop3...)|
|*user USERNAME*|nom d'utilisateur de la boite (aussi utilisé pour générer le nom de l'extraction)|
|*password PASSWORD*|mot de passe|
|*server [HostName|IP](:port)*|serveur de messagerie [HostName|IP](:port)|
|*container ONDISK*|localisation sur le disque du conteneur local à utiliser (répertoire Thunderbird, fichier pst, eml, mbox, msg...)|
|*folder INMAIL*|répertoire particulier de la boite à partir duquel faire l'extraction ou l'édition de la structure|
|*rootdir ONDISK*|répertoire de base sur le disque (par défaut répertoire courant de la console) pour l'extraction (extraction en root/username[-timestamp])|
|*dropemptyfolders*|n'extrait pas les répertoires n'ayant aucun message en direct ou dans son arborescence|
|*keeponlydeep*|garde les répertoires vides sauf ceux à la racine (il existe couramment des répertoires non utilisés à côté de INBOX)|
|*verbatim LOGLEVEL*|niveau de log (OFF|GLOBAL|WARNING|FOLDER|MESSAGE_GROUP|MESSAGE|MESSAGE_DETAILS), valeur par défaut OFF.|
|*nameslength NUMBER*|longueur limite des noms de répertoires et fichiers générés|
|*setchar ENCODING*|jeu d'encodage de caratère par défaut, notamment utile pour l'extraction correct des pst, valeur par défaut jeu de caractère de l'OS.|
|*extractlists*|génère les listes csv d'objets (messages, contacts, rendez-vous) le cas échéant|
|*extractmessagetextfile*|extrait un fichier avec le texte du message|
|*extractmessagetextmetadata*|inclus le texte du message dans les métadonnées|
|*extractfiletextfile*|extrait un fichier avec le texte des fichiers attachés|
|*extractfiletextmetadata*|inclus le texte du fichier attaché dans les métadonnées|
|*model NUMBER*|modèle d'extraction sur disque 1 (generateurseda) ou 2 (sedalib), valeur par défaut 2|
|*warning*|génère un avertissement quand il y a un problème d'extraction limité à un message en particulier (sinon cela est loggé au niveau FINE)|
|*x*|fait l'extraction|
|*l*|édite l'ensemble des répertoires de la messagerie (ne prend pas en compte les options -d et -k)|
|*z*|édite l'ensemble des répertoires de la messagerie ainsi que le nombre et le poids des messages qu'ils contiennent (ne prend pas en compte les options -d et -k)|

Si le niveau de log est autre chose que OFF l'opération, d'extraction ou d'édition, sera loggée sur la console et dans un fichier 
(en rootdir/username.log).

Selon le niveau choisi de log vous aurez: informations sur le process global (GLOBAL), avertissement sur 
les problèmes d'extraction et les items abandonnés (WARNING), liste des répertoires traités (FOLDER), 
décompte de messages traités (MESSAGE_GROUP), liste des messages traités (MESSAGE), 
problèmes avec certaines méta-données (MESSAGE_DETAILS).

A noter: Si aucune option -x, -l ou -z n'est mise l'interface graphique est lancée avec les éléments complétés.

Les libellés long des options peut être réduit au premier caractère précédé d'un seul - (par exemple -h est équivalent à --help)

**AVERTISSEMENT**: Editer la liste des répertoires avec le nombre et poids des messages est une opération potentiellement lourde sur un serveur distant car cela nécessite d'importer l'ensemble des messages.

Interface graphique
-------------------

![mailextractIHM](mailextract/windows/mailextractIHM.png)


L'application Resip
====================

Cette application permet toutes sortes de manipulations de structures d'archives que cela soit sous forme
SIP, DIP ou hiérarchie sur disque. Elle a été créée pour "Réaliser et Editer des SIP" pour les recettes d'où RESIP.

Execution
---------

    cd resip
    java -jar target/resip-{VERSION}-shaded.jar

Sous Windows, il est aussi possible de lancer l'exécutable: windows/Resip.exe

Elle peut être lancée avec des arguments en ligne de commande (voir ci-dessous), ou sans option pour avoir directement l'interface graphique.
Pour avoir toutes les possibilités d'options, il suffit d'utiliser l'argument --help ou -h.

Pour information la syntaxe des arguments est:

|Option|Description|
|--------|----------|
|*help*|aide|
|*diskimport ONDISK*|importe une hiérarchie d'AU depuis une hiérarchie de répertoires et fichiers avec en argument le répertoire racine XXXX|
|*exclude ONDISK*|exclu de l'import par diskimport les fichiers dont le nom sont conformes aux expressions régulières contenue sur chaque ligne du fichier|
|*listimport ONDISK*|importe une hiérarchie d'AU depuis un ensemble de répertoires et de fichiers dont la hiérarchie et les métadonnées sont décrits dans un csv|
|*sipimport ONDISK*|importe une hiérarchie d'AU depuis un SIP SEDA avec en argument le nom du fichier|
|*context ONDISK*|défini les informations globales utiles à la génération du SIP (MessageIdentifier...) dans le fichier indiqué|
|*generatesip ONDISK*|génère un paquet SEDA SIP de la structure importée avec en argument le nom du fichier à générer|
|*manifest ONDISK*|génère le manifest SEDA de la structure importée avec en argument le nom du fichier à générer|
|*workdir ONDISK*|désigne le répertoire de travail pour les logs, les répertoires d'extraction temporaire|
|*xcommand*|ne lance pas l'interface graphique|
|*hierarchical*|génère les ArchiveUnits en mode hiérarchique dans le manifest SEDA|
|*indented*|génère le manifest SEDA en XML indenté|
|*verbatim*|niveau de log (OFF|ERROR|GLOBAL|STEP|OBJECTS_GROUP|OBJECTS|OBJECTS_WARNINGS)*|;

Si le niveau de log est autre chose que OFF l'opération, d'extraction ou d'édition, sera loggée sur la console et dans un fichier 
(en workdir/log.txt).

Selon le niveau choisi de log vous aurez: informations sur le process global (GLOBAL), avertissement sur 
les problèmes n'empêchant pas le process global (WARNING), liste des étapes effectuées (STEP), 
décompte des unités d'archives et des objets traités (OBJECTS_GROUP), liste des unités d'archives 
et des objets traités (OBJECTS), 
problèmes de détails sur les objets (OBJECTS_WARNINGS).

Interface graphique
-------------------

![resipIHM](resip/windows/resipIHM.png)


L'utilitaire TestSipGenerator
=============================
Cet utilitaire en ligne de commande génère des SIP utilisés pour des tests
d'injection. Vous pouvez choisir la profondeur de l'arborescence, le
nombre et la taille d'objets standards répartis dans l'arbre, le nombre
et la taille de gros objets...

Il a été développé en utilisant sedalib et constitue donc aussi un exemple 
de code.

Execution
---------

    cd testsipgenerator
    java -jar target/testsipgenerator-{VERSION}-shaded.jar -h

Sous Windows, il est aussi possible de lancer l'exécutable: windows/TestSipGenerator.exe -h
Pour avoir toutes les possibilités d'options, il suffit d'utiliser l'argument --help ou -h.


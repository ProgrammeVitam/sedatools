\##### SEDA - Version 2.3 \| Standard d'Echange de Données pour
l'Archivage \#####

Vous cherchez [une autre version du
SEDA](https://github.com/culturecommunication/seda/tree/main/README.rst#3historique-des-versions) ?

<div class="section-numbering">

</div>

------------------------------------------------------------------------

<div class="contents">

</div>

------------------------------------------------------------------------

# Les caractéristiques du SEDA v2.3

## Introduction au SEDA

Consulter [la page d'accueil](https://github.com/culturecommunication/seda/tree/main/)

## Genèse du SEDA 2.3

**La version 2.3** du Standard d'échanges de données pour l'archivage
(SEDA) -publiée en **juin 2024** sous l'égide du Service
interministériel des Archives de France- comprend six schémas, un
dictionnaire et une documentation sommaire des nouveautés. La
documentation du standard est en cours de refonte. Aussi, la
documentation intégrale de la v2.3 sera-t-elle publiée en décembre 2024.

Les schémas résultent des travaux menés entre 2022 et 2024 par le Comité
de pilotage du SEDA, qui rassemble des acteurs œuvrant pour
l’utilisation de ce standard d'échange dans les services publics
d’archives, chez les tiers-archiveurs, les éditeurs de logiciels
d’archivage électronique ou encore les cabinets spécialisés dans
l'accompagnement de projets d'archivage électronique.

Les nouveautés par rapport à la version 2.2 :

-   Prise en charge la **signature numérique des documents**.
-   Possibilité d'ajouter des **identifiants pérennes** (DOI, ARK, etc.)
    aux métadonnées descriptives et aux groupes d'objets techniques
    (numériques et/ou physiques).
-   Possibilité de distinguer **l'usage** et la **version** des objets
    (numériques ou physiques), en complément ou alternative à l'élément
    `DataObjectVersion` existant.

Note de version (changelog) ---------------Les principaux changements
**par rapport à la version 2.2** sont :

**Signature numérique :**

-   Dépréciation du bloc `SignatureGroup`. Attention `SignatureGroup`
    est désactivé par sa simple mise en commentaires dans les fichiers
    XSD. Dans les versions suivantes du SEDA (à partir de la v2.4), il
    sera définitivement supprimé des schémas.
-   Ajout du bloc `SigningInformation` permettant de décrire les
    informations de signature de l'objet binaire associé à l'unité
    archivistique.

**Identifiants pérennes :**

-   Ajout d'un bloc `PersistentIdentifier`.

**Distinction des versions et usages des objets :**

Deux nouveaux éléments facultatifs (`DataObjectUse` et
`DataObjectNumber`) ont été ajoutés afin de distinguer la notion d’usage
et la notion de version :

-   Au niveau des objets binaires.
-   Au niveau des objets physiques.

# Schémas XML

[Les schémas](https://github.com/culturecommunication/seda/tree/seda-2.3/schema) traduisent la forme des messages échangés au
cours des transactions.

# Documentation

## Documentation générale
- [SEDA 2.3 \| Documentation sommaire des
nouveautés](https://raw.githubusercontent.com/culturecommunication/seda/seda-2.3/doc/seda-2.3-documentation_sommaire.pdf)
-   [SEDA 2.3 \| Guide
    d'introduction](https://github.com/culturecommunication/seda/raw/refs/heads/seda-2.3/doc/seda-2.3-guide_introduction_v0.5.pdf)
-   SEDA 2.3 \| Dictionnaire des balises `[à paraître]`
-   SEDA 2.3 \| Documentation fonctionnelle `[à paraître]`

## Documentation technique

-   [SEDA 2.3 \| Documentation HTML des schémas
    XML](https://raw.githubusercontent.com/culturecommunication/seda/seda-2.3/doc/seda-2.3-XML-schema_documentation_HTML.zip)
    `[ZIP à télécharger]`
-   [SEDA 2.3 \| Diagrammes SVG des schémas
    XML](https://raw.githubusercontent.com/culturecommunication/seda/seda-2.3/doc/seda-2.3-XML-schema_documentation_SVG.zip)
    `[ZIP à télécharger]`
-   [SEDA 2.3 \| Consulter la documentation technique en
    ligne](https://francearchives.gouv.fr/seda/2.3/)

# Outils

## Outils spécifiques à la version 2.3

-   [Feuille de transformation (XSLT) SEDA v2.3 vers
    EAD2002](https://github.com/culturecommunication/seda/tree/seda-2.3/outils/transformation/SEDA_2.x_vers_EAD2002/XSLT)
-   [Feuille de transformation (XSLT) SEDA v2.3 vers
    HTML](https://github.com/culturecommunication/seda/tree/seda-2.3/outils/transformation/SEDA_2.x_vers_HTML/XSLT)

## Outils génériques

- [Une palette d'outils](https://github.com/culturecommunication/seda/tree/outils/) facilite le traitement de vos
archives avec le SEDA !

# Téléchargement

[Télécharger l'ensemble du SEDA 2.3](https://github.com/culturecommunication/seda/releases/tag/seda_v2.3/)
(schémas XML et documentation) `[Bientôt]`

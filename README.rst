#####
VITAM
#####

.. section-numbering::

For a quick presentation in english, please follow `this link <README.en.rst>`_.


.. image:: doc/fr/logo_vitam.png
        :alt: Logo Vitam
        :align: center

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

.. code-block:: bash

    mkdir test
    cd test
    git clone https://gitlab.dev.programmevitam.fr/jslair/sedatools.git
    cd sedatools/
    mvn install
    java -jar target/sedalib-samples-0.9-SNAPSHOT-shaded.jar

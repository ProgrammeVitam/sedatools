# Standard d'échange de données pour l'archivage (SEDA)
Cette version 2.2 du Standard d'échanges de données pour l'archivage (SEDA) publiée en janvier 2022 sous l'égide du Service interministériel des Archives de France,
comprend six schémas et un dictionnaire.
Ils sont le fruit du travail mené en 2020-2021 par le Comité de pilotage du SEDA, qui rassemble des acteurs œuvrant pour l’utilisation de ce standard d'échange
dans les services publics d’archives, chez les tiers-archiveurs, les éditeurs de logiciels d’archivage électronique ou encore les cabinets spécialisés dans
l'accompagnement de projets d'archivage électronique.

Le SEDA modélise 6 transactions qui peuvent avoir lieu entre des acteurs dans le cadre de l'archivage de données :
le transfert, la demande de transfert, la modification, l'élimination, la communication et la restitution.
Les présents schémas traduisent formellement la forme des messages échangés au cours des transactions.
Ils ont été réalisés par le Cabinet Mintika à partir des principes définis en Comité de pilotage.
Les principaux changements par rapport à la version 2.1 publiée en juin 2018 sur https://francearchives.fr/seda/
sont:
- Modification de AgentType générique dans l’ontologie;
- Dépréciation de AgentAbstract;
- Ajout d'une nouvelle règle de gestion HoldRule dans les métadonnées de gestion;
- Ajout de HoldRuleCodeListVersion;
- Ajout de OriginatingSystemIdReplyToGroup et de TextContent (pour les mails);
- Ajout de LinkingAgentIdentifierType dans les Event;
- Ajout de DataObjectProfile dans les métadonnées techniques;
- Ajout de DateLitteral dans l’ontologie;
- Modification du type de MessageIdentifier (devient NonEmptyToken).

Le dictionnaire des balises SEDA est proposé quant à lui dans une version de travail. Il synthétise par grands ensembles de métadonnées (gestion, description, technique, transport et typologie de messages) les éléments présents dans les schémas du standard. 

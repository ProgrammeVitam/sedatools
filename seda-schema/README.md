# seda-schema

Exploitation des XSD SEDA : au lieu de recopier la structure du standard dans des classes Java,
le schéma est lu à l'exécution et pilote la structuration du contenu.

Le module ne dépend pas de `sedalib`. Il embarque les XSD SEDA 2.1, 2.2 et 2.3 (`sedaschema/xsd/`)
mais charge tout aussi bien n'importe quelle XSD (`SedaSchema.load(URL)`).

## Les deux variantes : officielle et Vitam

Pour 2.1 et 2.2, la XSD publiée par les Archives de France et celle qu'implémente Vitam — donc
`sedalib` et ReSIP — ne sont pas le même schéma. Les deux sont embarquées et se choisissent par
`SedaFlavor` :

```java
SedaSchema officielle = SedaSchema.of(SedaVersion.V2_1);                      // OFFICIAL par défaut
SedaSchema vitam      = SedaSchema.of(SedaVersion.V2_1, SedaFlavor.VITAM);
```

| | `sedaschema/xsd/official/` | `sedaschema/xsd/vitam/` |
|---|---|---|
| Points d'extension `*Abstract` (`OtherManagementAbstract`, `EventAbstract`…) | déclarés, substituables | mis en commentaire, remplacés par `xsd:any processContents="lax"` |
| `Management/UpdateOperation` (+ `UpdateOperationType`, `ArchiveUnitIdentifierKeyType`) | absent | présent en 2.1 et 2.2 |
| `ArchiveUnit/Management/History` (`ManagementHistoryType`) | absent | présent en 2.1 et 2.2 |
| `ArchiveUnit/DataObjectGroup` (`ObjectGroupRefType`) | absent | présent en 2.1 et 2.2 |
| `HoldRule` | depuis 2.2 | rétroporté en 2.1 |
| 2.3 | — | identique à l'officielle, rien n'est dupliqué |

`SedaVersion.hasVitamVariant()` dit si la version diverge ; à partir de 2.3 les ajouts Vitam sont
entrés dans le standard et `getMainXsdResource(VITAM)` renvoie la ressource officielle.

## Ce qu'il fournit

| Package | Rôle |
|---|---|
| `sedaschema` | `SedaSchema` : chargement (Xerces `XSModel`), racines, types nommés, résolution de chemins (`getElementAt("ArchiveTransfer/.../Content/Title")`) ; `SedaVersion` et `SedaFlavor` : les schémas embarqués |
| `sedaschema.model` | Le modèle de schéma : `ElementDef`, `ComplexTypeDef`, `SimpleTypeDef` (énumérations, motifs, facettes), `Particle`/`Group`/`Wildcard` avec `Occurrence` |
| `sedaschema.node` | `SedaNode`, instance générique d'élément SEDA ; `SedaNodeReader`/`SedaNodeWriter` XML ; `StructureHelper` (enfants ajoutables, position d'insertion, instance minimale) ; `NodeValidator` (modèle de contenu, attributs, énumérations, motifs) |
| `sedaschema.json` | `SchemaJsonExporter` : le schéma en JSON pour un client qui ne lit pas de XSD (front web générant ses formulaires) |

## Exemple

```java
SedaSchema schema = SedaSchema.of(SedaVersion.V2_3);
ElementDef auDef = schema.getElementAt("ArchiveTransfer/DataObjectPackage/DescriptiveMetadata/ArchiveUnit");

SedaNode au = new SedaNode(auDef).setAttribute("id", "ID12");
SedaNode content = StructureHelper.add(au, "Content");
StructureHelper.add(content, "Title").setText("Dossier 1");
StructureHelper.add(content, "DescriptionLevel").setText("File");   // inséré avant Title, comme l'exige le schéma

StructureHelper.addableChildren(au);   // [ArchiveUnitProfile[1], Management[1], ArchiveUnit[*], DataObjectReference[*]]
NodeValidator.validate(au);            // [] : structure valide
new SedaNodeWriter().toXml(au);

String json = new SchemaJsonExporter().toJson(schema);   // ~280 Ko avec la documentation, 220 sans
```

## Limites

- `NodeValidator` est un retour immédiat pour l'édition (ordre, cardinalités, choix, énumérations,
  motifs). La validation de référence d'un manifeste reste la validation XSD complète par Xerces.
- Les types abstraits et `xsi:type` ne sont pas résolus : un élément de type abstrait (`OpenType`
  via `OtherCoreTechnicalMetadataAbstract`…) est lu tel quel.
- Les profils d'unités archivistiques (RNG) ne sont pas pris en compte.
- Un manifeste produit par Vitam ou ReSIP en 2.1 ou 2.2 ne se lit qu'avec `SedaFlavor.VITAM` : les
  métadonnées d'extension ne sont pas déclarées dans la XSD officielle.

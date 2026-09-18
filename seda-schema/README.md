# seda-schema

Exploitation des XSD SEDA officielles : au lieu de recopier la structure du standard dans des
classes Java, le schéma est lu à l'exécution et pilote la structuration du contenu.

Le module ne dépend pas de `sedalib`. Il embarque les XSD SEDA 2.1, 2.2 et 2.3 (`sedaschema/xsd/`)
mais charge tout aussi bien n'importe quelle XSD (`SedaSchema.load(URL)`).

## Ce qu'il fournit

| Package | Rôle |
|---|---|
| `sedaschema` | `SedaSchema` : chargement (Xerces `XSModel`), racines, types nommés, résolution de chemins (`getElementAt("ArchiveTransfer/.../Content/Title")`) |
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

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
| `OpenType` | `xsd:any namespace="##other"` : extension hors namespace SEDA | `xsd:any` sans contrainte de namespace |
| `Management/UpdateOperation` (+ `UpdateOperationType`, `ArchiveUnitIdentifierKeyType`) | absent | présent en 2.1, 2.2 et 2.3 |
| `ArchiveUnit/Management/History` (`ManagementHistoryType`) | absent | présent en 2.1, 2.2 et 2.3 |
| `ArchiveUnit/DataObjectGroup` (`ObjectGroupRefType`) | absent | présent en 2.1, 2.2 et 2.3 |
| `HoldRule` | depuis 2.2 | rétroporté en 2.1 |
| `SigningInformation/Extended` (2.3) | de type `OpenType`, abstrait : inutilisable sans `xsi:type` | de type concret `ExtendedType` |

Les trois versions ont leur variante Vitam (`SedaVersion.hasVitamVariant()`). Celle de 2.3 est la
XSD de validation de Vitam (`seda-2.3/vitam-validation` dans les sources de Vitam), renommée
`seda-vitam-2.3-*.xsd`.

### Conformité de la variante Vitam au standard

Les ajouts Vitam sont dans l'esprit du standard, mais pas dans sa forme.

Le modèle officiel prévoit ses extensions par **substitution** : sept éléments abstraits déclarés
dans `seda-2.x-types.xsd`, sous le commentaire « *Objets à spécifier par substitution* », auxquels
une XSD tierce vient substituer ses propres éléments. Celui de `ManagementGroup` décrit mot pour
mot le besoin auquel répond `UpdateOperation` :

```xml
<!-- In ManagementGroup from seda-2.1-management.xsd: for Management extension -->
<xsd:element name="OtherManagementAbstract" abstract="true">
  <xsd:documentation>Utilisé par exemple pour manipuler un ArchiveUnit déjà existant
                     dans le système d'archivage électronique.</xsd:documentation>
```

Le second vecteur, `OpenType`, est un `<xsd:any namespace="##other" processContents="lax"/>` :
l'extension est permise, mais obligatoirement dans un autre namespace que celui du SEDA.

La variante Vitam n'emprunte ni l'un ni l'autre. Elle met les points d'extension en commentaire,
les remplace par des jokers sans contrainte de namespace — un en officiel, huit en Vitam 2.1 et
2.2 — et déclare ses métadonnées comme éléments locaux dans le namespace SEDA lui-même :

```xml
<!--xsd:element ref="ArchiveUnitReferenceAbstract"> … </xsd:element-->
<!-- Redefined by VITAM -->
<xsd:element name="DataObjectGroup" type="ObjectGroupRefType"/>
<xsd:any processContents="lax" minOccurs="0"/>
```

Validation d'un manifeste ReSIP 2.1 contre la XSD officielle 2.1 :

| manifeste | contre la XSD officielle 2.1 |
|---|---|
| sans métadonnée hors standard | valide |
| avec `<Management><UpdateOperation>` (namespace SEDA) | rejeté, `This element is not expected` |
| avec `<vitam:UpdateOperation>`, sans XSD d'extension | rejeté |
| avec `<vitam:UpdateOperation>` et une XSD déclarant `substitutionGroup="seda:OtherManagementAbstract"` | valide |

Une quinzaine de lignes de XSD d'extension auraient donc suffi à rendre la même métadonnée
conforme.

Deux conséquences pratiques :

- **L'inclusion ne va que dans un sens.** Un manifeste valide pour l'officiel l'est pour Vitam, plus
  permissif ; l'inverse est faux dès qu'une métadonnée Vitam apparaît. Exception en sens inverse en
  2.2, où Vitam restreint `MessageIdentifier` de `IdentifierType` à `NonEmptyTokenType` : un
  `MessageIdentifier` vide, valide pour le standard, est refusé par Vitam.
- **Les XSD Vitam ne sont pas des schémas XSD valides** (constaté en 2.1 et 2.2, la 2.3 reprend la
  même construction). Un `xsd:any` sans restriction de
  namespace dans un `choice` répété, à côté d'éléments nommés, viole la règle *Unique Particle
  Attribution*. libxml2 refuse de les compiler (`complex type 'ArchiveUnitType': The content model
  is not determinist`). Elles se chargent ici parce que Xerces désactive par défaut
  `schema-full-checking`, qui porte ce contrôle, et que ni ce module ni `SEDAXMLValidator` ne
  l'activent. Avec un validateur tiers, elles peuvent ne pas charger du tout.

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

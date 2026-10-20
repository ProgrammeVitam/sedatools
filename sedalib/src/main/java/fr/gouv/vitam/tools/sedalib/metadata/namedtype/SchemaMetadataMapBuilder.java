/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to provide
 * tools for construction and manipulation of SIP (Submission
 * Information Package) conform to the SEDA (Standard d’Échange
 * de données pour l’Archivage) standard.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitam.tools.sedalib.metadata.namedtype;

import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.core.PhysicalDataObject;
import fr.gouv.vitam.tools.sedalib.core.seda.SedaVersion;
import fr.gouv.vitam.tools.sedalib.metadata.ManagementMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.Operation;
import fr.gouv.vitam.tools.sedalib.metadata.compacted.DocumentContainer;
import fr.gouv.vitam.tools.sedalib.metadata.compacted.DocumentPack;
import fr.gouv.vitam.tools.sedalib.metadata.content.AdditionalProof;
import fr.gouv.vitam.tools.sedalib.metadata.content.Content;
import fr.gouv.vitam.tools.sedalib.metadata.content.Coverage;
import fr.gouv.vitam.tools.sedalib.metadata.content.CustodialHistory;
import fr.gouv.vitam.tools.sedalib.metadata.content.DataObjectReference;
import fr.gouv.vitam.tools.sedalib.metadata.content.Event;
import fr.gouv.vitam.tools.sedalib.metadata.content.Gps;
import fr.gouv.vitam.tools.sedalib.metadata.content.Keyword;
import fr.gouv.vitam.tools.sedalib.metadata.content.PersistentIdentifier;
import fr.gouv.vitam.tools.sedalib.metadata.content.ReferencedObject;
import fr.gouv.vitam.tools.sedalib.metadata.content.RelatedObjectReference;
import fr.gouv.vitam.tools.sedalib.metadata.content.Rule;
import fr.gouv.vitam.tools.sedalib.metadata.content.Signature;
import fr.gouv.vitam.tools.sedalib.metadata.content.SignatureDescription;
import fr.gouv.vitam.tools.sedalib.metadata.content.Signer;
import fr.gouv.vitam.tools.sedalib.metadata.content.SigningInformation;
import fr.gouv.vitam.tools.sedalib.metadata.content.TimestampingInformation;
import fr.gouv.vitam.tools.sedalib.metadata.content.Validator;
import fr.gouv.vitam.tools.sedalib.metadata.data.FileInfo;
import fr.gouv.vitam.tools.sedalib.metadata.data.FormatIdentification;
import fr.gouv.vitam.tools.sedalib.metadata.data.Metadata;
import fr.gouv.vitam.tools.sedalib.metadata.data.PhysicalDimensions;
import fr.gouv.vitam.tools.sedalib.metadata.data.Relationship;
import fr.gouv.vitam.tools.sedalib.metadata.data.Weight;
import fr.gouv.vitam.tools.sedalib.metadata.management.AccessRule;
import fr.gouv.vitam.tools.sedalib.metadata.management.AppraisalRule;
import fr.gouv.vitam.tools.sedalib.metadata.management.ArchiveUnitIdentifierKey;
import fr.gouv.vitam.tools.sedalib.metadata.management.ClassificationRule;
import fr.gouv.vitam.tools.sedalib.metadata.management.DisseminationRule;
import fr.gouv.vitam.tools.sedalib.metadata.management.HoldRule;
import fr.gouv.vitam.tools.sedalib.metadata.management.LogBook;
import fr.gouv.vitam.tools.sedalib.metadata.management.Management;
import fr.gouv.vitam.tools.sedalib.metadata.management.ReuseRule;
import fr.gouv.vitam.tools.sedalib.metadata.management.StorageRule;
import fr.gouv.vitam.tools.sedalib.metadata.management.ToDelete;
import fr.gouv.vitam.tools.sedalib.metadata.management.UpdateOperation;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedaschema.SedaFlavor;
import fr.gouv.vitam.tools.sedaschema.SedaSchema;
import fr.gouv.vitam.tools.sedaschema.SedaSchemaException;
import fr.gouv.vitam.tools.sedaschema.model.ComplexTypeDef;
import fr.gouv.vitam.tools.sedaschema.model.ElementDef;
import fr.gouv.vitam.tools.sedaschema.model.Particle;
import fr.gouv.vitam.tools.sedaschema.model.SimpleTypeDef;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Builds a metadataMap from the SEDA XSD read by seda-schema, instead of hand writing it.
 * <p>
 * The schema gives the children of a complex type, their order and their cardinality. The Java
 * class of each child comes from three tables: the complex types that have a sedalib class
 * ({@link #CLASS_BY_XSD_TYPE}), the types whose content is a value ({@link #LEAF_BY_XSD_TYPE},
 * then the XSD built in base through {@link #LEAF_BY_BUILT_IN}), and the handful of elements
 * sedalib reads with another class than its type would give ({@link #OVERRIDES}).
 * <p>
 * Three more tables hold what the schema can't give: the elements it declares that sedalib keeps
 * elsewhere ({@link #EXCLUDED}), the metadata sedalib adds outside of the schema
 * ({@link #ADDITIONS}, the Vitam compaction extension among them) and the classes that accept any
 * other element ({@link #EXPANDABLE}).
 */
public final class SchemaMetadataMapBuilder {

    private SchemaMetadataMapBuilder() {}

    /**
     * The XSD complex type each class with a metadataMap implements.
     */
    private static final Map<Class<?>, String> XSD_TYPE_BY_CLASS = new LinkedHashMap<>();

    static {
        XSD_TYPE_BY_CLASS.put(Content.class, "DescriptiveMetadataContentType");
        XSD_TYPE_BY_CLASS.put(Management.class, "ManagementType");
        XSD_TYPE_BY_CLASS.put(ManagementMetadata.class, "ManagementMetadataType");
        XSD_TYPE_BY_CLASS.put(AccessRule.class, "AccessRuleType");
        XSD_TYPE_BY_CLASS.put(AppraisalRule.class, "AppraisalRuleType");
        XSD_TYPE_BY_CLASS.put(ClassificationRule.class, "ClassificationRuleType");
        XSD_TYPE_BY_CLASS.put(DisseminationRule.class, "DisseminationRuleType");
        XSD_TYPE_BY_CLASS.put(HoldRule.class, "HoldRuleType");
        XSD_TYPE_BY_CLASS.put(ReuseRule.class, "ReuseRuleType");
        XSD_TYPE_BY_CLASS.put(StorageRule.class, "StorageRuleType");
        XSD_TYPE_BY_CLASS.put(LogBook.class, "LogBookType");
        XSD_TYPE_BY_CLASS.put(UpdateOperation.class, "UpdateOperationType");
        XSD_TYPE_BY_CLASS.put(ToDelete.class, "ToDeleteType");
        XSD_TYPE_BY_CLASS.put(ArchiveUnitIdentifierKey.class, "ArchiveUnitIdentifierKeyType");
        XSD_TYPE_BY_CLASS.put(Event.class, "EventType");
        XSD_TYPE_BY_CLASS.put(CustodialHistory.class, "CustodialHistoryType");
        XSD_TYPE_BY_CLASS.put(Keyword.class, "KeywordsType");
        XSD_TYPE_BY_CLASS.put(Coverage.class, "CoverageType");
        XSD_TYPE_BY_CLASS.put(Gps.class, "GpsType");
        XSD_TYPE_BY_CLASS.put(RelatedObjectReference.class, "RelatedObjectReferenceType");
        XSD_TYPE_BY_CLASS.put(DataObjectReference.class, "DataObjectRefType");
        XSD_TYPE_BY_CLASS.put(DataObjectOrArchiveUnitReferenceType.class, "DataObjectOrArchiveUnitReferenceType");
        XSD_TYPE_BY_CLASS.put(PersistentIdentifier.class, "PersistentIdType");
        XSD_TYPE_BY_CLASS.put(SigningInformation.class, "SigningInformationType");
        XSD_TYPE_BY_CLASS.put(SignatureDescription.class, "SignatureDescriptionType");
        XSD_TYPE_BY_CLASS.put(TimestampingInformation.class, "TimestampingInformationType");
        XSD_TYPE_BY_CLASS.put(AdditionalProof.class, "AdditionalProofType");
        XSD_TYPE_BY_CLASS.put(Signature.class, "SignatureType");
        XSD_TYPE_BY_CLASS.put(Signer.class, "SignerType");
        XSD_TYPE_BY_CLASS.put(Validator.class, "ValidatorType");
        XSD_TYPE_BY_CLASS.put(ReferencedObject.class, "ReferencedObjectType");
        XSD_TYPE_BY_CLASS.put(FileInfo.class, "FileInfoType");
        XSD_TYPE_BY_CLASS.put(FormatIdentification.class, "FormatIdentificationType");
        XSD_TYPE_BY_CLASS.put(Metadata.class, "CoreMetadataType");
        XSD_TYPE_BY_CLASS.put(PhysicalDimensions.class, "DimensionsType");
        XSD_TYPE_BY_CLASS.put(AgencyType.class, "OrganizationType");
        XSD_TYPE_BY_CLASS.put(AgentType.class, "AgentType");
        XSD_TYPE_BY_CLASS.put(PlaceType.class, "BirthOrDeathPlaceType");
        XSD_TYPE_BY_CLASS.put(LinkingAgentIdentifier.class, "LinkingAgentIdentifierType");
        XSD_TYPE_BY_CLASS.put(Operation.class, "OperationType");
        XSD_TYPE_BY_CLASS.put(BinaryDataObject.class, "BinaryDataObjectType");
        XSD_TYPE_BY_CLASS.put(PhysicalDataObject.class, "PhysicalDataObjectType");
    }

    /**
     * The class of a child whose XSD type holds elements: the table above read the other way,
     * plus the types sedalib keeps as raw XML.
     */
    private static final Map<String, Class<?>> CLASS_BY_XSD_TYPE = new LinkedHashMap<>();

    static {
        for (Map.Entry<Class<?>, String> e : XSD_TYPE_BY_CLASS.entrySet()) CLASS_BY_XSD_TYPE.putIfAbsent(
            e.getValue(),
            e.getKey()
        );
        CLASS_BY_XSD_TYPE.put("OpenType", AnyXMLListType.class);
        CLASS_BY_XSD_TYPE.put("AudioTechnicalMetadataType", AnyXMLListType.class);
        CLASS_BY_XSD_TYPE.put("DescriptiveTechnicalMetadataType", AnyXMLListType.class);
        CLASS_BY_XSD_TYPE.put("DocumentTechnicalMetadataType", AnyXMLListType.class);
        CLASS_BY_XSD_TYPE.put("ImageTechnicalMetadataType", AnyXMLListType.class);
        CLASS_BY_XSD_TYPE.put("TextTechnicalMetadataType", AnyXMLListType.class);
        CLASS_BY_XSD_TYPE.put("VideoTechnicalMetadataType", AnyXMLListType.class);
        CLASS_BY_XSD_TYPE.put("OrganizationDescriptiveMetadataType", AnyXMLType.class);
        CLASS_BY_XSD_TYPE.put("ManagementHistoryType", AnyXMLType.class);
    }

    /**
     * The class of a child whose XSD type holds a value, by type name.
     */
    private static final Map<String, Class<?>> LEAF_BY_XSD_TYPE = new LinkedHashMap<>();

    static {
        LEAF_BY_XSD_TYPE.put("TextType", TextType.class);
        LEAF_BY_XSD_TYPE.put("MeasurementType", LinearDimensionType.class);
        LEAF_BY_XSD_TYPE.put("MeasurementWeightType", Weight.class);
        LEAF_BY_XSD_TYPE.put("MessageDigestBinaryObjectType", DigestType.class);
        LEAF_BY_XSD_TYPE.put("RelationshipType", Relationship.class);
        LEAF_BY_XSD_TYPE.put("KeyType", EnumType.class);
        LEAF_BY_XSD_TYPE.put("LevelType", EnumType.class);
        LEAF_BY_XSD_TYPE.put("SigningRoleType", EnumType.class);
        LEAF_BY_XSD_TYPE.put("DetachedSigningRoleType", EnumType.class);
        LEAF_BY_XSD_TYPE.put("DateType", DateTimeType.class);
        LEAF_BY_XSD_TYPE.put("GroupIdType", StringType.class);
        LEAF_BY_XSD_TYPE.put("ArchiveUnitRefIdType", SIPInternalIDType.class);
        LEAF_BY_XSD_TYPE.put("DataObjectRefIdType", SIPInternalIDType.class);
        LEAF_BY_XSD_TYPE.put("GroupRefIdType", SIPInternalIDType.class);
    }

    /**
     * The class of a child whose XSD type is nothing more than one of the built in types.
     */
    private static final Map<String, Class<?>> LEAF_BY_BUILT_IN = new LinkedHashMap<>();

    static {
        LEAF_BY_BUILT_IN.put("boolean", BooleanType.class);
        LEAF_BY_BUILT_IN.put("date", DateType.class);
        LEAF_BY_BUILT_IN.put("dateTime", DateTimeType.class);
        LEAF_BY_BUILT_IN.put("int", IntegerType.class);
        LEAF_BY_BUILT_IN.put("integer", IntegerType.class);
        LEAF_BY_BUILT_IN.put("positiveInteger", IntegerType.class);
        LEAF_BY_BUILT_IN.put("IDREF", SIPInternalIDType.class);
    }

    /**
     * The elements sedalib reads with another class than their XSD type gives. Each one is a
     * singularity of the maps, kept as is so that building the map changes nothing: a sedalib
     * choice for Rule and RefNonRuleId, an inconsistency with a sibling class for the others.
     */
    private static final Map<String, Class<?>> OVERRIDES = new LinkedHashMap<>();

    static {
        // the rule identifier carries the rule metadata, see RuleType and RuleMetadataKind
        for (String rule : new String[] {
            "AccessRule",
            "AppraisalRule",
            "ClassificationRule",
            "DisseminationRule",
            "HoldRule",
            "ReuseRule",
            "StorageRule",
        }) {
            OVERRIDES.put(rule + ".Rule", Rule.class);
            OVERRIDES.put(rule + ".RefNonRuleId", StringType.class);
        }
        // FinalAction and LegalStatus are enumerations in the XSD, read as plain strings
        OVERRIDES.put("AppraisalRule.FinalAction", StringType.class);
        OVERRIDES.put("StorageRule.FinalAction", StringType.class);
        OVERRIDES.put("ManagementMetadata.LegalStatus", StringType.class);
        // NeedAuthorization is a boolean, read as a string, unlike PreventInheritance
        OVERRIDES.put("Management.NeedAuthorization", StringType.class);
        OVERRIDES.put("ManagementMetadata.NeedAuthorization", StringType.class);
        // the same dates are read as DateType in Signer and AgentType
        OVERRIDES.put("Validator.BirthDate", StringType.class);
        OVERRIDES.put("Validator.DeathDate", StringType.class);
        // the group reference is an IDREF, read as a string in the data objects
        OVERRIDES.put("BinaryDataObject.DataObjectGroupReferenceId", StringType.class);
        OVERRIDES.put("PhysicalDataObject.DataObjectGroupReferenceId", StringType.class);
        // the log book is typed in Management, raw XML in ManagementMetadata
        OVERRIDES.put("ManagementMetadata.LogBook", AnyXMLType.class);
        // these hold text with a language, read without it
        OVERRIDES.put("Content.DocumentType", StringType.class);
        OVERRIDES.put("Content.Type", StringType.class);
        OVERRIDES.put("Coverage.Juridictional", StringType.class);
        OVERRIDES.put("Coverage.Spatial", StringType.class);
        OVERRIDES.put("Coverage.Temporal", StringType.class);
        OVERRIDES.put("Event.EventDetail", StringType.class);
        OVERRIDES.put("Keyword.KeywordContent", StringType.class);
    }

    /**
     * Elements the schema declares that the map leaves out, by class: sedalib holds them another
     * way.
     */
    private static final Map<Class<?>, Set<String>> EXCLUDED = new LinkedHashMap<>();

    static {
        // the rule start date is carried by the Rule metadata, see RuleType and RuleMetadataKind
        for (Class<?> c : XSD_TYPE_BY_CLASS.keySet()) if (RuleType.class.isAssignableFrom(c)) EXCLUDED.put(
            c,
            Set.of("StartDate")
        );
        // the binary content is kept as a file and referenced by Uri, never inlined in the manifest
        EXCLUDED.put(BinaryDataObject.class, Set.of("Attachment"));
    }

    /**
     * Metadata sedalib holds although the schema of the version doesn't declare it, appended in
     * this order after the children of the schema.
     */
    private static final List<Addition> ADDITIONS = new ArrayList<>();

    static {
        // Vitam extensions of 2.1, in the standard from 2.2 on
        ADDITIONS.add(
            new Addition(Content.class, null, SedaVersion.V2_1, "OriginatingSystemIdReplyTo", StringType.class, false)
        );
        ADDITIONS.add(new Addition(Content.class, null, SedaVersion.V2_1, "TextContent", StringType.class, true));
        // Vitam compaction extension, outside of the standard
        ADDITIONS.add(new Addition(Content.class, null, null, "DocumentContainer", DocumentContainer.class, false));
        ADDITIONS.add(new Addition(Content.class, null, null, "DocumentPack", DocumentPack.class, false));
        // UpdateOperation left the XSD in 2.3, sedalib keeps it as the Vitam extension it was
        ADDITIONS.add(
            new Addition(Management.class, SedaVersion.V2_3, null, "UpdateOperation", UpdateOperation.class, false)
        );
        ADDITIONS.add(
            new Addition(ManagementMetadata.class, SedaVersion.V2_3, null, "UpdateOperation", UpdateOperation.class, false)
        );
    }

    /**
     * The classes that accept any other element besides the ones of their map.
     */
    private static final Set<Class<?>> EXPANDABLE = Set.of(
        Content.class,
        Event.class,
        HoldRule.class,
        Management.class,
        ManagementMetadata.class,
        Metadata.class,
        PhysicalDataObject.class
    );

    /**
     * One metadata outside of the schema.
     */
    private static final class Addition {

        private final Class<?> owner;
        // the range of versions it is added in, null for an open bound
        private final SedaVersion since;
        private final SedaVersion until;
        private final String name;
        private final Class<?> metadataClass;
        private final boolean many;

        private Addition(
            Class<?> owner,
            SedaVersion since,
            SedaVersion until,
            String name,
            Class<?> metadataClass,
            boolean many
        ) {
            this.owner = owner;
            this.since = since;
            this.until = until;
            this.name = name;
            this.metadataClass = metadataClass;
            this.many = many;
        }
    }

    /**
     * The maps already built, by class then version: building one walks the schema, serving one
     * happens on every metadata read, write and edit.
     */
    private static final Map<Class<?>, Map<SedaVersion, LinkedHashMap<String, ComplexListMetadataKind>>> CACHE =
        new ConcurrentHashMap<>();

    /**
     * Same as {@link #build(Class, SedaVersion)}, built once and kept.
     *
     * @param clazz   a class with a metadataMap
     * @param version the SEDA version
     * @return the map, or null when the class or its type is not in this version
     * @throws SEDALibException if the schema can't be read
     */
    public static LinkedHashMap<String, ComplexListMetadataKind> get(Class<?> clazz, SedaVersion version)
        throws SEDALibException {
        // the cache can't key on a missing version, which is served the maps it stands for
        if (version == null) version = bundledVersion(null);
        Map<SedaVersion, LinkedHashMap<String, ComplexListMetadataKind>> byVersion = CACHE.computeIfAbsent(
            clazz,
            c -> new EnumMap<>(SedaVersion.class)
        );
        synchronized (byVersion) {
            if (!byVersion.containsKey(version)) byVersion.put(version, build(clazz, version));
            return byVersion.get(version);
        }
    }

    /**
     * @param clazz a class with a metadataMap
     * @return true if this class accepts any other element besides the ones of its map
     */
    public static boolean isExpandable(Class<?> clazz) {
        return EXPANDABLE.contains(clazz);
    }

    /**
     * @return the classes with a metadataMap the schema declares, and their XSD complex type
     */
    public static Map<Class<?>, String> getXsdTypes() {
        return java.util.Collections.unmodifiableMap(XSD_TYPE_BY_CLASS);
    }

    /**
     * @param clazz a class with a metadataMap
     * @return true if the schema declares the complex type of this class
     */
    public static boolean isKnown(Class<?> clazz) {
        return XSD_TYPE_BY_CLASS.containsKey(clazz);
    }

    /**
     * @return the XSD complex type of this class, or null
     */
    public static String getXsdTypeName(Class<?> clazz) {
        return XSD_TYPE_BY_CLASS.get(clazz);
    }

    /**
     * Builds the metadataMap of a class from the schema of a version.
     *
     * @param clazz   a class with a metadataMap
     * @param version the SEDA version
     * @return the children the schema declares, in its order, or null when the class or its type
     * is not in this version
     * @throws SEDALibException if the schema can't be read
     */
    public static LinkedHashMap<String, ComplexListMetadataKind> build(Class<?> clazz, SedaVersion version)
        throws SEDALibException {
        String typeName = XSD_TYPE_BY_CLASS.get(clazz);
        if (typeName == null) return null;
        SedaVersion bundled = bundledVersion(version);
        fr.gouv.vitam.tools.sedaschema.SedaVersion schemaVersion = bundled.getSchemaVersion();
        if (schemaVersion == null) return null;
        ComplexTypeDef type;
        try {
            type = SedaSchema.of(schemaVersion, SedaFlavor.VITAM).getComplexType(typeName);
        } catch (SedaSchemaException e) {
            throw new SEDALibException("Lecture du schéma SEDA " + version + " impossible", e);
        }
        if (type == null) return null;
        LinkedHashMap<String, ComplexListMetadataKind> map = new LinkedHashMap<>();
        collect(clazz, type.getContent(), false, map);
        map.keySet().removeAll(EXCLUDED.getOrDefault(clazz, Set.of()));
        for (Addition addition : ADDITIONS) if (
            addition.owner.equals(clazz) && bundled.isBetween(addition.since, addition.until)
        ) map.put(addition.name, new ComplexListMetadataKind(addition.metadataClass, addition.many));
        return map;
    }

    /**
     * @param version the SEDA version, null when none is set
     * @return the version whose XSD serves it: no XSD is bundled for the 2.0, which sedalib has
     * always served the 2.1 maps for, and with no version set the hand written maps served their
     * first one, the 2.1 one
     */
    private static SedaVersion bundledVersion(SedaVersion version) {
        return version == null || version == SedaVersion.V2_0 ? SedaVersion.V2_1 : version;
    }

    private static void collect(
        Class<?> owner,
        Particle particle,
        boolean inRepeatableGroup,
        Map<String, ComplexListMetadataKind> out
    ) {
        if (particle == null) return;
        boolean many = inRepeatableGroup || particle.getOccurrence().isRepeatable();
        if (particle.isElement()) {
            ElementDef element = particle.getElement();
            if (element.isAbstract()) return;
            ComplexListMetadataKind known = out.get(element.getName());
            if (known != null) {
                if (many) known.setMany(true);
                return;
            }
            Class<?> metadataClass = metadataClassOf(owner.getSimpleName(), element);
            // a rule block repeats as a whole: its members other than the rule identifier are
            // regrouped around each Rule, which RuleMetadataKind marks (see RuleType)
            boolean inRuleBlock =
                inRepeatableGroup && RuleType.class.isAssignableFrom(owner) && !RULE_ELEMENT.equals(element.getName());
            out.put(
                element.getName(),
                inRuleBlock
                    ? new RuleMetadataKind(metadataClass, many)
                    : new ComplexListMetadataKind(metadataClass, many)
            );
        } else if (particle.isGroup()) {
            for (Particle child : particle.getGroup().getParticles()) collect(owner, child, many, out);
        }
    }

    private static final String RULE_ELEMENT = "Rule";

    private static Class<?> metadataClassOf(String owner, ElementDef element) {
        Class<?> override = OVERRIDES.get(owner + "." + element.getName());
        if (override != null) return override;

        ComplexTypeDef complex = element.getComplexType();
        if (complex != null && !complex.isAnonymous()) {
            Class<?> byType = complex.hasElementContent()
                ? CLASS_BY_XSD_TYPE.get(complex.getName())
                : LEAF_BY_XSD_TYPE.get(complex.getName());
            if (byType != null) return byType;
            if (complex.hasElementContent()) return AnyXMLType.class;
        }
        SimpleTypeDef simple = element.getSimpleType() != null ? element.getSimpleType() : element.getTextType();
        if (simple != null) {
            if (!simple.isAnonymous()) {
                Class<?> byName = LEAF_BY_XSD_TYPE.get(simple.getName());
                if (byName != null) return byName;
            }
            if (simple.isEnumeration()) return EnumType.class;
            Class<?> byBase = LEAF_BY_BUILT_IN.get(simple.getBuiltInBase());
            if (byBase != null) return byBase;
        }
        return StringType.class;
    }
}

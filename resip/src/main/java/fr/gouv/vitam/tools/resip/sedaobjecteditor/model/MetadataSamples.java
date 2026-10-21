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
package fr.gouv.vitam.tools.resip.sedaobjecteditor.model;

import fr.gouv.vitam.tools.sedalib.core.AbstractUnitaryDataObject;
import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;
import fr.gouv.vitam.tools.sedalib.core.PhysicalDataObject;
import fr.gouv.vitam.tools.sedalib.metadata.ArchiveUnitProfile;
import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.content.Rule;
import fr.gouv.vitam.tools.sedalib.metadata.data.Relationship;
import fr.gouv.vitam.tools.sedalib.metadata.data.Weight;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.*;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedaschema.SedaVersion;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Builds the sample metadata the editors propose when a metadata is added: a minimal sample, with
 * the main fields only and empty values, or a full one, with all the fields and example values.
 */
public final class MetadataSamples {

    /**
     * The fields of a complex metadata kept in a minimal sample.
     */
    public static final List<String> MINIMAL_TAGS = List.of(
        "DescriptionLevel",
        "Title",
        "Description",
        "CustodialHistoryItem",
        "KeywordContent",
        "Identifier",
        "FirstName",
        "BirthName",
        "Address",
        "PostalCode",
        "City",
        "Region",
        "Country",
        "EventType",
        "EventDateTime",
        "EventDetail",
        "Signer",
        "SigningTime",
        "Validator",
        "ValidationTime",
        "ReferencedObject",
        "SignedObjectId",
        "SignedObjectDigest",
        "GpsVersionID",
        "GpsAltitude",
        "GpsAltitudeRef",
        "GpsLatitude",
        "GpsLatitudeRef",
        "GpsLongitude",
        "GpsLongitudeRef",
        "AppraisalRule",
        "AccessRule",
        "HoldRule",
        "Rule",
        "ClassificationLevel",
        "FinalAction",
        "FormatLitteral",
        "MimeType",
        "FormatId",
        "Filename",
        "LastModified",
        "LinkingAgentIdentifierType",
        "LinkingAgentIdentifierValue",
        "PersistentIdentifierType",
        "PersistentIdentifierContent",
        "SigningRole"
    );

    private static final String[] METADATA_PACKAGES = {
        "fr.gouv.vitam.tools.sedalib.metadata.content.",
        "fr.gouv.vitam.tools.sedalib.metadata.namedtype.",
        "fr.gouv.vitam.tools.sedalib.metadata.management.",
        "fr.gouv.vitam.tools.sedalib.metadata.data.",
        "fr.gouv.vitam.tools.sedalib.metadata.",
    };

    private MetadataSamples() {}

    /**
     * Finds a metadata class by its simple name, in the sedalib metadata packages.
     *
     * @param simpleName the simple name of the class, such as "Keyword"
     * @return the class
     * @throws SEDALibException if no metadata class has this name
     */
    public static Class<?> metadataClass(String simpleName) throws SEDALibException {
        for (String metadataPackage : METADATA_PACKAGES) {
            try {
                return Class.forName(metadataPackage + simpleName);
            } catch (ClassNotFoundException ignored) {
                // in the next package
            }
        }
        throw new SEDALibException("Le type de métadonnée [" + simpleName + "] n'est pas connu");
    }

    /**
     * Builds a sample metadata of a type given by the simple name of its class.
     *
     * @param sedaVersion  the SEDA version of the sample, used by the complex metadata
     * @param metadataType the simple name of the metadata class, such as "Keyword"
     * @param elementName  the XML element name of the sample
     * @param minimal      true for a minimal sample, false for a full one
     * @return the sample
     * @throws SEDALibException if the type is unknown or has no sample
     */
    public static SEDAMetadata sample(
        SedaVersion sedaVersion,
        String metadataType,
        String elementName,
        boolean minimal
    ) throws SEDALibException {
        return sample(sedaVersion, metadataClass(metadataType), elementName, minimal);
    }

    /**
     * Builds a sample metadata of a class.
     *
     * @param sedaVersion   the SEDA version of the sample, used by the complex metadata
     * @param metadataClass the metadata class
     * @param elementName   the XML element name of the sample
     * @param minimal       true for a minimal sample, false for a full one
     * @return the sample
     * @throws SEDALibException if the class has no sample
     */
    public static SEDAMetadata sample(
        SedaVersion sedaVersion,
        Class<?> metadataClass,
        String elementName,
        boolean minimal
    ) throws SEDALibException {
        // the exact class, as a subclass such as ArchiveUnitProfile has its own sample
        if (metadataClass == StringType.class) return new StringType(elementName, minimal ? "" : "Text");
        if (metadataClass == TextType.class) return new TextType(elementName, minimal ? "" : "Text");
        if (metadataClass == BooleanType.class) return new BooleanType(elementName, minimal ? null : false);
        if (metadataClass == DateTimeType.class) return minimal
            ? new DateTimeType(elementName)
            : new DateTimeType(elementName, LocalDateTime.of(1970, 1, 1, 1, 0, 0));
        if (metadataClass == DateType.class) return minimal
            ? new DateType(elementName)
            : new DateType(elementName, LocalDate.of(2000, 1, 1));
        if (metadataClass == IntegerType.class) return minimal
            ? new IntegerType(elementName)
            : new IntegerType(elementName, 42);
        if (metadataClass == DigestType.class) return minimal
            ? new DigestType(elementName, "")
            : new DigestType(elementName, "Hash", "SHA-512");
        if (metadataClass == SIPInternalIDType.class) return new SIPInternalIDType(elementName, minimal ? "" : "ID11");
        if (metadataClass == LinearDimensionType.class) return minimal
            ? new LinearDimensionType(elementName)
            : new LinearDimensionType(elementName, 42.42, "metre");
        if (metadataClass == EnumType.class) {
            List<String> enumValues = EnumTypeConstants.enumListMap.get(elementName);
            if (enumValues == null) throw new SEDALibException("Type Enuméré [" + elementName + "] inconnu");
            return new EnumType(elementName, enumValues.get(0));
        }
        if (metadataClass == AnyXMLType.class) return new AnyXMLType(
            "AnyXMLType",
            minimal
                ? ""
                : "<" + elementName + "><BlockTag1>Text1</BlockTag1><BlockTag2>Text2</BlockTag2></" + elementName + ">"
        );
        if (metadataClass == ArchiveUnitProfile.class) return new ArchiveUnitProfile(minimal ? "" : "AUP-00001");
        if (metadataClass == Rule.class) return minimal
            ? new Rule("", null)
            : new Rule("Text", LocalDate.of(2000, 1, 1));
        if (metadataClass == Relationship.class) return new Relationship();
        if (metadataClass == Weight.class) return minimal ? new Weight() : new Weight(42.42, "gram");
        if (ComplexListType.class.isAssignableFrom(metadataClass)) return complexSample(
            sedaVersion,
            metadataClass,
            elementName,
            minimal
        );
        throw new SEDALibException("L'éditeur de métadonnée [" + metadataClass.getSimpleName() + "] n'existe pas");
    }

    private static ComplexListType newComplexListType(
        Class<?> metadataClass,
        SedaVersion sedaVersion,
        String elementName
    ) throws SEDALibException {
        try {
            try {
                return (ComplexListType) metadataClass.getConstructor(SedaVersion.class).newInstance(sedaVersion);
            } catch (NoSuchMethodException e) {
                Constructor<?> named = metadataClass.getConstructor(SedaVersion.class, String.class);
                return (ComplexListType) named.newInstance(sedaVersion, elementName);
            }
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            throw new SEDALibException(
                "La création d'une métadonnée de type [" + metadataClass + "] n'est pas possible",
                e
            );
        } catch (InvocationTargetException e) {
            throw new SEDALibException(
                "La création d'une métadonnée de type [" + metadataClass + "] a généré une erreur",
                e.getTargetException()
            );
        }
    }

    private static ComplexListType complexSample(
        SedaVersion sedaVersion,
        Class<?> metadataClass,
        String elementName,
        boolean minimal
    ) throws SEDALibException {
        ComplexListType result = newComplexListType(metadataClass, sedaVersion, elementName);
        result.elementName = elementName;
        for (Map.Entry<String, ComplexListMetadataKind> e : result.getMetadataMap().entrySet()) {
            if (minimal && !MINIMAL_TAGS.contains(e.getKey())) continue;
            // a full sample shows a repeatable field twice
            int count = (e.getValue().isMany() && !minimal) ? 2 : 1;
            for (int i = 0; i < count; i++) {
                // by the simple name, so that the compaction metadata, out of the searched packages, have
                // no sample: the compacted documents are built by the compaction, never edited
                SEDAMetadata field = sample(
                    sedaVersion,
                    e.getValue().getMetadataClass().getSimpleName(),
                    e.getKey(),
                    minimal
                );
                try {
                    result.addMetadata(field);
                } catch (SEDALibException ignored) {
                    // no real case
                }
            }
        }
        if (!result.isNotExpandable() && !minimal) try {
            result.addNewMetadata(
                "AnyXMLType",
                "<AnyOtherMetadata><SubTag1>Text1</SubTag1><SubTag2>Text2</SubTag2></AnyOtherMetadata>"
            );
        } catch (SEDALibException ignored) {
            // no real case
        }
        return result;
    }

    /**
     * Builds a sample BinaryDataObject for a package, in its SEDA version, without adding it.
     *
     * @param dataObjectPackage the package
     * @param minimal           true for a minimal sample, false for a full one
     * @return the sample
     * @throws SEDALibException if a field sample can't be built
     */
    public static BinaryDataObject binaryDataObjectSample(DataObjectPackage dataObjectPackage, boolean minimal)
        throws SEDALibException {
        BinaryDataObject result = new BinaryDataObject();
        fillUnitaryDataObjectSample(result, dataObjectPackage, minimal);
        return result;
    }

    /**
     * Builds a sample PhysicalDataObject for a package, in its SEDA version, without adding it.
     *
     * @param dataObjectPackage the package
     * @param minimal           true for a minimal sample, false for a full one
     * @return the sample
     * @throws SEDALibException if a field sample can't be built
     */
    public static PhysicalDataObject physicalDataObjectSample(DataObjectPackage dataObjectPackage, boolean minimal)
        throws SEDALibException {
        PhysicalDataObject result = new PhysicalDataObject();
        fillUnitaryDataObjectSample(result, dataObjectPackage, minimal);
        return result;
    }

    private static void fillUnitaryDataObjectSample(
        AbstractUnitaryDataObject result,
        DataObjectPackage dataObjectPackage,
        boolean minimal
    ) throws SEDALibException {
        result.setDataObjectPackage(dataObjectPackage);
        for (Map.Entry<String, ComplexListMetadataKind> e : result.getMetadataMap().entrySet()) {
            if (MINIMAL_TAGS.contains(e.getKey())) result.addMetadata(
                sample(result.getSedaVersion(), e.getValue().getMetadataClass().getSimpleName(), e.getKey(), minimal)
            );
        }
    }
}

/**
 * Copyright French Prime minister Office/DINSIC/Vitam Program (2015-2019)
 * <p>
 * contact.vitam@programmevitam.fr
 * <p>
 * This software is developed as a validation helper tool, for constructing Submission Information Packages (archives
 * sets) in the Vitam program whose purpose is to implement a digital archiving back-office system managing high
 * volumetry securely and efficiently.
 * <p>
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA archiveTransfer the following URL "http://www.cecill.info".
 * <p>
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 * <p>
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 * <p>
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */
package fr.gouv.vitam.tools.sedalib.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.gouv.vitam.tools.sedalib.metadata.content.PersistentIdentifier;
import fr.gouv.vitam.tools.sedalib.metadata.data.PhysicalDimensions;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.*;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The Class PhysicalDataObject.
 * <p>
 * Class for SEDA element PhysicalDataObject. It contains metadata.
 */
public class PhysicalDataObject extends AbstractUnitaryDataObject implements DataObject, ComplexListInterface {

    /**
     * Init metadata map.
     */
    @ComplexListMetadataMap(isExpandable = true, seda2Version = {1})
    public static final Map<String, ComplexListMetadataKind> METADATA_MAP_V1;

    static {
        METADATA_MAP_V1 = new LinkedHashMap<>();
        METADATA_MAP_V1.put("DataObjectSystemId", new ComplexListMetadataKind(StringType.class, false));
        METADATA_MAP_V1.put("DataObjectGroupSystemId", new ComplexListMetadataKind(StringType.class, false));
        METADATA_MAP_V1.put("Relationship", new ComplexListMetadataKind(AnyXMLType.class, true));
        METADATA_MAP_V1.put("DataObjectGroupReferenceId", new ComplexListMetadataKind(StringType.class, false));
        METADATA_MAP_V1.put("DataObjectGroupId", new ComplexListMetadataKind(StringType.class, false));
        METADATA_MAP_V1.put("DataObjectVersion", new ComplexListMetadataKind(StringType.class, false));

        METADATA_MAP_V1.put("PhysicalId", new ComplexListMetadataKind(StringType.class, false));
        METADATA_MAP_V1.put("PhysicalDimensions", new ComplexListMetadataKind(PhysicalDimensions.class, false));
    }

    @ComplexListMetadataMap(isExpandable = true, seda2Version = {2})
    public static final Map<String, ComplexListMetadataKind> METADATA_MAP_V2;

    static {
        METADATA_MAP_V2 = new LinkedHashMap<>();
        METADATA_MAP_V2.put("DataObjectProfile", new ComplexListMetadataKind(StringType.class, false));
        METADATA_MAP_V2.put("DataObjectSystemId", new ComplexListMetadataKind(StringType.class, false));
        METADATA_MAP_V2.put("DataObjectGroupSystemId", new ComplexListMetadataKind(StringType.class, false));
        METADATA_MAP_V2.put("Relationship", new ComplexListMetadataKind(AnyXMLType.class, true));
        METADATA_MAP_V2.put("DataObjectVersion", new ComplexListMetadataKind(StringType.class, false));

        METADATA_MAP_V2.put("PhysicalId", new ComplexListMetadataKind(StringType.class, false));
        METADATA_MAP_V2.put("PhysicalDimensions", new ComplexListMetadataKind(PhysicalDimensions.class, false));
    }

    @ComplexListMetadataMap(isExpandable = true, seda2Version = {3})
    public static final Map<String, ComplexListMetadataKind> METADATA_MAP_V3;

    static {
        METADATA_MAP_V3 = new LinkedHashMap<>();
        METADATA_MAP_V3.put("DataObjectProfile", new ComplexListMetadataKind(StringType.class, false));
        METADATA_MAP_V3.put("DataObjectSystemId", new ComplexListMetadataKind(StringType.class, false));
        METADATA_MAP_V3.put("DataObjectGroupSystemId", new ComplexListMetadataKind(StringType.class, false));
        METADATA_MAP_V3.put("Relationship", new ComplexListMetadataKind(AnyXMLType.class, true));
        METADATA_MAP_V3.put("DataObjectVersion", new ComplexListMetadataKind(StringType.class, false));
        METADATA_MAP_V3.put("PersistentIdentifier", new ComplexListMetadataKind(PersistentIdentifier.class, true));
        METADATA_MAP_V3.put("DataObjectUse", new ComplexListMetadataKind(StringType.class, false));
        METADATA_MAP_V3.put("DataObjectNumber", new ComplexListMetadataKind(IntegerType.class, false));

        METADATA_MAP_V3.put("PhysicalId", new ComplexListMetadataKind(StringType.class, false));
        METADATA_MAP_V3.put("PhysicalDimensions", new ComplexListMetadataKind(PhysicalDimensions.class, false));
    }

    private final static LinkedHashMap<String, ComplexListMetadataKind>[] METADATA_MAPS = new LinkedHashMap[SEDA2Version.MAX_SUPPORTED_VERSION + 1];
    private final static Boolean[] NON_EXPANDABLE_FLAGS = new Boolean[SEDA2Version.MAX_SUPPORTED_VERSION + 1];

    static {
        ComplexListInterface.initMetadataMaps(PhysicalDataObject.class, METADATA_MAPS, NON_EXPANDABLE_FLAGS);
    }

    /**
     * {@inheritDoc}
     */
    @JsonIgnore
    @Override
    public LinkedHashMap<String, ComplexListMetadataKind> getMetadataMap() throws SEDALibException {
        LinkedHashMap<String, ComplexListMetadataKind> result = METADATA_MAPS[SEDA2Version.getSeda2Version()];
        if (result == null)
            result = METADATA_MAPS[0];
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @JsonIgnore
    @Override
    public boolean isNotExpandable() throws SEDALibException {
        Boolean result = NON_EXPANDABLE_FLAGS[SEDA2Version.getSeda2Version()];
        if (result == null)
            result = NON_EXPANDABLE_FLAGS[0];
        return result;
    }

    // Constructors

    /**
     * Instantiates a new PhysicalDataObject.
     */
    public PhysicalDataObject() {
        this(null);
    }

    /**
     * Instantiates a new PhysicalDataObject.
     * <p>
     * If DataObjectPackage is defined the new PhysicalDataObject is added with a generated
     * uniqID in the structure.
     *
     * @param dataObjectPackage the DataObjectPackage
     */
    public PhysicalDataObject(DataObjectPackage dataObjectPackage) {
        super(dataObjectPackage);
    }

    /**
     * Instantiates a new PhysicalDataObject.
     * <p>
     * If DataObjectPackage is defined the new PhysicalDataObject is added with a generated
     * uniqID in the structure.
     * <p>
     * Fragment sample: <code>
     * &lt;DataObjectProfile&gt;DataObject1&lt;DataObjectProfile&gt; // only in SEDA 2.2
     * &lt;DataObjectVersion&gt;PhysicalMaster_1&lt;/DataObjectVersion&gt;
     * &lt;PhysicalId&gt;940 W&lt;/PhysicalId&gt;
     * &lt;PhysicalDimensions&gt;
     * &lt;Width unit="centimetre"&gt;10&lt;/Width&gt;
     * &lt;Height unit="centimetre"&gt;8&lt;/Height&gt;
     * &lt;Depth unit="centimetre"&gt;1&lt;/Depth&gt;
     * &lt;Diameter unit="centimetre"&gt;0&lt;/Diameter&gt;
     * &lt;Weight unit="gram"&gt;59&lt;/Weight&gt;
     * &lt;/PhysicalDimensions&gt;
     * </code>
     *
     * @param dataObjectPackage the DataObjectPackage
     * @param xmlData           the raw XML content describing this PhysicalDataObject
     *                          in manifest but without DataObjectGroup ID or RefID
     *                          information
     * @throws SEDALibException if any xmlData reading exception
     */
    public PhysicalDataObject(DataObjectPackage dataObjectPackage, String xmlData) throws SEDALibException {
        this(dataObjectPackage);
        fromSedaXmlFragments(xmlData);
    }

    // SEDA XML importer

    /**
     * Import the PhysicalDataObject in XML expected form from the SEDA Manifest in
     * the ArchiveTransfer.
     *
     * @param xmlReader             the SEDAXMLEventReader reading the SEDA manifest
     * @param dataObjectPackage     the DataObjectPackage to be completed
     * @param sedaLibProgressLogger the progress logger or null if no progress log expected
     * @return the read PhysicalDataObject, or null if not a PhysicalDataObject
     * @throws SEDALibException     if the XML can't be read or the SEDA scheme is
     *                              not respected
     * @throws InterruptedException if export process is interrupted
     */
    public static PhysicalDataObject fromSedaXml(SEDAXMLEventReader xmlReader, DataObjectPackage dataObjectPackage,
                                                 SEDALibProgressLogger sedaLibProgressLogger) throws SEDALibException, InterruptedException {
        PhysicalDataObject pdo = new PhysicalDataObject();
        return (importUnitaryDataObjectPackageIdElementFromSedaXml(pdo, xmlReader, dataObjectPackage, sedaLibProgressLogger)
                ? pdo : null);
    }
}

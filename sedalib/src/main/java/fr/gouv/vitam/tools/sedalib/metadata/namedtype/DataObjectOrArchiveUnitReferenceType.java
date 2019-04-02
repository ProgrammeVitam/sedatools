package fr.gouv.vitam.tools.sedalib.metadata.namedtype;

import fr.gouv.vitam.tools.sedalib.metadata.content.DataObjectReference;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import java.util.LinkedHashMap;

/**
 * The Class AgencyType.
 * <p>
 * For abstract reference to SIP internal or external reference type SEDA metadata
 */
public class DataObjectOrArchiveUnitReferenceType extends ComplexListType {
    /**
     * Init metadata map.
     */
    @ComplexListMetadataMap
    static final public LinkedHashMap<String, ComplexListMetadataKind> metadataMap;
    static {
        metadataMap = new LinkedHashMap<String, ComplexListMetadataKind>();
        metadataMap.put("ArchiveUnitRefId", new ComplexListMetadataKind(SIPInternalIDType.class, false));
        metadataMap.put("DataObjectReference", new ComplexListMetadataKind(DataObjectReference.class, false));
        metadataMap.put("RepositoryArchiveUnitPID", new ComplexListMetadataKind(StringType.class, false));
        metadataMap.put("RepositoryObjectPID", new ComplexListMetadataKind(StringType.class, false));
        metadataMap.put("ExternalReference", new ComplexListMetadataKind(StringType.class, false));
    }

    /**
     * Instantiates a new reference to SIP internal or external reference.
     *
     * @param elementName the element name
     */
    public DataObjectOrArchiveUnitReferenceType(String elementName) {
        super(elementName);
    }

    /**
     * Instantiates a new reference to SIP internal or external reference with a DataObjectGroupID.
     *
     * @param elementName       the element name
     * @param dataObjectGroupID the data object group id
     */
    public DataObjectOrArchiveUnitReferenceType(String elementName, String dataObjectGroupID) {
        super(elementName);
        try {
            addNewMetadata("DataObjectReference", null,dataObjectGroupID);
        } catch (SEDALibException ignored) {
        }
    }

}

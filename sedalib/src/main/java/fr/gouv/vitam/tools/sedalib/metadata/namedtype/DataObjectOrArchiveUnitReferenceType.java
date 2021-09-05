package fr.gouv.vitam.tools.sedalib.metadata.namedtype;

import fr.gouv.vitam.tools.sedalib.metadata.content.DataObjectReference;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The Class AgencyType.
 * <p>
 * For abstract reference to SIP internal or external reference type SEDA metadata
 */
public class DataObjectOrArchiveUnitReferenceType extends ComplexListType {

    static final String DATAOBJECTREFERENCE="DataObjectReference";

    /**
     * Init metadata map.
     */
    @ComplexListMetadataMap
    public static final Map<String, ComplexListMetadataKind> metadataMap;

    static {
        metadataMap = new LinkedHashMap<>();
        metadataMap.put("ArchiveUnitRefId", new ComplexListMetadataKind(SIPInternalIDType.class, false));
        metadataMap.put(DATAOBJECTREFERENCE, new ComplexListMetadataKind(DataObjectReference.class, false));
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
     * @throws SEDALibException if sub elements construction is not possible (not supposed to occur)
     */
    public DataObjectOrArchiveUnitReferenceType(String elementName, String dataObjectGroupID) throws SEDALibException{
        super(elementName);
            addNewMetadata(DATAOBJECTREFERENCE, null,dataObjectGroupID);
    }

}

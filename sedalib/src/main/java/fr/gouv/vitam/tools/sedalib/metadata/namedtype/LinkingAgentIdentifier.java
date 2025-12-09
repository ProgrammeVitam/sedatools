package fr.gouv.vitam.tools.sedalib.metadata.namedtype;

import fr.gouv.vitam.tools.sedalib.core.seda.SedaVersion;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import java.util.LinkedHashMap;
import java.util.Map;

public class LinkingAgentIdentifier extends ComplexListType {
    /**
     * Init metadata map.
     */
    @ComplexListMetadataMap(sedaVersion = { SedaVersion.V2_2, SedaVersion.V2_3 })
    public static final Map<String, ComplexListMetadataKind> metadataMap;

    static {
        metadataMap = new LinkedHashMap<>();
        metadataMap.put("LinkingAgentIdentifierType", new ComplexListMetadataKind(StringType.class, false));
        metadataMap.put("LinkingAgentIdentifierValue", new ComplexListMetadataKind(StringType.class, false));
        metadataMap.put("LinkingAgentRole", new ComplexListMetadataKind(StringType.class, false));
    }

    /**
     * Instantiates a new linking agent identifier type.
     *
     * @param elementName the element name
     */
    public LinkingAgentIdentifier(String elementName) {
        super(elementName);
    }

    /**
     * Instantiates a new linking agent identifier with type, value and role.
     *
     * @param elementName                 the element name
     * @param linkingAgentIdentifierType  the linking agent identifier type
     * @param linkingAgentIdentifierValue the linking agent identifier value
     * @param linkingAgentRole            the linking agent role
     * @throws SEDALibException if sub elements construction is not possible (not supposed to occur)
     */
    public LinkingAgentIdentifier(String elementName,
                                      String linkingAgentIdentifierType,
                                      String linkingAgentIdentifierValue,
                                      String linkingAgentRole) throws SEDALibException{
        super(elementName);
        if (linkingAgentIdentifierType!=null)
            addNewMetadata("LinkingAgentIdentifierType", linkingAgentIdentifierType);
        if (linkingAgentIdentifierValue!=null)
            addNewMetadata("LinkingAgentIdentifierValue", linkingAgentIdentifierValue);
        if (linkingAgentRole!=null)
            addNewMetadata("LinkingAgentRole", linkingAgentRole);
    }
}

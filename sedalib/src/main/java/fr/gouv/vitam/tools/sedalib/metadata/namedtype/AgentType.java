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
 * circulated by CEA, CNRS and INRIA archiveDeliveryRequestReply the following URL "http://www.cecill.info".
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
package fr.gouv.vitam.tools.sedalib.metadata.namedtype;

import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The Class AgentType.
 * <p>
 * For abstract agent type SEDA metadata
 */
public class AgentType extends ComplexListType {

    static final String FIRSTNAME_TAG="FirstName";
    static final String BIRTHNAME_TAG="BirthName";
    static final String FULLNAME_TAG="FullName";
    static final String IDENTIFIER_TAG="Identifier";

    /**
     * Init metadata map.
     */
    @ComplexListMetadataMap
    static Map<String, ComplexListMetadataKind> metadataMap;

    static {
        metadataMap = new LinkedHashMap<>();
        metadataMap.put(FIRSTNAME_TAG, new ComplexListMetadataKind(StringType.class, false));
        metadataMap.put(BIRTHNAME_TAG, new ComplexListMetadataKind(StringType.class, false));
        metadataMap.put(FULLNAME_TAG, new ComplexListMetadataKind(StringType.class, false));
        metadataMap.put("GivenName", new ComplexListMetadataKind(StringType.class, false));
        metadataMap.put("Gender", new ComplexListMetadataKind(StringType.class, false));
        metadataMap.put("BirthDate", new ComplexListMetadataKind(DateType.class, false));
        metadataMap.put("BirthPlace", new ComplexListMetadataKind(PlaceType.class, false));
        metadataMap.put("DeathDate", new ComplexListMetadataKind(DateType.class, false));
        metadataMap.put("DeathPlace", new ComplexListMetadataKind(PlaceType.class, false));
        metadataMap.put("Nationality", new ComplexListMetadataKind(StringType.class, true));
        metadataMap.put("Corpname", new ComplexListMetadataKind(StringType.class, false));
        metadataMap.put(IDENTIFIER_TAG, new ComplexListMetadataKind(StringType.class, true));
        metadataMap.put("Function", new ComplexListMetadataKind(TextType.class, true));
        metadataMap.put("Activity", new ComplexListMetadataKind(TextType.class, true));
        metadataMap.put("Position", new ComplexListMetadataKind(TextType.class, true));
        metadataMap.put("Role", new ComplexListMetadataKind(TextType.class, true));
        metadataMap.put("Mandate", new ComplexListMetadataKind(TextType.class, true));
    }

    /**
     * Instantiates a new agent type.
     *
     * @param elementName the element name
     */
    public AgentType(String elementName) {
        super(elementName);
    }

    /**
     * Instantiates a new agent type with fullname.
     *
     * @param elementName the element name
     * @param fullName    the full name
     * @throws SEDALibException if sub element construction is not possible (not supposed to occur)
     */
    public AgentType(String elementName, String fullName) throws SEDALibException {
        super(elementName);

        addNewMetadata(FULLNAME_TAG, fullName);
    }

    /**
     * Instantiates a new agent type with firstname and birthname.
     *
     * @param elementName the element name
     * @param firstName   the first name
     * @param birthName   the birth name
     * @throws SEDALibException if sub elements construction is not possible (not supposed to occur)
     */
    public AgentType(String elementName, String firstName, String birthName) throws SEDALibException {
        super(elementName);

        addNewMetadata(FIRSTNAME_TAG, firstName);
        addNewMetadata(BIRTHNAME_TAG, birthName);
    }

    /**
     * Instantiates a new agent type with firstname, birthname and identifier.
     *
     * @param elementName the element name
     * @param firstName   the first name
     * @param birthName   the birth name
     * @param identifier  the identifier
     * @throws SEDALibException if sub elements construction is not possible (not supposed to occur)
     */
    public AgentType(String elementName, String firstName, String birthName, String identifier) throws SEDALibException {
        super(elementName);

        addNewMetadata(FIRSTNAME_TAG, firstName);
        addNewMetadata(BIRTHNAME_TAG, birthName);
        addNewMetadata(IDENTIFIER_TAG, identifier);
    }
}

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
package fr.gouv.vitam.tools.sedalib.metadata.content;

import fr.gouv.vitam.tools.sedalib.core.seda.SedaVersion;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.ComplexListMetadataKind;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.ComplexListMetadataMap;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.ComplexListType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.StringType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class PersistentIdentifier.
 * <p>
 * Class for SEDA element PersistentIdentifier.
 * <p>
 * A BinaryDataObject and ArchiveUnit metadata.
 * <p>
 * Standard quote: "Customize Toolbar…"
 */
public class PersistentIdentifier extends ComplexListType {

    /**
     * Init metadata map.
     */
    @ComplexListMetadataMap (sedaVersion = { SedaVersion.V2_3 })
    public static final Map<String, ComplexListMetadataKind> metadataMap;

    static {
        metadataMap = new LinkedHashMap<>();
        metadataMap.put("PersistentIdentifierType", new ComplexListMetadataKind(StringType.class, false));
        metadataMap.put("PersistentIdentifierOrigin", new ComplexListMetadataKind(StringType.class, false));
        metadataMap.put("PersistentIdentifierReference", new ComplexListMetadataKind(StringType.class, false));
        metadataMap.put("PersistentIdentifierContent", new ComplexListMetadataKind(StringType.class, false));
    }

    // Constructors

    /**
     * Instantiates a new persistent identifier.
     */
    public PersistentIdentifier() {
        super("PersistentIdentifier");
    }

    /**
     * Creates a new instance of PersistentIdentifier.
     *
     * @param persistentIdentifierType      the persistent identifier type
     * @param persistentIdentifierOrigin    the persistent identifier origin
     * @param persistentIdentifierReference the persistent identifier reference
     * @param persistentIdentifierContent   the persistent identifier content
     * @throws SEDALibException if sub-elements construction fails (not expected to occur)
     */
    public PersistentIdentifier(String persistentIdentifierType,
                                String persistentIdentifierOrigin,
                                String persistentIdentifierReference,
                                String persistentIdentifierContent) throws SEDALibException {
        super("PersistentIdentifier");
        if (persistentIdentifierType != null) {
            addNewMetadata("PersistentIdentifierType", persistentIdentifierType);
        }
        if (persistentIdentifierOrigin != null) {
            addNewMetadata("PersistentIdentifierOrigin", persistentIdentifierOrigin);
        }
        if (persistentIdentifierReference != null) {
            addNewMetadata("PersistentIdentifierReference", persistentIdentifierReference);
        }
        if (persistentIdentifierContent != null) {
            addNewMetadata("PersistentIdentifierContent", persistentIdentifierContent);
        }
    }

    /**
     * Generates a summary from the PersistentIdentifier metadata.
     * <p>
     * The summary includes values for "PersistentIdentifierType" or "PersistentIdentifierOrigin"
     * (if "PersistentIdentifierType" is not available) and "PersistentIdentifierContent",
     * joined with " : " as a delimiter.
     *
     * @return a summary string constructed from the metadata
     */
    public String getSummary() {
        List<String> summaryList = new ArrayList<>(2);
        if (getFirstNamedMetadata("PersistentIdentifierType") != null)
            summaryList.add(((StringType) getFirstNamedMetadata("PersistentIdentifierType")).getValue());
        else if (getFirstNamedMetadata("PersistentIdentifierOrigin") != null)
            summaryList.add(((StringType) getFirstNamedMetadata("PersistentIdentifierOrigin")).getValue());
        else if (getFirstNamedMetadata("PersistentIdentifierReference") != null)
            summaryList.add(((StringType) getFirstNamedMetadata("PersistentIdentifierReference")).getValue());
        if (getFirstNamedMetadata("PersistentIdentifierContent") != null)
            summaryList.add(((StringType) getFirstNamedMetadata("PersistentIdentifierContent")).getValue());

        return String.join(" : ", summaryList);
    }
}

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
package fr.gouv.vitam.tools.sedalib.metadata.management;

import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.content.Event;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.*;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import java.util.LinkedHashMap;

/**
 * The Class Management.
 * <p>
 * Class for SEDA element Management.
 * <p>
 * A ArchiveUnit metadata.
 * <p>
 * Standard quote: "Métadonnées de gestion applicables à l’ArchiveUnit concernée
 * et à ses héritiers"
 */
public class Management extends ComplexListType {

    /**
     * Init metadata map.
     */
    @ComplexListMetadataMap(isExpandable = true)
    static final public LinkedHashMap<String, ComplexListMetadataKind> metadataMap;
    static {
        metadataMap = new LinkedHashMap<String, ComplexListMetadataKind>();
        metadataMap.put("StorageRule", new ComplexListMetadataKind(StorageRule.class, false));
        metadataMap.put("AppraisalRule", new ComplexListMetadataKind(AppraisalRule.class, false));
        metadataMap.put("AccessRule", new ComplexListMetadataKind(AccessRule.class, false));
        metadataMap.put("DisseminationRule", new ComplexListMetadataKind(DisseminationRule.class, false));
        metadataMap.put("ReuseRule", new ComplexListMetadataKind(ReuseRule.class, false));
        metadataMap.put("ClassificationRule",
                new ComplexListMetadataKind(ClassificationRule.class, false));
        metadataMap.put("HoldRule", new ComplexListMetadataKind(HoldRule.class, false));
        metadataMap.put("LogBook",
                new ComplexListMetadataKind(LogBook.class, false));
        metadataMap.put("NeedAuthorization",
                new ComplexListMetadataKind(StringType.class, false));
        // Vitam extension
        metadataMap.put("UpdateOperation",
                new ComplexListMetadataKind(UpdateOperation.class, false));
    }

    /**
     * Instantiates a new Management.
     */
    public Management() {
        super("Management");
    }

    /**
     * Export the Management metadata to csv List for the csv metadata file.
     * <p>
     * In the HashMap result, the key is a metadata path of a leaf and the value is the leaf of the metadata value.
     *
     * @return the linked hash map with header title as key and metadata value as value
     * @throws SEDALibException the seda lib exception
     */
    public LinkedHashMap<String, String> externToCsvList() throws SEDALibException {
        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
        String previousXMLElementName = null;
        int count = 0;
        for (SEDAMetadata sm : metadataList) {
            if (!sm.getXmlElementName().equals(previousXMLElementName)) {
                previousXMLElementName = sm.getXmlElementName();
                count = 0;
            } else count++;
            final String addedName;
            if (isAMultiValuedMetadata(sm.getXmlElementName()))
                addedName = sm.getXmlElementName() + "." + count;
            else
                addedName = sm.getXmlElementName();
            LinkedHashMap<String, String> smCsvList = sm.toCsvList();
            smCsvList.entrySet().stream().forEach(e -> {
                result.put("Management."+addedName + (e.getKey().isEmpty() ? "" : "." + e.getKey()), e.getValue());
            });
        }
        return result;
    }

}

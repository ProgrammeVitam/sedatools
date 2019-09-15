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

import fr.gouv.vitam.tools.sedalib.metadata.content.Event;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.ComplexListMetadataKind;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.ComplexListMetadataMap;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.ComplexListType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.StringType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import java.util.LinkedHashMap;

/**
 * The Class LogBook.
 * <p>
 * Class for SEDA element LogBook.
 * <p>
 * An ArchiveUnit metadata.
 * <p>
 * Standard quote: "Gestion des traces."
 */
public class LogBook extends ComplexListType {

    /**
     * Init metadata map.
     */
    @ComplexListMetadataMap
    static final public LinkedHashMap<String, ComplexListMetadataKind> metadataMap;
    static {
        metadataMap = new LinkedHashMap<String, ComplexListMetadataKind>();
        metadataMap.put("Event", new ComplexListMetadataKind(Event.class, true));
    }

    /**
     * Instantiates a new custodial history.
     */
    public LogBook() {
        super("LogBook");
    }

    /**
     * Instantiates a new logbook type from args, each is an event item.
     *
     * @param args        the generic args for NameTypeMetadata construction
     * @throws SEDALibException if args are not suitable for constructor
     */
    public LogBook(Object... args) throws SEDALibException {
        this();
        for (int i=0;i<args.length;i++){
            if (!(args[i] instanceof String))
                throw new SEDALibException("Mauvais arguments pour le constructeur de l'élément [" + elementName + "]");
            addNewMetadata("Event",args[i]);
        }
    }
}

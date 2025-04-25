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
package fr.gouv.vitam.tools.sedalib.metadata.content;

import fr.gouv.vitam.tools.sedalib.metadata.namedtype.*;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The Class Event.
 * <p>
 * Class for SEDA element Event.
 * <p>
 * An Event metadata.
 * <p>
 * Standard quote: "Informations décrivant un événement survenu au cours d’une
 * procédure (ex. publication d’un marché, notification d’un marché, recueil
 * d’un avis administratif, etc.)."
 */
public class Event extends ComplexListType {

    /**
     * Init metadata map.
     */
    @ComplexListMetadataMap(isExpandable = true, seda2Version = {1})
    public static final Map<String, ComplexListMetadataKind> metadataMap_default;

    static {
        metadataMap_default = new LinkedHashMap<>();
        metadataMap_default.put("EventIdentifier",
                new ComplexListMetadataKind(StringType.class, false));
        metadataMap_default.put("EventTypeCode", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_default.put("EventType", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_default.put("EventDateTime", new ComplexListMetadataKind(DateTimeType.class, false));
        metadataMap_default.put("EventDetail", new ComplexListMetadataKind(StringType.class, true));
        metadataMap_default.put("Outcome", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_default.put("OutcomeDetail", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_default.put("OutcomeDetailMessage",
                new ComplexListMetadataKind(StringType.class, false));
        metadataMap_default.put("EventDetailData",
                new ComplexListMetadataKind(StringType.class, false));
    }

    @ComplexListMetadataMap(isExpandable = true, seda2Version = {2,3})
    public static final Map<String, ComplexListMetadataKind> metadataMap_v2;

    static {
        metadataMap_v2 = new LinkedHashMap<>();
        metadataMap_v2.put("EventIdentifier",
                new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v2.put("EventTypeCode", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v2.put("EventType", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v2.put("EventDateTime", new ComplexListMetadataKind(DateTimeType.class, false));
        metadataMap_v2.put("EventDetail", new ComplexListMetadataKind(StringType.class, true));
        metadataMap_v2.put("Outcome", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v2.put("OutcomeDetail", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v2.put("OutcomeDetailMessage",
                new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v2.put("EventDetailData",
                new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v2.put("LinkingAgentIdentifier",
                new ComplexListMetadataKind(LinkingAgentIdentifierType.class, true));
    }

    /**
     * Instantiates a new Event.
     */
    public Event() {
        super("Event");
    }

    /**
     * Instantiates a new Event, with EventIdentifier, EventType, EventDateTime and Outcome.
     * If any is null, it's not added.
     *
     * @param eventIdentifier the event identifier
     * @param eventType       the event type
     * @param eventDateTime   the event date time
     * @param outcome         the outcome
     * @throws SEDALibException if sub elements construction is not possible (not supposed to occur)
     */
    public Event(String eventIdentifier, String eventType, LocalDateTime eventDateTime, String outcome) throws SEDALibException {
        super("Event");

        if (eventIdentifier != null) addNewMetadata("EventIdentifier", eventIdentifier);
        if (eventType != null) addNewMetadata("EventType", eventType);
        if (eventDateTime != null) addNewMetadata("EventDateTime", eventDateTime);
        if (outcome != null) addNewMetadata("Outcome", outcome);
    }
}

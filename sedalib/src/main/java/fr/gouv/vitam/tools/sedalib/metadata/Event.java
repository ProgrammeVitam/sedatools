/**
 * Copyright French Prime minister Office/DINSIC/Vitam Program (2015-2019)
 *
 * contact.vitam@programmevitam.fr
 * 
 * This software is developed as a validation helper tool, for constructing Submission Information Packages (archives 
 * sets) in the Vitam program whose purpose is to implement a digital archiving back-office system managing high 
 * volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA archiveDeliveryRequestReply the following URL "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */
package fr.gouv.vitam.tools.sedalib.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.gouv.vitam.tools.sedalib.metadata.namedtype.ComplexListType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.DateTimeType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.StringType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.TextType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;

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

	{
		metadataOrderedList = new ArrayList<String>();
		metadataOrderedList.add("EventIdentifier");
		metadataOrderedList.add("EventTypeCode");
		metadataOrderedList.add("EventType");
		metadataOrderedList.add("EventDateTime");
		metadataOrderedList.add("EventDetail");
		metadataOrderedList.add("Outcome");
		metadataOrderedList.add("OutcomeDetail");
		metadataOrderedList.add("OutcomeDetailMessage");
		metadataOrderedList.add("EventDetailData");

		metadataMap = new HashMap<String, ComplexListType.MetadataKind>();
		metadataMap.put("EventIdentifier",
				new ComplexListType.MetadataKind(StringType.class, false));
		metadataMap.put("EventTypeCode", new ComplexListType.MetadataKind(StringType.class, false));
		metadataMap.put("EventType", new ComplexListType.MetadataKind(StringType.class, false));
		metadataMap.put("EventDateTime", new ComplexListType.MetadataKind(DateTimeType.class, false));
		metadataMap.put("EventDetail", new ComplexListType.MetadataKind(TextType.class, true));
		metadataMap.put("Outcome", new ComplexListType.MetadataKind(StringType.class, false));
		metadataMap.put("OutcomeDetail", new ComplexListType.MetadataKind(StringType.class, false));
		metadataMap.put("OutcomeDetailMessage",
				new ComplexListType.MetadataKind(StringType.class, false));
		metadataMap.put("EventDetailData",
				new ComplexListType.MetadataKind(StringType.class, false));
	}

	/** The metadata ordered list. */
	public static List<String> metadataOrderedList;

	/** The metadata map. */
	public static HashMap<String, MetadataKind> metadataMap;

	@Override
	public List<String> getMetadataOrderedList() {
		return metadataOrderedList;
	}

	@Override
	public HashMap<String, MetadataKind> getMetadataMap() {
		return metadataMap;
	}

	@Override
	public boolean isNotExpendable() {
		return true;
	}

	/**
	 * Instantiates a new Content.
	 */
	public Event() {
		super("Event");
	}

	/**
	 * Import the Event in XML expected form for the SEDA Manifest.
	 *
	 * @param xmlReader the SEDAXMLEventReader reading the SEDA manifest
	 * @return the read Event
	 * @throws SEDALibException if the XML can't be read or the SEDA scheme is not
	 *                          respected
	 */
	public static Event fromSedaXml(SEDAXMLEventReader xmlReader) throws SEDALibException {
		Event event = new Event();
		event = (Event) fromSedaXmlInObject(xmlReader, event);
		return event;
	}
}

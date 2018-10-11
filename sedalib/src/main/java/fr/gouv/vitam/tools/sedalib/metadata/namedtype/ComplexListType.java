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
package fr.gouv.vitam.tools.sedalib.metadata.namedtype;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLStreamWriter;

/**
 * The Class ComplexListType.
 * <p>
 * For abstract SEDA metadata composed with list of other metadata or types
 */
public abstract class ComplexListType extends NamedTypeMetadata {

	/**
	 * The Class MetadataItem.
	 */
	protected class MetadataKind {

		/** The c. */
		public Class<?> metadataClass;

		/** The many. */
		public boolean many;

		/**
		 * Instantiates a new metadata item.
		 *
		 * @param metadataClass the metadata class
		 * @param many          the many
		 */
		public MetadataKind(Class<?> metadataClass, boolean many) {
			this.metadataClass = metadataClass;
			this.many = many;
		}
	}

	/** The metadata ordered list. */
	static protected List<String> metadataOrderedList;

	/** The metadata map. */
	static protected HashMap<String, MetadataKind> metadataMap;

	/** The metadata list. */
	public List<SEDAMetadata> metadataList;

	/**
	 * Instantiates a new management.
	 *
	 * @param elementName the element name
	 */
	public ComplexListType(String elementName) {
		super(elementName);
		this.metadataList = new ArrayList<SEDAMetadata>();
	}

	/**
	 * New SEDA metadata.
	 *
	 * @param elementName the element name
	 * @param args        the args
	 * @return the SEDA metadata
	 * @throws SEDALibException if failed in construction
	 */
	private SEDAMetadata newSEDAMetadata(String elementName, Object[] args) throws SEDALibException {
		MetadataKind mi = getMetadataMap().get(elementName);
		Constructor<?> constructor;
		try {
			if (mi == null)
				constructor = GenericXMLBlockType.class.getConstructor(String.class, Object[].class);
			else
				constructor = mi.metadataClass.getConstructor(String.class, Object[].class);
			return (SEDAMetadata) constructor.newInstance(elementName, args);
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			throw new SEDALibException("Impossible de construire l'élément [" + elementName + "]");
		}
	}

	/**
	 * Adds a new metadata, or replace it if it exists and the metadata can't have
	 * many values
	 *
	 * @param elementName the element name
	 * @param args        the args of the metadata constructor
	 * @throws SEDALibException if construction is not possible, most of the time
	 *                          wrong args
	 */
	public void addNewMetadata(String elementName, Object... args) throws SEDALibException {
		int addOrderIndex, curOrderIndex, i;
		boolean manyFlag, setFlag;
		if ((args.length > 0) && (args[0] != null)) {
			addOrderIndex = getMetadataOrderedList().indexOf(elementName);
			i = 0;
			setFlag = false;
			if (addOrderIndex == -1) {
				if (isNotExpendable())
					throw new SEDALibException(
							"Il n'est pas possible d'étendre le schéma avec des métadonnées non prévues ["
									+ elementName + "]");
				manyFlag = true;
				boolean noBeforeEqual = true;
				for (SEDAMetadata sm : metadataList) {
					if ((sm.getXmlElementName().equals(elementName)) && noBeforeEqual)
						noBeforeEqual = false;
					if (!(sm.getXmlElementName().equals(elementName)) && !noBeforeEqual)
						break;
					i++;
				}
			} else {
				manyFlag = getMetadataMap().get(elementName).many;
				for (SEDAMetadata sm : metadataList) {
					curOrderIndex = getMetadataOrderedList().indexOf(sm.getXmlElementName());
					if ((!manyFlag) && (curOrderIndex == addOrderIndex)) {
						setFlag = true;
						break;
					}
					if ((curOrderIndex == -1) || (curOrderIndex > addOrderIndex))
						break;
					i++;
				}
			}
			if (manyFlag)
				metadataList.add(i, newSEDAMetadata(elementName, args));
			else {
				if (setFlag)
					metadataList.set(i, newSEDAMetadata(elementName, args));
				else
					metadataList.add(i, newSEDAMetadata(elementName, args));
			}
		}
	}

	/**
	 * Adds a metadata, or replace it if it exists and the metadata can't have many
	 * values.
	 *
	 * @param sedaMetadata the named type metadata
	 * @throws SEDALibException if try to add an unknown metadata in a not
	 *                          expandable type
	 */
	public void addMetadata(SEDAMetadata sedaMetadata) throws SEDALibException {
		int addOrderIndex, curOrderIndex, i;
		boolean manyFlag, setFlag;
		addOrderIndex = getMetadataOrderedList().indexOf(sedaMetadata.getXmlElementName());
		i = 0;
		setFlag = false;
		if (addOrderIndex == -1) {
			if (isNotExpendable())
				throw new SEDALibException(
						"Il n'est pas possible d'étendre le schéma avec des métadonnées non prévues ["
								+ elementName + "]");
			manyFlag = true;
			boolean noBeforeEqual = true;
			for (SEDAMetadata sm : metadataList) {
				if ((sm.getXmlElementName().equals(sedaMetadata.getXmlElementName())) && noBeforeEqual)
					noBeforeEqual = false;
				if (!(sm.getXmlElementName().equals(sedaMetadata.getXmlElementName())) && !noBeforeEqual)
					break;
				i++;
			}
		} else {
			manyFlag = getMetadataMap().get(sedaMetadata.getXmlElementName()).many;
			for (SEDAMetadata sm : metadataList) {
				curOrderIndex = getMetadataOrderedList().indexOf(sm.getXmlElementName());
				if ((!manyFlag) && (curOrderIndex == addOrderIndex)) {
					setFlag = true;
					break;
				}
				if ((curOrderIndex == -1) || (curOrderIndex > addOrderIndex))
					break;
				i++;
			}
		}
		if (manyFlag)
			metadataList.add(i, sedaMetadata);
		else {
			if (setFlag)
				metadataList.set(i, sedaMetadata);
			else
				metadataList.add(i, sedaMetadata);
		}
	}

	/**
	 * Checks if metadata is lacking.
	 *
	 * @param elementName the element name
	 * @return true, if metadata is lacking
	 */
	public boolean isMetadataLacking(String elementName) {
		for (SEDAMetadata sm : metadataList) {
			if (sm.getXmlElementName().equals(elementName))
				return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata#toSedaXml(fr.gouv.vitam.
	 * tools.sedalib.xml.SEDAXMLStreamWriter)
	 */
	public void toSedaXml(SEDAXMLStreamWriter xmlWriter) throws SEDALibException {
		try {
			xmlWriter.writeStartElement(elementName);
			for (SEDAMetadata sm : metadataList) {
				sm.toSedaXml(xmlWriter);
			}
			xmlWriter.writeEndElement();
		} catch (XMLStreamException e) {
			throw new SEDALibException("Erreur d'écriture XML dans un élément Content\n->" + e.getMessage());
		}
	}

	/**
	 * Import the Content in XML expected form for the SEDA Manifest.
	 *
	 * @param xmlReader       the SEDAXMLEventReader reading the SEDA manifest
	 * @param complexListType the ComplexListType to complete
	 * @return the complex list type completed or null of not convenient element
	 *         name
	 * @throws SEDALibException if the XML can't be read or the SEDA scheme is not
	 *                          respected, for example
	 */
	public static ComplexListType fromSedaXmlInObject(SEDAXMLEventReader xmlReader, ComplexListType complexListType)
			throws SEDALibException {
		Class<?> metadataClass;
		try {
			if (xmlReader.nextBlockIfNamed(complexListType.elementName)) {
				xmlReader.peekUsefullEvent();
				String tmp = xmlReader.peekName();
				while (tmp != null) {
					MetadataKind mi = complexListType.getMetadataMap().get(tmp);
					if (mi == null) {
						if (complexListType.isNotExpendable())
							throw new SEDALibException(
									"Il n'est pas possible d'étendre le schéma avec des métadonnées non prévues ["
											+ complexListType.elementName + "]");
						else
							metadataClass = GenericXMLBlockType.class;
					} else
						metadataClass = mi.metadataClass;
					SEDAMetadata sm = SEDAMetadata.fromSedaXml(xmlReader, metadataClass);
					complexListType.addMetadata(sm);
					tmp = xmlReader.peekName();
				}
				xmlReader.endBlockNamed(complexListType.elementName);
			} else
				return null;
		} catch (XMLStreamException | IllegalArgumentException | SEDALibException e) {
			throw new SEDALibException("Erreur de lecture XML dans un élément Content\n->" + e.getMessage());
		}
		return complexListType;
	}

	/**
	 * Add metadata from fragments in XML expected form for the SEDA Manifest.
	 *
	 * @param xmlData the xml data
	 * @throws SEDALibException if the XML can't be read or the SEDA scheme is not
	 *                          respected
	 */
	public void addSedaXmlFragments(String xmlData) throws SEDALibException {
		Class<?> metadataClass;
		try (ByteArrayInputStream bais = new ByteArrayInputStream(xmlData.getBytes("UTF-8"));
				SEDAXMLEventReader xmlReader = new SEDAXMLEventReader(bais, true)) {
			// jump document start
			xmlReader.nextUsefullEvent();
			String tmp = xmlReader.peekName();
			while (tmp != null) {
				MetadataKind mi = getMetadataMap().get(tmp);
				if (mi == null) {
					if (isNotExpendable())
						throw new SEDALibException(
								"Il n'est pas possible d'étendre le schéma avec des métadonnées non prévues ["
										+ elementName + "]");
					else
						metadataClass = GenericXMLBlockType.class;
				} else
					metadataClass = mi.metadataClass;
				SEDAMetadata sm = SEDAMetadata.fromSedaXml(xmlReader, metadataClass);
				addMetadata(sm);
				tmp = xmlReader.peekName();
			}
		} catch (XMLStreamException | IllegalArgumentException | SEDALibException | IOException e) {
			throw new SEDALibException("Erreur de lecture XML de fragments d'un élément Content\n->" + e.getMessage());
		}
	}

	/**
	 * Gets the metadata ordered list.
	 *
	 * @return the metadata ordered list
	 */
	public abstract List<String> getMetadataOrderedList();

	/**
	 * Gets the metadata map, which link xml element name with metadata class and
	 * cardinality.
	 *
	 * @return the metadata map
	 */
	public abstract HashMap<String, MetadataKind> getMetadataMap();

	/**
	 * Checks if it the metadata list is closed.
	 *
	 * @return true, if is not expendable
	 */
	public boolean isNotExpendable() {
		return false;
	}

}

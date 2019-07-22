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
package fr.gouv.vitam.tools.sedalib.core;

import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLStreamWriter;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * The Class GlobalMetadata.
 * <p>
 * Class for the global metadata read from or used to generate a SEDA SIP
 * Manifest (MessageIdentifier, Comment...). These metadata are either in the
 * header before the DataObjectPackage or at the end of the ArchiveTransfer.
 */
public class GlobalMetadata {

    // SEDA elements
    // Before DataObjectPackage

    /**
     * The comment.
     */
    public String comment;

    /**
     * The date.
     */
    public String date;

    /**
     * The now flag.
     */
    public boolean nowFlag;

    /**
     * The message identifier.
     */
    public String messageIdentifier;

    // Signature
    // - signature of xml content not supported

    /**
     * The archival agreement.
     */
    public String archivalAgreement;

    /**
     * The code list versions xml data.
     */
    public String codeListVersionsXmlData;

    // After DataObjectPackage

    // RelatedTransferReference
    // - reference to other transfers not supported

    /**
     * The transfer request reply identifier.
     */
    public String transferRequestReplyIdentifier;

    /**
     * The archival agency identifier.
     */
    public String archivalAgencyIdentifier;

    /**
     * The archival agency organization descriptive metadata.
     */
    public String archivalAgencyOrganizationDescriptiveMetadataXmlData;

    /**
     * The transferring agency identifier.
     */
    public String transferringAgencyIdentifier;

    /**
     * The transferring agency organization descriptive metadata.
     */
    public String transferringAgencyOrganizationDescriptiveMetadataXmlData;

    // Constructors

    /**
     * Instantiates a new global metadata.
     */
    public GlobalMetadata() {
        this.comment = null;
        this.date = null;
        this.nowFlag = true;
        this.messageIdentifier = null;
        this.archivalAgreement = null;
        this.codeListVersionsXmlData = null;
        this.transferRequestReplyIdentifier = null;
        this.archivalAgencyIdentifier = null;
        this.archivalAgencyOrganizationDescriptiveMetadataXmlData = null;
        this.transferringAgencyIdentifier = null;
        this.transferringAgencyOrganizationDescriptiveMetadataXmlData = null;
    }

    // Methods

    // SEDA XML exporter

    /**
     * Export the elements that can be edited. This is in XML expected form for the
     * SEDA Manifest but in String.
     *
     * @return the XML elements in String format
     * @throws SEDALibException if the XML can't be written
     */
    public String toSedaXmlFragments() throws SEDALibException {
        String result;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             SEDAXMLStreamWriter xmlWriter = new SEDAXMLStreamWriter(baos, 2, true)) {
            xmlWriter.writeElementValueIfNotEmpty("Comment", comment);
            if (!isNowFlag())
                xmlWriter.writeElementValueIfNotEmpty("Date", date);
            xmlWriter.writeElementValueIfNotEmpty("MessageIdentifier", messageIdentifier);
            xmlWriter.writeElementValueIfNotEmpty("ArchivalAgreement", archivalAgreement);
            xmlWriter.writeRawXMLBlockIfNotEmpty(codeListVersionsXmlData);
            xmlWriter.writeElementValueIfNotEmpty("TransferRequestReplyIdentifier", transferRequestReplyIdentifier);
            xmlWriter.writeStartElement("ArchivalAgency");
            xmlWriter.writeElementValue("Identifier", archivalAgencyIdentifier);
            xmlWriter.writeRawXMLBlockIfNotEmpty(archivalAgencyOrganizationDescriptiveMetadataXmlData);
            xmlWriter.writeEndElement();
            xmlWriter.writeStartElement("TransferringAgency");
            xmlWriter.writeElementValue("Identifier", transferringAgencyIdentifier);
            xmlWriter.writeRawXMLBlockIfNotEmpty(transferringAgencyOrganizationDescriptiveMetadataXmlData);
            xmlWriter.writeEndElement();
            xmlWriter.close();
            result = baos.toString("UTF-8");
        } catch (SEDALibException | XMLStreamException | IOException e) {
            throw new SEDALibException("Erreur interne", e);
        }
        return result;
    }

    // SEDA XML importer

    /**
     * Read the GlobalMetadata element content in XML expected form from the SEDA
     * Manifest in the ArchiveTransfer. Utility methods for fromSedaXml and
     * fromSedaXmlFragments
     *
     * @param xmlReader the SEDAXMLEventReader reading the SEDA manifest
     * @throws SEDALibException if the XML can't be read or the SEDA scheme is not
     *                          respected
     */
    private void setFromXmlContent(SEDAXMLEventReader xmlReader)
            throws SEDALibException {
        try {
            comment = xmlReader.nextValueIfNamed("Comment");
            date = xmlReader.nextValueIfNamed("Date");
            nowFlag = (date == null);
            messageIdentifier = xmlReader.nextValueIfNamed("MessageIdentifier");
            archivalAgreement = xmlReader.nextValueIfNamed("ArchivalAgreement");
            codeListVersionsXmlData = xmlReader.nextBlockAsStringIfNamed("CodeListVersions");
            transferRequestReplyIdentifier = xmlReader.nextValueIfNamed("TransferRequestReplyIdentifier");
            if (xmlReader.nextBlockIfNamed("ArchivalAgency")) {
                archivalAgencyIdentifier = xmlReader.nextValueIfNamed("Identifier");
                archivalAgencyOrganizationDescriptiveMetadataXmlData = xmlReader
                        .nextBlockAsStringIfNamed("OrganizationDescriptiveMetadata");
                xmlReader.endBlockNamed("ArchivalAgency");
            }
            if (xmlReader.nextBlockIfNamed("TransferringAgency")) {
                transferringAgencyIdentifier = xmlReader.nextValueIfNamed("Identifier");
                transferringAgencyOrganizationDescriptiveMetadataXmlData = xmlReader
                        .nextBlockAsStringIfNamed("OrganizationDescriptiveMetadata");
                xmlReader.endBlockNamed("TransferringAgency");
            }
        } catch (XMLStreamException e) {
            throw new SEDALibException("Erreur de lecture XML", e);
        }
    }

    /**
     * Import the elements that can be edited. This
     * is in XML expected form for the SEDA Manifest but in String.
     *
     * @param fragments the XML elements in String format
     * @throws SEDALibException if the XML can't be read or the SEDA scheme is not
     *                          respected
     */
    public void fromSedaXmlFragments(String fragments) throws SEDALibException {
        GlobalMetadata gm = new GlobalMetadata();

        try (ByteArrayInputStream bais = new ByteArrayInputStream(fragments.getBytes("UTF-8"));
             SEDAXMLEventReader xmlReader = new SEDAXMLEventReader(bais, true)) {
            // jump StartDocument
            xmlReader.nextUsefullEvent();
            gm.setFromXmlContent(xmlReader);
            XMLEvent event = xmlReader.xmlReader.peek();
            if (!event.isEndDocument())
                throw new SEDALibException("Il y a des champs illégaux");
        } catch (XMLStreamException | SEDALibException | IOException e) {
            throw new SEDALibException("Erreur de lecture du GlobalMetadata", e);
        }

        this.comment = gm.comment;
        this.date = gm.date;
        this.nowFlag = gm.nowFlag;
        this.messageIdentifier = gm.messageIdentifier;
        this.archivalAgreement = gm.archivalAgreement;
        this.codeListVersionsXmlData = gm.codeListVersionsXmlData;
        this.transferRequestReplyIdentifier = gm.transferRequestReplyIdentifier;
        this.archivalAgencyIdentifier = gm.archivalAgencyIdentifier;
        this.archivalAgencyOrganizationDescriptiveMetadataXmlData = gm.archivalAgencyOrganizationDescriptiveMetadataXmlData;
        this.transferringAgencyIdentifier = gm.transferringAgencyIdentifier;
        this.transferringAgencyOrganizationDescriptiveMetadataXmlData = gm.transferringAgencyOrganizationDescriptiveMetadataXmlData;
    }

    // Getters and setters

//	/**
//	 * Gets the message identifier.
//	 *
//	 * @return the message identifier
//	 */
//	public String getMessageIdentifier() {
//		return messageIdentifier;
//	}
//
//	/**
//	 * Sets the message identifier.
//	 *
//	 * @param messageIdentifier the new message identifier
//	 */
//	public void setMessageIdentifier(String messageIdentifier) {
//		this.messageIdentifier = messageIdentifier;
//	}
//
//	/**
//	 * Gets the comment.
//	 *
//	 * @return the comment
//	 */
//	public String getComment() {
//		return comment;
//	}
//
//	/**
//	 * Sets the comment.
//	 *
//	 * @param comment the new comment
//	 */
//	public void setComment(String comment) {
//		this.comment = comment;
//	}
//
//	/**
//	 * Gets the date.
//	 *
//	 * @return the date
//	 */
//	public String getDate() {
//		return date;
//	}
//
//	/**
//	 * Sets the date.
//	 *
//	 * @param date the new date
//	 */
//	public void setDate(String date) {
//		this.date = date;
//	}
//
//	/**
//	 * Gets the archival agreement.
//	 *
//	 * @return the archival agreement
//	 */
//	public String getArchivalAgreement() {
//		return archivalAgreement;
//	}
//
//	/**
//	 * Sets the archival agreement.
//	 *
//	 * @param archivalAgreement the new archival agreement
//	 */
//	public void setArchivalAgreement(String archivalAgreement) {
//		this.archivalAgreement = archivalAgreement;
//	}
//
//	/**
//	 * Gets the code list versions xml data.
//	 *
//	 * @return the code list versions xml data
//	 */
//	public String getCodeListVersionsXmlData() {
//		return codeListVersionsXmlData;
//	}
//
//	/**
//	 * Sets the code list versions xml data.
//	 *
//	 * @param codeListVersionsXmlString the new code list versions xml data
//	 */
//	public void setCodeListVersionsXmlData(String codeListVersionsXmlString) {
//		this.codeListVersionsXmlData = codeListVersionsXmlString;
//	}
//
//	/**
//	 * Gets the transfer request reply identifier.
//	 *
//	 * @return the transfer request reply identifier
//	 */
//	public String getTransferRequestReplyIdentifier() {
//		return transferRequestReplyIdentifier;
//	}
//
//	/**
//	 * Sets the transfer request reply identifier.
//	 *
//	 * @param transferRequestReplyIdentifier the new transfer request reply
//	 *                                       identifier
//	 */
//	public void setTransferRequestReplyIdentifier(String transferRequestReplyIdentifier) {
//		this.transferRequestReplyIdentifier = transferRequestReplyIdentifier;
//	}
//
//	/**
//	 * Gets the archival agency identifier.
//	 *
//	 * @return the archival agency identifier
//	 */
//	public String getArchivalAgencyIdentifier() {
//		return archivalAgencyIdentifier;
//	}
//
//	/**
//	 * Sets the archival agency identifier.
//	 *
//	 * @param archivalAgencyIdentifier the new archival agency identifier
//	 */
//	public void setArchivalAgencyIdentifier(String archivalAgencyIdentifier) {
//		this.archivalAgencyIdentifier = archivalAgencyIdentifier;
//	}
//
//	/**
//	 * Gets the archival agency organization descriptive metadata.
//	 *
//	 * @return the archival agency organization descriptive metadata
//	 */
//	public String getArchivalAgencyOrganizationDescriptiveMetadataXmlData() {
//		return archivalAgencyOrganizationDescriptiveMetadataXmlData;
//	}
//
//	/**
//	 * Sets the archival agency organization descriptive metadata.
//	 *
//	 * @param archivalAgencyOrganizationDescriptiveMetadataXmlData the new archival agency
//	 *                                                      organization descriptive
//	 *                                                      metadata
//	 */
//	public void setArchivalAgencyOrganizationDescriptiveMetadataXmlData(String archivalAgencyOrganizationDescriptiveMetadata) {
//		this.archivalAgencyOrganizationDescriptiveMetadataXmlData = archivalAgencyOrganizationDescriptiveMetadata;
//	}
//
//	/**
//	 * Gets the transferring agency identifier.
//	 *
//	 * @return the transferring agency identifier
//	 */
//	public String getTransferringAgencyIdentifier() {
//		return transferringAgencyIdentifier;
//	}
//
//	/**
//	 * Sets the transferring agency identifier.
//	 *
//	 * @param transferringAgencyIdentifier the new transferring agency identifier
//	 */
//	public void setTransferringAgencyIdentifier(String transferringAgencyIdentifier) {
//		this.transferringAgencyIdentifier = transferringAgencyIdentifier;
//	}
//
//	/**
//	 * Gets the transferring agency organization descriptive metadata.
//	 *
//	 * @return the transferring agency organization descriptive metadata
//	 */
//	public String getTransferringAgencyOrganizationDescriptiveMetadataXmlData() {
//		return transferringAgencyOrganizationDescriptiveMetadataXmlData;
//	}
//
//	/**
//	 * Sets the transferring agency organization descriptive metadata.
//	 *
//	 * @param transferringAgencyOrganizationDescriptiveMetadataXmlData the new transferring
//	 *                                                          agency organization
//	 *                                                          descriptive metadata
//	 */
//	public void setTransferringAgencyOrganizationDescriptiveMetadataXmlData(
//			String transferringAgencyOrganizationDescriptiveMetadata) {
//		this.transferringAgencyOrganizationDescriptiveMetadataXmlData = transferringAgencyOrganizationDescriptiveMetadata;
//	}

    /**
     * Checks if is now flag.
     *
     * @return true, if is now flag
     */
    public boolean isNowFlag() {
        return nowFlag;
    }

    /**
     * Sets the now flag.
     *
     * @param nowFlag the new now flag
     */
    public void setNowFlag(boolean nowFlag) {
        this.nowFlag = nowFlag;
    }
}

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
package fr.gouv.vitam.tools.sedalib.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

/**
 * The Class ArchiveDeliveryRequestReply
 * <p>
 * Class for the SEDA DIP manifest (document XML ArchiveDeliveryRequestReply in
 * SEDA standard) content management. It contains elements declared in
 * manifest, but also objects to manage the structure. Elements not used in
 * Vitam DIPs, for the moment, are not supported in this class.
 * <p>
 * It uses the generic DataObjectPackage class for DataObjet and ArchiveUnit
 * structure.
 */

public class ArchiveDeliveryRequestReply {

    // SEDA elements
    /** The archive transfer global metadata. */
    private GlobalMetadata globalMetadata;

    /** The inner DataObjects and ArchiveUnit structure. */
    private DataObjectPackage dataObjectPackage;

    // Constructors

    /**
     * Instantiates a new empty ArchiveDeliveryRequestReply. Used for json
     * deserialization.
     */
    public ArchiveDeliveryRequestReply() {
        this.globalMetadata = null;
        this.setDataObjectPackage(new DataObjectPackage());
    }

    // Methods

    /**
     * Gets the summary description of ArchiveDeliveryRequestReply. It list the
     * number of ArchiveUnits, DataObjectGroup, BinaryDataObject (with size in
     * bytes) and PhysicalDatObject
     *
     * @return the description String
     */
    @JsonIgnore
    public String getDescription() {
        return "ArchiveDeliveryRequestReply\n" + getDataObjectPackage().getDescription();
    }

    // SEDA XML importer

    /**
     * Import start document of SEDA ArchiveDeliveryRequestReply XML.
     *
     * @param xmlReader      the SEDAXMLEventReader reading the SEDA manifest
     * @param sedaLibProgressLogger the progress logger or null if no progress log expected
     * @throws SEDALibException if the XML can't be read or is not in expected form
     */

    private static void importStartDocument(SEDAXMLEventReader xmlReader, SEDALibProgressLogger sedaLibProgressLogger)
            throws SEDALibException {
        XMLEvent event;
        try {
            event = xmlReader.nextUsefullEvent();
            if (!event.isStartDocument())
                throw new SEDALibException("Pas de document XML");
            if (!xmlReader.nextBlockIfNamed("ArchiveDeliveryRequestReply")) {
                throw new SEDALibException("Pas d'élément ArchiveTransfer");
            }
            if (sedaLibProgressLogger !=null)
                sedaLibProgressLogger.log(SEDALibProgressLogger.STEP, "Début de l'import du document ArchiveTransferRequestReply");

        } catch (XMLStreamException e) {
            throw new SEDALibException("Erreur de lecture XML\n->" + e.getMessage());
        }
    }

    /**
     * Import header, with global metadata, of SEDA ArchiveDeliveryRequestReply XML.
     *
     * @param xmlReader                   the SEDAXMLEventReader reading the SEDA
     *                                    manifest
     * @param archiveDeliveryRequestReply the ArchiveDeliveryRequestReply to be
     *                                    completed
     * @param sedaLibProgressLogger              the progress logger or null if no progress log expected
     */

    private static void importHeader(SEDAXMLEventReader xmlReader,
                                     ArchiveDeliveryRequestReply archiveDeliveryRequestReply, SEDALibProgressLogger sedaLibProgressLogger) {
        try {
            if (sedaLibProgressLogger !=null)
                sedaLibProgressLogger.log(SEDALibProgressLogger.STEP, "Début de l'import de l'entête");
            archiveDeliveryRequestReply.globalMetadata.comment = xmlReader.nextValueIfNamed("Comment");
            archiveDeliveryRequestReply.globalMetadata.date = xmlReader.nextMandatoryValue("Date");
            archiveDeliveryRequestReply.globalMetadata
                    .messageIdentifier = xmlReader.nextMandatoryValue("MessageIdentifier");
            if (xmlReader.peekBlockIfNamed("Signature"))
                throw new SEDALibException("L'élément Signature dans l'ArchiveTransfer n'est pas supporté");
            archiveDeliveryRequestReply.globalMetadata
                    .archivalAgreement = xmlReader.nextValueIfNamed("ArchivalAgreement");
            archiveDeliveryRequestReply.globalMetadata
                    .codeListVersionsXmlData = xmlReader.nextMandatoryBlockAsString("CodeListVersions");
            if (sedaLibProgressLogger !=null)
                sedaLibProgressLogger.log(SEDALibProgressLogger.STEP, "Entête importé");
        } catch (XMLStreamException | SEDALibException e) {
            // TODO to correct when VITAM DIP will use more elements
            if (sedaLibProgressLogger !=null)
                sedaLibProgressLogger.log(SEDALibProgressLogger.STEP,
                    "L'entête n'est pas conforme à un ArchiveDeliveryRequestReply, mais la tentative d'analyse continue");
            archiveDeliveryRequestReply.globalMetadata = null;
            // throw new SEDALibException("Erreur de lecture XML d'entête du manifest\n->" +
            // e.getMessage());
        }
    }

    /**
     * Import footer of manifest, after DataObjectPackage.
     *
     * @param xmlReader                   the SEDAXMLEventReader reading the SEDA
     *                                    manifest
     * @param archiveDeliveryRequestReply the ArchiveTransfer to be completed
     * @throws SEDALibException if the XML can't be read or is not in expected form
     */
    private static void importFooter(SEDAXMLEventReader xmlReader,
                                     ArchiveDeliveryRequestReply archiveDeliveryRequestReply) throws SEDALibException {
        try {
            if (xmlReader.peekBlockIfNamed("ReplyCode"))
                throw new SEDALibException(
                        "L'élément ReplyCode dans ArchiveDeliveryRequestReply n'est pas supporté à ce stade");
            if (xmlReader.peekBlockIfNamed("Operation"))
                throw new SEDALibException(
                        "L'élément Operation dans ArchiveDeliveryRequestReply n'est pas supporté à ce stade");
            if (xmlReader.peekBlockIfNamed("MessageRequestIdentifier"))
                throw new SEDALibException(
                        "L'élément MessageRequestIdentifier dans ArchiveDeliveryRequestReply n'est pas supporté à ce stade");
            if (xmlReader.peekBlockIfNamed("AuthorizationRequestReplyIdentifier"))
                throw new SEDALibException(
                        "L'élément AuthorizationRequestReplyIdentifier dans ArchiveDeliveryRequestReply n'est pas supporté à ce stade");
            if (xmlReader.peekBlockIfNamed("UnitIdentifier"))
                throw new SEDALibException(
                        "L'élément UnitIdentifier dans ArchiveDeliveryRequestReply n'est pas supporté à ce stade");
            if (xmlReader.peekBlockIfNamed("ArchivalAgency"))
                throw new SEDALibException(
                        "L'élément ArchivalAgency dans ArchiveDeliveryRequestReply n'est pas supporté à ce stade");
            if (xmlReader.peekBlockIfNamed("Requester"))
                throw new SEDALibException(
                        "L'élément Requester dans ArchiveDeliveryRequestReply n'est pas supporté à ce stade");
        } catch (XMLStreamException | SEDALibException e) {
            throw new SEDALibException("Erreur de lecture de la fin du manifest\n->" + e.getMessage());
        }
    }

    /**
     * Import end document.
     *
     * @param xmlReader                   the SEDAXMLEventReader reading the SEDA
     *                                    manifest
     * @param archiveDeliveryRequestReply the ArchiveDeliveryRequestReply to be
     *                                    completed
     * @throws SEDALibException if the XML can't be read or is not in expected form
     */
    private static void importEndDocument(SEDAXMLEventReader xmlReader,
                                          ArchiveDeliveryRequestReply archiveDeliveryRequestReply) throws SEDALibException {
        XMLEvent event;
        try {
            xmlReader.endBlockNamed("ArchiveDeliveryRequestReply");
            event = xmlReader.peekUsefullEvent();
            if (!event.isEndDocument())
                throw new SEDALibException("Pas de fin attendue du document XML");
        } catch (XMLStreamException | SEDALibException e) {
            throw new SEDALibException("Erreur de lecture de la cloture du manifest\n->" + e.getMessage());
        }
    }

    /**
     * Import the whole structure from XML SEDA Manifest.
     *
     * @param xmlReader      the SEDAXMLEventReader reading the SEDA manifest
     * @param rootDir        the directory where the BinaryDataObject files are
     *                       exported
     * @param sedaLibProgressLogger the progress logger or null if no progress log expected
     * @return the read ArchiveDeliveryRequestReply
     * @throws SEDALibException     if the XML can't be read or is not in expected form
     * @throws InterruptedException if export process is interrupted
     */
    public static ArchiveDeliveryRequestReply fromSedaXml(SEDAXMLEventReader xmlReader, String rootDir,
                                                          SEDALibProgressLogger sedaLibProgressLogger) throws SEDALibException, InterruptedException {
        ArchiveDeliveryRequestReply archiveDeliveryRequestReply;
        importStartDocument(xmlReader, sedaLibProgressLogger);
        archiveDeliveryRequestReply = new ArchiveDeliveryRequestReply();
        archiveDeliveryRequestReply.setGlobalMetadata(new GlobalMetadata());
        importHeader(xmlReader, archiveDeliveryRequestReply, sedaLibProgressLogger);
        archiveDeliveryRequestReply
                .setDataObjectPackage(DataObjectPackage.fromSedaXml(xmlReader, rootDir, sedaLibProgressLogger));
        importFooter(xmlReader, archiveDeliveryRequestReply);
        importEndDocument(xmlReader, archiveDeliveryRequestReply);

        return archiveDeliveryRequestReply;
    }

    // Getters and setters

    /**
     * Gets the DIP context.
     *
     * @return the archiveTransfer context GlobalMetadata
     */
    public GlobalMetadata getGlobalMetadata() {
        return globalMetadata;
    }

    /**
     * Sets the DIP context.
     *
     * @param globalMetadata the new archiveTransfer context
     */
    public void setGlobalMetadata(GlobalMetadata globalMetadata) {
        this.globalMetadata = globalMetadata;
    }

    /**
     * Gets the DataObjectPackage.
     *
     * @return the data object package
     */
    public DataObjectPackage getDataObjectPackage() {
        return dataObjectPackage;
    }

    /**
     * Sets the DataObjectPackage.
     *
     * @param dataObjectPackage the new data object package
     */
    public void setDataObjectPackage(DataObjectPackage dataObjectPackage) {
        this.dataObjectPackage = dataObjectPackage;
    }
}
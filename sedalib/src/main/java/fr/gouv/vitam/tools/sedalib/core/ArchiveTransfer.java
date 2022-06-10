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

import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;
import fr.gouv.vitam.tools.sedalib.xml.IndentXMLTool;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLStreamWriter;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLValidator;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import javax.xml.validation.Schema;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.doProgressLog;

/**
 * The Class ArchiveTransfer
 * <p>
 * Class for the SEDA SIP manifest (document XML ArchiveTransfer in SEDA
 * standard) content management. It contains all the elements declared in
 * manifest, but also objects to manage the structure.
 * <p>
 * It uses the generic DataObjectPackage class for DataObjet and ArchiveUnit
 * structure.
 */
public class ArchiveTransfer {

    // SEDA elements
    /**
     * The archive transfer global metadata.
     */
    private GlobalMetadata globalMetadata;

    /**
     * The inner DataObjects and ArchiveUnit structure.
     */
    private DataObjectPackage dataObjectPackage;

    // Constructors

    /**
     * Instantiates a new archive transfer. Used for json deserialization.
     */
    public ArchiveTransfer() {
        this.globalMetadata = null;
        this.setDataObjectPackage(new DataObjectPackage());
    }

    // Methods

    /**
     * Gets the summary description of ArchiveTransfer. It list the number of
     * ArchiveUnits, DataObjectGroup, BinaryDataObject (with size in bytes) and
     * PhysicalDatObject
     *
     * @return the description String
     */
    @JsonIgnore
    public String getDescription() {
        return "ArchiveTransfer\n" + getDataObjectPackage().getDescription();
    }

    // SEDA XML exporter

    /**
     * Export start document of SEDA ArchiveTransfer XML.
     *
     * @param xmlWriter the SEDAXMLStreamWriter generating the SEDA manifest
     * @throws SEDALibException if the XML can't be written
     */

    private void exportStartDocument(SEDAXMLStreamWriter xmlWriter) throws SEDALibException {
        try {
            xmlWriter.writeStartDocument();
            xmlWriter.writeStartElement("ArchiveTransfer");
            xmlWriter.writeNamespace("xlink", "http://www.w3.org/1999/xlink");
            xmlWriter.writeNamespace("pr", "info:lc/xmlns/premis-v2");
            switch(SEDA2Version.getSeda2Version()){
                case 1:
                    xmlWriter.writeDefaultNamespace("fr:gouv:culture:archivesdefrance:seda:v2.1");
                    xmlWriter.writeNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
                    xmlWriter.writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "schemaLocation",
                            "fr:gouv:culture:archivesdefrance:seda:v2.1 seda-2.1-main.xsd");
                    break;
                case 2:
                    xmlWriter.writeDefaultNamespace("fr:gouv:culture:archivesdefrance:seda:v2.2");
                    xmlWriter.writeNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
                    xmlWriter.writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "schemaLocation",
                            "fr:gouv:culture:archivesdefrance:seda:v2.2 seda-2.2-main.xsd");
                    break;
                default:
                    throw new SEDALibException("Version de SEDA sans schéma",null);
            }
            xmlWriter.setXmlId(true);
        } catch (XMLStreamException e) {
            throw new SEDALibException("Erreur d'écriture XML du début du manifest", e);
        }
    }

    /**
     * Export header, with global metadata, of SEDA ArchiveTransfer XML.
     *
     * @param xmlWriter the SEDAXMLStreamWriter generating the SEDA manifest
     * @throws SEDALibException if the XML can't be written
     */

    private void exportHeader(SEDAXMLStreamWriter xmlWriter) throws SEDALibException {
        try {
            xmlWriter.writeElementValueIfNotEmpty("Comment", globalMetadata.comment);
            if (globalMetadata.isNowFlag())
                globalMetadata.date = SEDAXMLStreamWriter.getStringFromDateTime(null);
            xmlWriter.writeElementValueIfNotEmpty("Date", globalMetadata.date);
            xmlWriter.writeElementValueIfNotEmpty("MessageIdentifier", globalMetadata.messageIdentifier);
            xmlWriter.writeElementValueIfNotEmpty("ArchivalAgreement", globalMetadata.archivalAgreement);
            xmlWriter.writeRawXMLBlockIfNotEmpty(globalMetadata.codeListVersionsXmlData);
        } catch (XMLStreamException e) {
            throw new SEDALibException("Erreur d'écriture XML d'entête du manifest", e);
        }
    }

    /**
     * Export footer of manifest, after ArchiveTransfer.
     *
     * @param xmlWriter the SEDAXMLStreamWriter generating the SEDA manifest
     * @throws SEDALibException if the XML can't be written
     */

    private void exportFooter(SEDAXMLStreamWriter xmlWriter) throws SEDALibException {
        try {
            xmlWriter.writeElementValueIfNotEmpty("TransferRequestReplyIdentifier",
                    globalMetadata.transferRequestReplyIdentifier);
            xmlWriter.writeStartElement("ArchivalAgency");
            xmlWriter.writeElementValue("Identifier", globalMetadata.archivalAgencyIdentifier);
            xmlWriter.writeRawXMLBlockIfNotEmpty(globalMetadata.transferringAgencyOrganizationDescriptiveMetadataXmlData);
            xmlWriter.writeEndElement();
            xmlWriter.writeStartElement("TransferringAgency");
            xmlWriter.writeElementValue("Identifier", globalMetadata.transferringAgencyIdentifier);
            xmlWriter.writeRawXMLBlockIfNotEmpty(globalMetadata.transferringAgencyOrganizationDescriptiveMetadataXmlData);
            xmlWriter.writeEndElement();
        } catch (XMLStreamException e) {
            throw new SEDALibException("Erreur d'écriture XML de la fin du manifest", e);
        }
    }

    /**
     * Export end document.
     *
     * @param xmlWriter the SEDAXMLStreamWriter generating the SEDA manifest
     * @throws SEDALibException if the XML can't be written
     */
    private void exportEndDocument(SEDAXMLStreamWriter xmlWriter) throws SEDALibException {
        try {
            xmlWriter.writeEndElement();
            xmlWriter.writeEndDocument();
            xmlWriter.flush();
        } catch (XMLStreamException e) {
            throw new SEDALibException("Erreur d'écriture XML de la cloture du manifest", e);
        }
    }

    /**
     * Export the whole structure in XML SEDA Manifest.
     *
     * @param xmlWriter             the SEDAXMLStreamWriter generating the SEDA manifest
     * @param imbricateFlag         indicates if the manifest ArchiveUnits are to be
     *                              exported in imbricate mode (true) or in flat mode
     *                              (false)
     * @param sedaLibProgressLogger the progress logger or null if no progress log expected
     * @throws SEDALibException     if the XML can't be written
     * @throws InterruptedException if export process is interrupted
     */
    public void toSedaXml(SEDAXMLStreamWriter xmlWriter, boolean imbricateFlag, SEDALibProgressLogger sedaLibProgressLogger)
            throws SEDALibException, InterruptedException {
        exportStartDocument(xmlWriter);
        exportHeader(xmlWriter);
        dataObjectPackage.toSedaXml(xmlWriter, imbricateFlag, sedaLibProgressLogger);
        exportFooter(xmlWriter);
        exportEndDocument(xmlWriter);

    }

    // SEDA XML importer

    /**
     * Import start document of SEDA ArchiveTransfer XML.
     *
     * @param xmlReader             the SEDAXMLEventReader reading the SEDA manifest
     * @throws SEDALibException if the XML can't be read or is not in expected form
     */

    private static void importStartDocument(SEDAXMLEventReader xmlReader)
            throws SEDALibException {
        XMLEvent event;
        try {
            event = xmlReader.nextUsefullEvent();
            if (!event.isStartDocument())
                throw new SEDALibException("Pas de document XML");
            if (!xmlReader.nextBlockIfNamed("ArchiveTransfer")) {
                throw new SEDALibException("Pas d'élément ArchiveTransfer");
            }
        } catch (XMLStreamException e) {
            throw new SEDALibException("Erreur de lecture XML", e);
        }
    }

    /**
     * Import header, with global metadata, of SEDA ArchiveTransfer XML.
     *
     * @param xmlReader             the SEDAXMLEventReader reading the SEDA manifest
     * @param archiveTransfer       the ArchiveTransfer to be completed
     * @throws SEDALibException if the XML can't be read or is not in expected form
     */

    private static void importHeader(SEDAXMLEventReader xmlReader, ArchiveTransfer archiveTransfer) throws SEDALibException {
        try {
            archiveTransfer.globalMetadata.comment = xmlReader.nextValueIfNamed("Comment");
            archiveTransfer.globalMetadata.date = xmlReader.nextMandatoryValue("Date");
            archiveTransfer.globalMetadata.messageIdentifier = xmlReader.nextMandatoryValue("MessageIdentifier");
            if (xmlReader.peekBlockIfNamed("Signature"))
                throw new SEDALibException("L'élément Signature dans l'ArchiveTransfer n'est pas supporté");
            archiveTransfer.globalMetadata.archivalAgreement = xmlReader.nextValueIfNamed("ArchivalAgreement");
            archiveTransfer.globalMetadata
                    .codeListVersionsXmlData = xmlReader.nextMandatoryBlockAsString("CodeListVersions");
        } catch (XMLStreamException | SEDALibException e) {
            throw new SEDALibException("Erreur de lecture XML d'entête du manifest", e);
        }
    }

    /**
     * Import footer of manifest, after ArchiveTransfer.
     *
     * @param xmlReader       the SEDAXMLEventReader reading the SEDA manifest
     * @param archiveTransfer the ArchiveTransfer to be completed
     * @throws SEDALibException if the XML can't be read or is not in expected form
     */
    private static void importFooter(SEDAXMLEventReader xmlReader, ArchiveTransfer archiveTransfer)
            throws SEDALibException {
        try {
            if (xmlReader.peekBlockIfNamed("RelatedTransferReference"))
                throw new SEDALibException(
                        "L'élément RelatedTransferReference dans l'ArchiveTransfer n'est pas supporté");

            archiveTransfer.globalMetadata
                    .transferRequestReplyIdentifier = xmlReader.nextValueIfNamed("TransferRequestReplyIdentifier");
            if (!xmlReader.nextBlockIfNamed("ArchivalAgency"))
                throw new SEDALibException("Elément ArchivalAgency obligatoire");
            archiveTransfer.globalMetadata.archivalAgencyIdentifier = xmlReader.nextMandatoryValue("Identifier");
            archiveTransfer.globalMetadata.archivalAgencyOrganizationDescriptiveMetadataXmlData =
                    xmlReader.nextBlockAsStringIfNamed("OrganizationDescriptiveMetadata");
            xmlReader.endBlockNamed("ArchivalAgency");
            if (!xmlReader.nextBlockIfNamed("TransferringAgency"))
                throw new SEDALibException("Elément TransferingAgency Obligatoire");
            archiveTransfer.globalMetadata.transferringAgencyIdentifier = xmlReader.nextMandatoryValue("Identifier");
            archiveTransfer.globalMetadata.transferringAgencyOrganizationDescriptiveMetadataXmlData =
                    xmlReader.nextBlockAsStringIfNamed("OrganizationDescriptiveMetadata");
            xmlReader.endBlockNamed("TransferringAgency");
        } catch (XMLStreamException | SEDALibException e) {
            throw new SEDALibException("Erreur de lecture de la fin du manifest", e);
        }
    }

    /**
     * Import end document.
     *
     * @param xmlReader       the SEDAXMLEventReader reading the SEDA manifest
     * @throws SEDALibException if the XML can't be read or is not in expected form
     */
    private static void importEndDocument(SEDAXMLEventReader xmlReader)
            throws SEDALibException {
        XMLEvent event;
        try {
            xmlReader.endBlockNamed("ArchiveTransfer");
            event = xmlReader.peekUsefullEvent();
            if (!event.isEndDocument())
                throw new SEDALibException("Pas de fin attendue du document XML");
        } catch (XMLStreamException | SEDALibException e) {
            throw new SEDALibException("Erreur de lecture de la cloture du manifest", e);
        }
    }

    /**
     * Import the whole structure from XML SEDA Manifest.
     *
     * @param xmlReader             the SEDAXMLEventReader reading the SEDA manifest
     * @param rootDir               the directory where the BinaryDataObject files are
     *                              exported
     * @param sedaLibProgressLogger the progress logger or null if no progress log expected
     * @return the read ArchiveTransfer
     * @throws SEDALibException     if the XML can't be read or is not in expected form
     * @throws InterruptedException if export process is interrupted
     */
    public static ArchiveTransfer fromSedaXml(SEDAXMLEventReader xmlReader, String rootDir,
                                              SEDALibProgressLogger sedaLibProgressLogger) throws SEDALibException, InterruptedException {
        ArchiveTransfer archiveTransfer;
        importStartDocument(xmlReader);
        archiveTransfer = new ArchiveTransfer();
        archiveTransfer.setGlobalMetadata(new GlobalMetadata());
        importHeader(xmlReader, archiveTransfer);
        archiveTransfer.setDataObjectPackage(DataObjectPackage.fromSedaXml(xmlReader, rootDir, sedaLibProgressLogger));
        importFooter(xmlReader, archiveTransfer);
        importEndDocument(xmlReader);

        doProgressLog(sedaLibProgressLogger, SEDALibProgressLogger.STEP,
                "sedalib: ArchiveTransfer importé", null);

        return archiveTransfer;
    }

    // SEDA Validator

    public void seda21Validate(SEDALibProgressLogger sedaLibProgressLogger) throws SEDALibException, InterruptedException {
        String manifest;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             SEDAXMLStreamWriter ixsw = new SEDAXMLStreamWriter(baos, IndentXMLTool.STANDARD_INDENT)) {
            toSedaXml(ixsw, true, sedaLibProgressLogger);
            manifest = baos.toString("UTF-8");
        } catch (XMLStreamException | IOException e) {
            throw new SEDALibException("Echec d'écriture XML du manifest", e);
        }
        SEDAXMLValidator sedaXMLvalidator = new SEDAXMLValidator();
        Schema sedaSchema = sedaXMLvalidator.getSEDASchema();
        sedaXMLvalidator.checkWithXSDSchema(manifest, sedaSchema);
    }

    public void sedaProfileValidate(String profileFileName, SEDALibProgressLogger sedaLibProgressLogger) throws SEDALibException, InterruptedException {
        String manifest;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             SEDAXMLStreamWriter ixsw = new SEDAXMLStreamWriter(baos, IndentXMLTool.STANDARD_INDENT)) {
            toSedaXml(ixsw, true, sedaLibProgressLogger);
            manifest = baos.toString("UTF-8");
        } catch (XMLStreamException | IOException e) {
            throw new SEDALibException("Echec d'écriture XML du manifest", e);
        }
        SEDAXMLValidator sedaXMLvalidator = new SEDAXMLValidator();
        Schema sedaSchema;
        if (profileFileName.endsWith(".rng"))
            sedaSchema = sedaXMLvalidator.getSchemaFromRNGFile(profileFileName);
        else
            sedaSchema = sedaXMLvalidator.getSchemaFromXSDFile(profileFileName);

        if (profileFileName.endsWith(".rng"))
            sedaXMLvalidator.checkWithRNGSchema(manifest, sedaSchema);
        else
            sedaXMLvalidator.checkWithXSDSchema(manifest, sedaSchema);
    }

    // Getters and setters

    /**
     * Gets the SIP context.
     *
     * @return the archiveTransfer context GlobalMetadata
     */
    public GlobalMetadata getGlobalMetadata() {
        return globalMetadata;
    }

    /**
     * Sets the SIP context.
     *
     * @param globalMetadata the archiveTransfer context GlobalMetadata
     */
    public void setGlobalMetadata(GlobalMetadata globalMetadata) {
        this.globalMetadata = globalMetadata;
    }

    /**
     * Gets the DataObjectPackage.
     *
     * @return the DataObjectPackage
     */
    public DataObjectPackage getDataObjectPackage() {
        return dataObjectPackage;
    }

    /**
     * Sets the DataObjectPackage.
     *
     * @param dataObjectPackage the new DataObjectPackage
     */
    public void setDataObjectPackage(DataObjectPackage dataObjectPackage) {
        this.dataObjectPackage = dataObjectPackage;
    }
}
/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to provide
 * tools for construction and manipulation of SIP (Submission
 * Information Package) conform to the SEDA (Standard d’Échange
 * de données pour l’Archivage) standard.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitam.tools.sedalib.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.gouv.vitam.tools.sedalib.metadata.Operation;
import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.InnerIdentifierType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.StringType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;
import fr.gouv.vitam.tools.sedaschema.SedaVersion;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.GLOBAL;
import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.doProgressLog;

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
    /**
     * The archive transfer global metadata.
     */
    private GlobalMetadata globalMetadata;

    /**
     * The inner DataObjects and ArchiveUnit structure.
     */
    private DataObjectPackage dataObjectPackage;

    /**
     * The Reply Code.
     */
    private StringType replyCode;

    /**
     * The Operation.
     */
    private Operation operation;

    /**
     * The Message Request Identifier.
     */
    private StringType messageRequestIdentifier;

    /**
     * The Authorization Request Reply Identifier.
     */
    private StringType authorizationRequestReplyIdentifier;

    /**
     * The Unit Identifier.
     */
    private StringType unitIdentifier;

    /**
     * The Archival Agency Identifier.
     */
    private InnerIdentifierType archivalAgency;

    /**
     * The Requester Identifier.
     */
    private InnerIdentifierType requester;

    // Constructors

    /**
     * Instantiates an ArchiveDeliveryRequestReply for json deserialization, which sets its
     * DataObjectPackage and so its SEDA version.
     */
    private ArchiveDeliveryRequestReply() {
        this.globalMetadata = null;
    }

    /**
     * Instantiates a new empty ArchiveDeliveryRequestReply.
     *
     * @param sedaVersion the SEDA version of the reply
     */
    public ArchiveDeliveryRequestReply(SedaVersion sedaVersion) {
        this.globalMetadata = null;
        this.setDataObjectPackage(new DataObjectPackage(sedaVersion));
    }

    // Methods

    /**
     * Gets the SEDA version, the one of its DataObjectPackage.
     *
     * @return the SEDA version
     */
    @JsonIgnore
    public SedaVersion getSedaVersion() {
        return dataObjectPackage.getSedaVersion();
    }

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
     * @param xmlReader             the SEDAXMLEventReader reading the SEDA manifest
     * @throws SEDALibException if the XML can't be read or is not in expected form
     */
    private static SedaVersion importStartDocument(SEDAXMLEventReader xmlReader) throws SEDALibException {
        XMLEvent event;
        String namespace;
        try {
            event = xmlReader.nextUsefullEvent();
            if (!event.isStartDocument()) throw new SEDALibException("Pas de document XML");
            event = xmlReader.peekUsefullEvent();
            namespace = event.isStartElement() ? event.asStartElement().getName().getNamespaceURI() : null;
            if (!xmlReader.nextBlockIfNamed("ArchiveDeliveryRequestReply")) {
                throw new SEDALibException("Pas d'élément ArchiveTransfer");
            }
        } catch (XMLStreamException e) {
            throw new SEDALibException("Erreur de lecture XML", e);
        }
        // the version of the manifest is the one of its namespace
        SedaVersion sedaVersion = SedaVersion.fromNamespace(namespace);
        if (sedaVersion == null) throw new SEDALibException(
            "Le manifest n'est pas dans une version SEDA connue, son espace de noms est [" + namespace + "]"
        );
        return sedaVersion;
    }

    /**
     * Import header, with global metadata, of SEDA ArchiveDeliveryRequestReply XML.
     *
     * @param xmlReader                   the SEDAXMLEventReader reading the SEDA
     *                                    manifest
     * @param archiveDeliveryRequestReply the ArchiveDeliveryRequestReply to be
     *                                    completed
     * @param sedaLibProgressLogger       the progress logger or null if no progress log expected
     * @throws InterruptedException       if interrupted
     */

    private static void importHeader(
        SEDAXMLEventReader xmlReader,
        ArchiveDeliveryRequestReply archiveDeliveryRequestReply,
        SEDALibProgressLogger sedaLibProgressLogger
    ) throws InterruptedException {
        try {
            archiveDeliveryRequestReply.globalMetadata.comment = xmlReader.nextValueIfNamed("Comment");
            archiveDeliveryRequestReply.globalMetadata.date = xmlReader.nextMandatoryValue("Date");
            archiveDeliveryRequestReply.globalMetadata.messageIdentifier = xmlReader.nextMandatoryValue(
                "MessageIdentifier"
            );
            if (xmlReader.peekBlockIfNamed("Signature")) throw new SEDALibException(
                "L'élément Signature dans l'ArchiveTransfer n'est pas supporté"
            );
            archiveDeliveryRequestReply.globalMetadata.archivalAgreement = xmlReader.nextValueIfNamed(
                "ArchivalAgreement"
            );
            archiveDeliveryRequestReply.globalMetadata.codeListVersionsXmlData = xmlReader.nextMandatoryBlockAsString(
                "CodeListVersions"
            );
        } catch (XMLStreamException | SEDALibException e) {
            doProgressLog(
                sedaLibProgressLogger,
                SEDALibProgressLogger.STEP,
                "sedalib: l'entête n'est pas conforme à un ArchiveDeliveryRequestReply, mais la tentative d'analyse continue",
                null
            );
            archiveDeliveryRequestReply.globalMetadata = null;
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
    private static void importFooter(
        SEDAXMLEventReader xmlReader,
        ArchiveDeliveryRequestReply archiveDeliveryRequestReply,
        SEDALibProgressLogger sedaLibProgressLogger
    ) throws SEDALibException, InterruptedException { //NOSONAR
        try {
            SEDALibProgressLogger.doProgressLog(
                sedaLibProgressLogger,
                GLOBAL,
                "sedalib: début du bloc de fin spécifique du DIP",
                null
            );
            if (xmlReader.peekBlockIfNamed("ReplyCode")) {
                archiveDeliveryRequestReply.replyCode = (StringType) SEDAMetadata.fromSedaXml(
                    xmlReader,
                    StringType.class,
                    archiveDeliveryRequestReply.getSedaVersion()
                );
                SEDALibProgressLogger.doProgressLog(
                    sedaLibProgressLogger,
                    GLOBAL,
                    archiveDeliveryRequestReply.replyCode.toString(),
                    null
                );
            }
            if (xmlReader.peekBlockIfNamed("Operation")) {
                archiveDeliveryRequestReply.operation = (Operation) SEDAMetadata.fromSedaXml(
                    xmlReader,
                    Operation.class,
                    archiveDeliveryRequestReply.getSedaVersion()
                );
                SEDALibProgressLogger.doProgressLog(
                    sedaLibProgressLogger,
                    GLOBAL,
                    archiveDeliveryRequestReply.operation.toString(),
                    null
                );
            }
            if (xmlReader.peekBlockIfNamed("MessageRequestIdentifier")) {
                archiveDeliveryRequestReply.messageRequestIdentifier = (StringType) SEDAMetadata.fromSedaXml(
                    xmlReader,
                    StringType.class,
                    archiveDeliveryRequestReply.getSedaVersion()
                );
                SEDALibProgressLogger.doProgressLog(
                    sedaLibProgressLogger,
                    GLOBAL,
                    archiveDeliveryRequestReply.messageRequestIdentifier.toString(),
                    null
                );
            }
            if (xmlReader.peekBlockIfNamed("AuthorizationRequestReplyIdentifier")) {
                archiveDeliveryRequestReply.authorizationRequestReplyIdentifier = (StringType) SEDAMetadata.fromSedaXml(
                    xmlReader,
                    StringType.class,
                    archiveDeliveryRequestReply.getSedaVersion()
                );
                SEDALibProgressLogger.doProgressLog(
                    sedaLibProgressLogger,
                    GLOBAL,
                    archiveDeliveryRequestReply.authorizationRequestReplyIdentifier.toString(),
                    null
                );
            }
            if (xmlReader.peekBlockIfNamed("UnitIdentifier")) {
                archiveDeliveryRequestReply.unitIdentifier = (StringType) SEDAMetadata.fromSedaXml(
                    xmlReader,
                    StringType.class,
                    archiveDeliveryRequestReply.getSedaVersion()
                );
                SEDALibProgressLogger.doProgressLog(
                    sedaLibProgressLogger,
                    GLOBAL,
                    archiveDeliveryRequestReply.unitIdentifier.toString(),
                    null
                );
            }
            if (xmlReader.peekBlockIfNamed("ArchivalAgency")) {
                archiveDeliveryRequestReply.archivalAgency = (InnerIdentifierType) SEDAMetadata.fromSedaXml(
                    xmlReader,
                    InnerIdentifierType.class,
                    archiveDeliveryRequestReply.getSedaVersion()
                );
                SEDALibProgressLogger.doProgressLog(
                    sedaLibProgressLogger,
                    GLOBAL,
                    archiveDeliveryRequestReply.archivalAgency.toString(),
                    null
                );
            }
            if (xmlReader.peekBlockIfNamed("Requester")) {
                archiveDeliveryRequestReply.requester = (InnerIdentifierType) SEDAMetadata.fromSedaXml(
                    xmlReader,
                    InnerIdentifierType.class,
                    archiveDeliveryRequestReply.getSedaVersion()
                );
                SEDALibProgressLogger.doProgressLog(
                    sedaLibProgressLogger,
                    GLOBAL,
                    archiveDeliveryRequestReply.requester.toString(),
                    null
                );
            }
            SEDALibProgressLogger.doProgressLog(
                sedaLibProgressLogger,
                GLOBAL,
                "sedalib: fin du bloc de fin spécifique du DIP",
                null
            );
        } catch (XMLStreamException | SEDALibException e) {
            throw new SEDALibException("Erreur de lecture de la fin du manifest", e);
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
    private static void importEndDocument(
        SEDAXMLEventReader xmlReader,
        ArchiveDeliveryRequestReply archiveDeliveryRequestReply
    ) throws SEDALibException { //NOSONAR
        // this parameter may be useful in future if this function is improved
        XMLEvent event;
        try {
            xmlReader.endBlockNamed("ArchiveDeliveryRequestReply");
            event = xmlReader.peekUsefullEvent();
            if (!event.isEndDocument()) throw new SEDALibException("Pas de fin attendue du document XML");
        } catch (XMLStreamException | SEDALibException e) {
            throw new SEDALibException("Erreur de lecture de la cloture du manifest", e);
        }
    }

    /**
     * Import the whole structure from XML SEDA Manifest.
     *
     * The SEDA version is the one of the manifest namespace.
     *
     * @param xmlReader             the SEDAXMLEventReader reading the SEDA manifest
     * @param rootDir               the directory where the BinaryDataObject files are
     *                              exported
     * @param sedaLibProgressLogger the progress logger or null if no progress log expected
     * @return the read ArchiveDeliveryRequestReply
     * @throws SEDALibException     if the XML can't be read, is not in expected form or not in a known
     *                              SEDA version
     * @throws InterruptedException if export process is interrupted
     */
    public static ArchiveDeliveryRequestReply fromSedaXml(
        SEDAXMLEventReader xmlReader,
        String rootDir,
        SEDALibProgressLogger sedaLibProgressLogger
    ) throws SEDALibException, InterruptedException {
        ArchiveDeliveryRequestReply archiveDeliveryRequestReply;
        SedaVersion sedaVersion = importStartDocument(xmlReader);
        archiveDeliveryRequestReply = new ArchiveDeliveryRequestReply(sedaVersion);
        archiveDeliveryRequestReply.setGlobalMetadata(new GlobalMetadata());
        importHeader(xmlReader, archiveDeliveryRequestReply, sedaLibProgressLogger);
        archiveDeliveryRequestReply.setDataObjectPackage(
            DataObjectPackage.fromSedaXml(xmlReader, sedaVersion, rootDir, sedaLibProgressLogger)
        );
        importFooter(xmlReader, archiveDeliveryRequestReply, sedaLibProgressLogger);
        importEndDocument(xmlReader, archiveDeliveryRequestReply);

        doProgressLog(
            sedaLibProgressLogger,
            SEDALibProgressLogger.STEP,
            "sedalib: archiveDeliveryRequestReply importé",
            null
        );

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

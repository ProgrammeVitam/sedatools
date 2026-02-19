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
package fr.gouv.vitam.tools.sedalib.process;

import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.core.DataObjectGroup;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;
import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.compacted.*;
import fr.gouv.vitam.tools.sedalib.metadata.content.Content;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.StringType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.TextType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.doProgressLog;
import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.doProgressLogIfStep;

/**
 * The Class Compactor.
 * <p>
 * Class for compacting an ArchiveUnit hierarchy.
 * <p>
 * The original ArchiveUnit hierarchy is replaced by another reduced hierarchy collecting all informations
 * in the specific metadatas DocumentContainer, DocumentPack, RecordGrp, Document, SubDocument and FileObjects.
 * This compacted format is reversible for the kept metadatas (To be noticed:
 * ArchiveUnitProfiles in the original ArchiveUnit hierarchy, if any, are lost)
 */

public class Compactor {

    /**
     * Log and error string finals.
     */
    private static final String MODULE = "sedalib: ";
    private static final String EXPORTED_DOCUMENTS = " documents exportés";

    /**
     * The metadata tags.
     */
    private static final String DESCRIPTION_LEVEL = "DescriptionLevel";
    private static final String TITLE = "Title";

    /**
     * The data Object package containing archive unit to compact.
     */
    private DataObjectPackage dataObjectPackage;

    /**
     * The compacted documents tree.
     */

    private RecordGrp rootRecordGrp;

    /**
     * The compacted documents list.
     */
    private List<Document> documentsList;

    /**
     * The compacted files management class and list var.
     */
    static class CompactedFile {

        String compactedFilename;
        Path onDiskPath;

        CompactedFile(String compactedFilename, Path onDiskPath) {
            this.compactedFilename = compactedFilename;
            this.onDiskPath = onDiskPath;
        }
    }

    private Map<Document, List<CompactedFile>> compactedFileListMap;

    /**
     * The document data object version and content metada filter.
     */
    private List<String> documentObjectVersionFilter;
    private Map<String, Integer> documentContentMetadataFilter;

    /**
     * The sub document data object version and content metada filter.
     */
    private List<String> subDocumentObjectVersionFilter;
    private Map<String, Integer> subDocumentContentMetadataFilter;

    /**
     * The max number of documents in a DocumentPack.
     */
    private int packMaxDocumentNumber;

    /**
     * The max length of documents metadatain a DocumentPack.
     */
    private int packDocumentsMaxMetadataSize;

    /**
     * Is deflated flag.
     */
    private boolean deflatedFlag;

    /**
     * The processed treenode, document, sub-document and with dropped metadata or file counters.
     */
    private int treenodeCounter;
    private int documentCounter;
    private int localSubDocumentCounter;
    private int totalSubDocumentCounter;
    private int fileCounter;
    private int treenodeWithDroppedNotDesciptiveMetadata;
    private int treenodeWithDroppedFile;
    private int leafWithDroppedFile;
    private int droppedMetadataCounted;
    private int truncatedMetadataCounted;
    private int packCounter;

    /**
     * The archive unit to compact
     */
    private ArchiveUnit archiveUnit;

    /**
     * The work directory name where to compact
     */
    private String workDirectoryName;

    /**
     * The start and end instants, for duration computation.
     */
    private Instant start;
    private Instant end;

    /**
     * The progress logger.
     */
    private final SEDALibProgressLogger sedaLibProgressLogger;

    /**
     * Instantiates a new Compactor.
     *
     * @param sedaLibProgressLogger the progress logger or null if no progress log expected
     */
    private Compactor(SEDALibProgressLogger sedaLibProgressLogger) {
        this.dataObjectPackage = null;
        this.sedaLibProgressLogger = sedaLibProgressLogger;
        this.documentObjectVersionFilter = null;
        this.documentContentMetadataFilter = null;
        this.subDocumentObjectVersionFilter = null;
        this.subDocumentContentMetadataFilter = null;
        this.rootRecordGrp = null;
        this.documentsList = null;
        this.compactedFileListMap = null;
        this.treenodeCounter = 0;
        this.documentCounter = 0;
        this.fileCounter = 0;
        this.localSubDocumentCounter = 0;
        this.totalSubDocumentCounter = 0;
        this.leafWithDroppedFile = 0;
        this.droppedMetadataCounted = 0;
        this.truncatedMetadataCounted = 0;
        this.packCounter = 0;
        this.deflatedFlag = false;
    }

    /**
     * Instantiates a new Compactor for an archive unit with a work directory.
     *
     * @param archiveUnit           the archive unit
     * @param workDirectoryName     the work directory name
     * @param sedaLibProgressLogger the progress logger
     */
    public Compactor(ArchiveUnit archiveUnit, String workDirectoryName, SEDALibProgressLogger sedaLibProgressLogger) {
        this(sedaLibProgressLogger);
        this.archiveUnit = archiveUnit;
        this.workDirectoryName = workDirectoryName;
        this.dataObjectPackage = archiveUnit.getDataObjectPackage();
    }

    /**
     * Set compacted document pack limits for metadata size and documents number.
     *
     * @param packDocumentsMaxMetadataSize the pack documents max metadata size
     * @param packMaxDocumentNumber        the pack max document number
     */
    public void setCompactedDocumentPackLimit(int packDocumentsMaxMetadataSize, int packMaxDocumentNumber) {
        this.packDocumentsMaxMetadataSize = packDocumentsMaxMetadataSize;
        this.packMaxDocumentNumber = packMaxDocumentNumber;
    }

    /**
     * Set object version filters.
     *
     * @param documentObjectVersionFilter    the document object version filter
     * @param subDocumentObjectVersionFilter the sub document object version filter
     */
    public void setObjectVersionFilters(
        List<String> documentObjectVersionFilter,
        List<String> subDocumentObjectVersionFilter
    ) {
        this.documentObjectVersionFilter = documentObjectVersionFilter;
        this.subDocumentObjectVersionFilter = subDocumentObjectVersionFilter;
    }

    /**
     * Set metadata filters.
     *
     * @param documentContentMetadataFilter    the document content metadata filter
     * @param subDocumentContentMetadataFilter the sub document content metadata filter
     */
    public void setMetadataFilters(
        Map<String, Integer> documentContentMetadataFilter,
        Map<String, Integer> subDocumentContentMetadataFilter
    ) {
        this.documentContentMetadataFilter = documentContentMetadataFilter;
        this.subDocumentContentMetadataFilter = subDocumentContentMetadataFilter;
    }

    /**
     * Is deflated flag boolean.
     *
     * @return the boolean
     */
    public boolean isDeflatedFlag() {
        return deflatedFlag;
    }

    /**
     * Sets deflated flag.
     *
     * @param deflatedFlag the deflated flag
     */
    public void setDeflatedFlag(boolean deflatedFlag) {
        this.deflatedFlag = deflatedFlag;
    }

    private static String getExtension(String fileName) {
        if (fileName == null) return "";
        int i = fileName.lastIndexOf('.');
        return i < 0 ? "" : fileName.substring(i + 1);
    }

    private static String getExtendedCompactedFileName(String radical, Path onDiskPath) {
        String extension;
        extension = getExtension(onDiskPath.getFileName().toString());
        if (!extension.isEmpty()) return radical + "." + extension;
        else return radical;
    }

    private SEDAMetadata truncateTextType(SEDAMetadata sm, int limit) throws SEDALibException {
        if ((sm instanceof TextType) && (((TextType) sm).getValue().length() > limit)) {
            sm = new TextType(
                sm.getXmlElementName(),
                ((TextType) sm).getValue().substring(0, limit),
                ((TextType) sm).getLang()
            );
            truncatedMetadataCounted++;
        }
        if (sm instanceof StringType) {
            if (((StringType) sm).getValue().length() > limit) {
                sm = new TextType(sm.getXmlElementName(), ((StringType) sm).getValue().substring(0, limit));
                truncatedMetadataCounted++;
            }
        } else throw new SEDALibException(
            "Tentative pendant le compactage de troncature " +
            "d'une métadonnée [" +
            sm.getXmlElementName() +
            "] qui n'est pas de type TextType ou StringType"
        );
        return sm;
    }

    private Content copyAllSEDAMetadata(Content originContent, Content destContent) {
        destContent.getMetadataList().addAll(originContent.getMetadataList());
        return destContent;
    }

    private Content filterContentMetadata(boolean documentFlag, Content originContent) throws SEDALibException {
        Map<String, Integer> contentMetadataFilter;
        Content resultContent = new Content();

        contentMetadataFilter = (documentFlag ? documentContentMetadataFilter : subDocumentContentMetadataFilter);
        // if no metadata filter defined all is copied
        if (contentMetadataFilter == null) {
            copyAllSEDAMetadata(originContent, resultContent);
        } else for (SEDAMetadata sm : originContent.getMetadataList()) {
            Integer limit = contentMetadataFilter.get(sm.getXmlElementName());
            if (limit != null) {
                if (limit == 0) resultContent.getMetadataList().add(sm);
                else resultContent.getMetadataList().add(truncateTextType(sm, limit));
            } else droppedMetadataCounted++;
        }
        return resultContent;
    }

    private SubDocument getCompactedSubDocumentArchiveUnit(
        ArchiveUnit au,
        String parentAuURI,
        List<CompactedFile> compactedFilesList
    ) throws SEDALibException {
        SubDocument subDocument;
        String auURI;
        String compactedFileURI;

        subDocument = new SubDocument(filterContentMetadata(false, au.getContent()));
        auURI = parentAuURI + File.separator + "SubDocument" + localSubDocumentCounter;
        localSubDocumentCounter++;
        if (au.getTheDataObjectGroup() != null) {
            for (BinaryDataObject bdo : au.getTheDataObjectGroup().getBinaryDataObjectList()) {
                StringType dataObjectVersion = bdo.getMetadataDataObjectVersion();
                String radical = dataObjectVersion.getValue().split("_")[0];
                if (subDocumentObjectVersionFilter.contains(radical)) {
                    compactedFileURI = getExtendedCompactedFileName(
                        auURI + "-" + dataObjectVersion.getValue(),
                        bdo.getOnDiskPath()
                    );
                    subDocument.addMetadata(new FileObject(bdo, compactedFileURI));
                    compactedFilesList.add(new CompactedFile(compactedFileURI, bdo.getOnDiskPath()));
                } else leafWithDroppedFile++;
            }
        }
        for (ArchiveUnit auChild : au.getChildrenAuList().getArchiveUnitList()) {
            subDocument.addMetadata(getCompactedSubDocumentArchiveUnit(auChild, auURI, compactedFilesList));
        }
        totalSubDocumentCounter++;
        return subDocument;
    }

    private Document getCompactedDocumentArchiveUnit(ArchiveUnit au, RecordGrp parentRecordGrp)
        throws SEDALibException {
        Document document;
        String parentRecordGrpID;
        String auURI;
        String compactedFileURI;
        List<CompactedFile> compactedFileList = new ArrayList<>();

        parentRecordGrpID = parentRecordGrp.getSimpleMetadata("RecordGrpID");
        document = new Document(parentRecordGrpID, filterContentMetadata(true, au.getContent()));
        auURI = parentRecordGrpID + "-" + "Document" + documentCounter;
        if (au.getTheDataObjectGroup() != null) {
            for (BinaryDataObject bdo : au.getTheDataObjectGroup().getBinaryDataObjectList()) {
                StringType dataObjectVersion = bdo.getMetadataDataObjectVersion();
                String radical = dataObjectVersion.getValue().split("_")[0];
                if (documentObjectVersionFilter.contains(radical)) {
                    compactedFileURI = getExtendedCompactedFileName(
                        auURI + "-" + dataObjectVersion.getValue(),
                        bdo.getOnDiskPath()
                    );
                    document.addMetadata(new FileObject(bdo, compactedFileURI));
                    compactedFileList.add(new CompactedFile(compactedFileURI, bdo.getOnDiskPath()));
                } else leafWithDroppedFile++;
            }
        }
        localSubDocumentCounter = 1;
        for (ArchiveUnit auChild : au.getChildrenAuList().getArchiveUnitList()) {
            document.addMetadata(getCompactedSubDocumentArchiveUnit(auChild, auURI, compactedFileList));
        }
        documentCounter++;
        compactedFileListMap.put(document, compactedFileList);
        fileCounter += compactedFileList.size();
        return document;
    }

    private RecordGrp addTreeNodeChild(ArchiveUnit au, RecordGrp parentRecordGrp) throws SEDALibException {
        RecordGrp curRecordGrp;

        curRecordGrp = new RecordGrp("Node" + treenodeCounter, au.getContent(), au.getManagement());
        if (parentRecordGrp == null) rootRecordGrp = curRecordGrp;
        else parentRecordGrp.addMetadata(curRecordGrp);
        if (!au.getDataObjectRefList().getDataObjectList().isEmpty()) treenodeWithDroppedFile++;
        return curRecordGrp;
    }

    private void recurseCompactArchiveUnit(ArchiveUnit au, RecordGrp parentRecordGrp)
        throws SEDALibException, InterruptedException {
        if (dataObjectPackage.isTouchedInDataObjectPackageId(au.getInDataObjectPackageId())) return;
        dataObjectPackage.addTouchedInDataObjectPackageId(au.getInDataObjectPackageId());
        String descriptionLevel = au.getContent().getSimpleMetadata(DESCRIPTION_LEVEL);
        if (!"Item".equals(descriptionLevel)) {
            treenodeCounter++;
            RecordGrp curRecordGrp = addTreeNodeChild(au, parentRecordGrp);
            if (
                (au.getManagement() != null) || (au.getArchiveUnitProfile() != null)
            ) treenodeWithDroppedNotDesciptiveMetadata++;
            for (ArchiveUnit auChild : au.getChildrenAuList().getArchiveUnitList()) recurseCompactArchiveUnit(
                auChild,
                curRecordGrp
            );
        } else documentsList.add(getCompactedDocumentArchiveUnit(au, parentRecordGrp));
        int counter = dataObjectPackage.getNextInOutCounter();
        doProgressLogIfStep(
            sedaLibProgressLogger,
            SEDALibProgressLogger.OBJECTS_GROUP,
            counter,
            MODULE + counter + " ArchiveUnit compactées"
        );
    }

    private Path createDocumentPackArchiveFile(int packCount, List<CompactedFile> compactedFileList)
        throws SEDALibException {
        Path archiveFile = Paths.get(workDirectoryName)
            .toAbsolutePath()
            .resolve("Document" + packCount + (deflatedFlag ? ".zip" : ".tar"));

        if (deflatedFlag) {
            try (ZipOutputStream zipout = new ZipOutputStream(new FileOutputStream(archiveFile.toString()))) {
                ZipEntry e;
                for (CompactedFile compactedFile : compactedFileList) {
                    e = new ZipEntry(compactedFile.compactedFilename);
                    zipout.putNextEntry(e);
                    try (FileInputStream fis = new FileInputStream(compactedFile.onDiskPath.toFile())) {
                        IOUtils.copy(fis, zipout);
                    }
                    zipout.closeEntry();
                }
            } catch (IOException e) {
                throw new SEDALibException(
                    "Echec de l'export du fichier du paquet de documents [" + archiveFile + "]",
                    e
                );
            }
        } else {
            try (
                TarArchiveOutputStream tarOut = new TarArchiveOutputStream(new FileOutputStream(archiveFile.toString()))
            ) {
                for (CompactedFile compactedFile : compactedFileList) {
                    TarArchiveEntry e = new TarArchiveEntry(
                        compactedFile.onDiskPath.toFile(),
                        compactedFile.compactedFilename
                    );
                    tarOut.putArchiveEntry(e);
                    try (FileInputStream fis = new FileInputStream(compactedFile.onDiskPath.toFile())) {
                        IOUtils.copy(fis, tarOut);
                    }
                    tarOut.closeArchiveEntry();
                }
            } catch (IOException e) {
                throw new SEDALibException(
                    "Echec de l'export du fichier du paquet de documents [" + archiveFile + "]",
                    e
                );
            }
        }
        return archiveFile;
    }

    private void addDocumentPackArchiveUnit(
        ArchiveUnit rootContainerAU,
        DocumentPack documentPack,
        int documentCount,
        List<CompactedFile> compactedFileList,
        int packCount
    ) throws SEDALibException, InterruptedException {
        documentPack.addNewMetadata("DocumentsCount", documentCount);
        documentPack.addNewMetadata("FileObjectsCount", compactedFileList.size());
        Path documentPackFilePath = createDocumentPackArchiveFile(packCount, compactedFileList);
        ArchiveUnit packAu = new ArchiveUnit(dataObjectPackage);
        Content curContent = new Content();
        curContent.addNewMetadata(DESCRIPTION_LEVEL, "Item");
        curContent.addNewMetadata(TITLE, "DocumentPack" + packCount);
        curContent.addMetadata(documentPack);
        packAu.setContent(curContent);
        DataObjectGroup dog = new DataObjectGroup(dataObjectPackage, null);
        BinaryDataObject bdo = new BinaryDataObject(
            dataObjectPackage,
            documentPackFilePath,
            documentPackFilePath.getFileName().toString(),
            "BinaryMaster_1"
        );
        bdo.extractTechnicalElements(null);
        dog.addDataObject(bdo);
        packAu.addDataObjectById(dog.getInDataObjectPackageId());

        rootContainerAU.addChildArchiveUnit(packAu);
        doProgressLog(
            sedaLibProgressLogger,
            SEDALibProgressLogger.OBJECTS_GROUP,
            "  Paquet n°" + packCount + " finalisé",
            null
        );
    }

    public ArchiveUnit doCompact() throws SEDALibException, InterruptedException {
        Path exportPath = Paths.get(workDirectoryName).toAbsolutePath();
        try {
            Files.createDirectories(exportPath);
        } catch (Exception e) {
            throw new SEDALibException(
                "Création du répertoire de création des conteneurs [" + exportPath + "] impossible\n->" + e.getMessage()
            );
        }

        Date d = new Date();
        start = Instant.now();
        String log = "Début du compactage de l'ArchiveUnit [" + archiveUnit.getInDataObjectPackageId() + "]\n";
        log += " avec export des conteneurs dans le répertoire [" + exportPath + "]\n";
        log += " date=" + DateFormat.getDateTimeInstance().format(d);
        doProgressLog(sedaLibProgressLogger, SEDALibProgressLogger.GLOBAL, log, null);

        this.documentsList = new ArrayList<>();
        this.compactedFileListMap = new HashMap<>();

        dataObjectPackage.resetTouchedInDataObjectPackageIdMap();
        dataObjectPackage.resetInOutCounter();
        if (archiveUnit.getContent().getSimpleMetadata(DESCRIPTION_LEVEL).equals("Item")) throw new SEDALibException(
            "Impossible de compacter l'ArchiveUnit à la racine est un Item"
        );
        recurseCompactArchiveUnit(archiveUnit, null);

        // create container root ArchiveUnit
        ArchiveUnit rootContainerAU;
        Content curContent;

        rootContainerAU = new ArchiveUnit(dataObjectPackage);
        curContent = new Content();
        curContent.addNewMetadata(DESCRIPTION_LEVEL, "RecordGrp");
        curContent.addNewMetadata(TITLE, archiveUnit.getContent().getSimpleMetadata(TITLE));
        DocumentContainer documentContainer = new DocumentContainer(documentCounter, fileCounter, rootRecordGrp);
        curContent.addMetadata(documentContainer);
        rootContainerAU.setContent(curContent);

        // create all packs ArchiveUnit
        DocumentPack documentPack = new DocumentPack(rootRecordGrp);
        String xmlString;
        int documentCount = 0;
        int totalDocumentCount = 0;
        int metadataSize = documentPack.toString().length() + 100;
        packCounter = 1;
        List<CompactedFile> compactedFileList = new ArrayList<>();
        for (Document doc : documentsList) {
            xmlString = doc.toString();
            metadataSize += xmlString.length() + xmlString.lines().count() * 4;
            if (
                ((metadataSize > packDocumentsMaxMetadataSize) && (documentCount != 0)) ||
                (documentCount >= packMaxDocumentNumber)
            ) {
                doProgressLog(
                    sedaLibProgressLogger,
                    SEDALibProgressLogger.OBJECTS_GROUP,
                    MODULE + totalDocumentCount + EXPORTED_DOCUMENTS,
                    null
                );
                addDocumentPackArchiveUnit(
                    rootContainerAU,
                    documentPack,
                    documentCount,
                    compactedFileList,
                    packCounter
                );
                documentPack = new DocumentPack(rootRecordGrp);
                documentCount = 0;
                compactedFileList.clear();
                metadataSize = documentPack.toString().length() + 100;
                packCounter++;
            }
            documentPack.addMetadata(doc);
            compactedFileList.addAll(compactedFileListMap.get(doc));
            documentCount++;
            totalDocumentCount++;
            doProgressLogIfStep(
                sedaLibProgressLogger,
                SEDALibProgressLogger.OBJECTS_GROUP,
                totalDocumentCount,
                MODULE + totalDocumentCount + EXPORTED_DOCUMENTS
            );
        }
        doProgressLog(
            sedaLibProgressLogger,
            SEDALibProgressLogger.OBJECTS_GROUP,
            MODULE + totalDocumentCount + EXPORTED_DOCUMENTS,
            null
        );
        addDocumentPackArchiveUnit(rootContainerAU, documentPack, documentCount, compactedFileList, packCounter);

        dataObjectPackage.replaceArchiveUnitBy(archiveUnit, rootContainerAU);
        doProgressLog(
            sedaLibProgressLogger,
            SEDALibProgressLogger.GLOBAL,
            MODULE + "compactage d'une ArchiveUnit terminée.",
            null
        );
        end = Instant.now();
        return rootContainerAU;
    }

    /**
     * Gets the summary of the export process.
     *
     * @return the summary String
     */
    public String getSummary() {
        String result = "Compactage de l'ArchiveUnit [" + archiveUnit.getInDataObjectPackageId() + "]\n";
        result += "  avec " + treenodeCounter + " noeuds dans l'arbre de classement, ";
        result += documentCounter + " documents et ";
        result += totalSubDocumentCounter + " sous-documents\n";
        result += "  dans " + packCounter + " paquets de documents\n";
        if (treenodeWithDroppedNotDesciptiveMetadata + treenodeWithDroppedFile + leafWithDroppedFile > 0) result +=
        "  avec\n";
        if (treenodeWithDroppedNotDesciptiveMetadata != 0) result +=
        "  - " +
        treenodeWithDroppedNotDesciptiveMetadata +
        " noeuds dans l'arbre de classement dont des métadonnées non descriptives ont été éliminées\n";
        if (treenodeWithDroppedFile != 0) result +=
        "  - " + treenodeWithDroppedFile + " noeuds dans l'arbre de classement dont des objets ont été éliminés\n";
        if (leafWithDroppedFile != 0) result +=
        "  - " + leafWithDroppedFile + " documents ou sous-documents dont des objets ont été éliminés\n";
        if (droppedMetadataCounted != 0) result +=
        "  - " + droppedMetadataCounted + " metadonnées de premier rang qui ont été éliminés\n";
        if (truncatedMetadataCounted != 0) result +=
        "  - " + truncatedMetadataCounted + " metadonnées de premier rang qui ont été tronquées\n";
        if ((start != null) && (end != null)) result +=
        "effectué en " + Duration.between(start, end).toString().substring(2) + "\n";
        return result;
    }
}

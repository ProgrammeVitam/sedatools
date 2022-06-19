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
package fr.gouv.vitam.tools.sedalib.process;

import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.core.DataObjectGroup;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;
import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.compacted.*;
import fr.gouv.vitam.tools.sedalib.metadata.content.Content;
import fr.gouv.vitam.tools.sedalib.metadata.data.FileInfo;
import fr.gouv.vitam.tools.sedalib.metadata.data.FormatIdentification;
import fr.gouv.vitam.tools.sedalib.metadata.management.Management;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.DigestType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.IntegerType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.StringType;
import fr.gouv.vitam.tools.sedalib.utils.CompressUtility;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.doProgressLog;
import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.doProgressLogIfStep;

/**
 * The Class DeCompactor.
 * <p>
 * Class for decompacting an ArchiveUnit hierarchy.
 * <p>
 * The origin ArchiveUnit hierarchy is restored with all informations from the reduced hierarchy
 * in the specific metadatas DocumentContainer, DocumentPack, RecordGrp, Document, SubDocument and FileObjects.
 */
public class DeCompactor {

    /**
     * Log and error string finals.
     */
    private static final String MODULE = "sedalib: ";

    /**
     * The metadata tags.
     */
    private static final String TITLE = "Title";

    /**
     * The data Object package containing archive unit to compact.
     */
    private DataObjectPackage dataObjectPackage;

    /**
     * The xml representation of the reference RecordGrp in DocumentContainer.
     */
    private String containerRecordGrpXMLData;

    /**
     * The decompacted ArchiveUnit tree.
     */
    private ArchiveUnit decompactedRootArchiveUnit;

    /**
     * The map between ArchiveUnit tree and node names.
     */

    private Map<String, ArchiveUnit> treeNodeNameArchiveUnitMap;

    /**
     * The expected documents and file objects number in container.
     */
    private long expectedDocumentNumber;
    private long expectedFileObjectNumber;

    /**
     * The cumulated expected documents and file objects number in pack.
     */
    private long cumulatedPackExpectedDocumentNumber;
    private long cumulatedPackExpectedFileObjectNumber;
    /**
     * The processed ArchiveUnit, BinaryDataObject  counters.
     */
    private int archiveUnitCounter;
    private int binaryDataObjectCounter;

    /**
     * The archive unit to compact
     */
    private ArchiveUnit archiveUnit;

    /**
     * The work directory path where to uncompact
     */
    private Path workDirectoryPath;

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
    private DeCompactor(SEDALibProgressLogger sedaLibProgressLogger) {
        this.dataObjectPackage = null;
        this.sedaLibProgressLogger = sedaLibProgressLogger;
        this.decompactedRootArchiveUnit = null;
        this.treeNodeNameArchiveUnitMap = new HashMap<>();
        this.archiveUnitCounter = 0;
        this.binaryDataObjectCounter = 0;
    }

    /**
     * Instantiates a new DeCompactor for an archive unit with a work directory.
     *
     * @param archiveUnit           the archive unit
     * @param workDirectoryName     the work directory name
     * @param sedaLibProgressLogger the progress logger
     */
    public DeCompactor(ArchiveUnit archiveUnit, String workDirectoryName, SEDALibProgressLogger sedaLibProgressLogger) {
        this(sedaLibProgressLogger);
        this.archiveUnit = archiveUnit;
        this.workDirectoryPath = Paths.get(workDirectoryName).toAbsolutePath();
        this.dataObjectPackage = archiveUnit.getDataObjectPackage();
    }

    private RecordGrp extractInformationAndRecordGrpFromDocumentContainer(ArchiveUnit archiveUnit) throws SEDALibException {
        DocumentContainer documentContainer;
        RecordGrp recordGrp = null;

        expectedDocumentNumber = 0;
        expectedFileObjectNumber = 0;

        documentContainer = (DocumentContainer) archiveUnit.getContent().getFirstNamedMetadata("DocumentContainer");
        if (documentContainer == null)
            throw new SEDALibException(MODULE + "pas de DocumentContainer dans l'ArchiveUnit");
        for (SEDAMetadata sm : documentContainer.metadataList) {
            switch (sm.getXmlElementName()) {
                case "DocumentsCount":
                    expectedDocumentNumber = ((IntegerType) sm).getValue();
                    break;
                case "FileObjectsCount":
                    expectedFileObjectNumber = ((IntegerType) sm).getValue();
                    break;
                case "RecordGrp":
                    recordGrp = (RecordGrp) sm;
                    break;
                default:
                    throw new SEDALibException(MODULE + " metadonnée [" + sm.getXmlElementName()
                            + "] inattendue dans le DocumentContainer ["
                            + archiveUnit.getInDataObjectPackageId() + "]", null);
            }
        }
        if (recordGrp == null)
            throw new SEDALibException(MODULE + "pas de RecordGrp dans le DocumentContainer de l'ArchiveUnit ["
                    + archiveUnit.getInDataObjectPackageId() + "]", null);
        return recordGrp;
    }

    private ArchiveUnit constructArchiveUnitTree(RecordGrp recordGrp) throws SEDALibException, InterruptedException {
        ArchiveUnit result;
        String nodeName = null;

        result = new ArchiveUnit(dataObjectPackage);
        for (SEDAMetadata sm : recordGrp.metadataList) {
            switch (sm.getXmlElementName()) {
                case "RecordGrpID":
                    nodeName = ((StringType) sm).getValue();
                    break;
                case "Management":
                    result.setManagement((Management) sm);
                    break;
                case "Content":
                    result.setContent((Content) sm);
                    break;
                case "RecordGrp":
                    result.addChildArchiveUnit(constructArchiveUnitTree((RecordGrp) sm));
                    break;
                default:
                    throw new SEDALibException(MODULE + " metadonnée [" + sm.getXmlElementName()
                            + "] inattendue dans un RecordGrp");
            }
        }
        if (nodeName == null)
            throw new SEDALibException(MODULE + " manque le nom d'un noeud RecordGrp");
        treeNodeNameArchiveUnitMap.put(nodeName, result);
        int counter = dataObjectPackage.getNextInOutCounter();
        doProgressLogIfStep(sedaLibProgressLogger, SEDALibProgressLogger.OBJECTS_GROUP, counter,
                MODULE + counter + " ArchiveUnit decompactées");
        return result;
    }

    private Path extractInformationAndArchiveUnitsFromDocumentPack(ArchiveUnit archiveUnit) throws SEDALibException, InterruptedException {
        DocumentPack documentPack;
        RecordGrp recordGrp = null;
        List<Document> documentList = new ArrayList<>();

        long packExpectedDocumentNumber = 0;
        long packExpectedFileObjectNumber = 0;

        documentPack = (DocumentPack) archiveUnit.getContent().getFirstNamedMetadata("DocumentPack");
        if (documentPack == null)
            throw new SEDALibException(MODULE + "pas de DocumentPack dans l'ArchiveUnit");
        for (SEDAMetadata sm : documentPack.metadataList) {
            switch (sm.getXmlElementName()) {
                case "DocumentsCount":
                    packExpectedDocumentNumber = ((IntegerType) sm).getValue();
                    break;
                case "FileObjectsCount":
                    packExpectedFileObjectNumber = ((IntegerType) sm).getValue();
                    break;
                case "RecordGrp":
                    recordGrp = (RecordGrp) sm;
                    if (!recordGrp.toString().equals(containerRecordGrpXMLData))
                        throw new SEDALibException(MODULE + " le RecordGrp du DocumentPack ["
                                + archiveUnit.getInDataObjectPackageId() + "] est inconsistant avec celui du DocumentContainer");
                    break;
                case "Document":
                    documentList.add((Document) sm);
                    break;
                default:
                    throw new SEDALibException(MODULE + " metadonnée [" + sm.getXmlElementName()
                            + "] inattendue dans le DocumentPack ["
                            + archiveUnit.getInDataObjectPackageId() + "]", null);
            }
        }
        if (recordGrp == null)
            throw new SEDALibException(MODULE + "pas de RecordGrp dans le DocumentPack de l'ArchiveUnit");
        if (documentList.size() != packExpectedDocumentNumber)
            doProgressLog(sedaLibProgressLogger, SEDALibProgressLogger.GLOBAL,
                    MODULE + "le nombre de Documents est incohérent [prévus " + packExpectedDocumentNumber
                            + " trouvés " + documentList.size() + "] dans le DocumentPack ["
                            + archiveUnit.getInDataObjectPackageId() + "]", null);
        if (archiveUnit.getContent().getSimpleMetadata(TITLE) == null)
            throw new SEDALibException(MODULE + "pas de Title dans l'ArchiveUnit ["
                    + archiveUnit.getInDataObjectPackageId() + "]");
        Path documentDirPath = workDirectoryPath.resolve(archiveUnit.getContent().getSimpleMetadata(TITLE)
                + "-" + archiveUnit.getInDataObjectPackageId());
        for (Document document : documentList)
            extractArchiveUnitsFromDocument(document, documentDirPath);

        cumulatedPackExpectedDocumentNumber += packExpectedDocumentNumber;
        cumulatedPackExpectedFileObjectNumber += packExpectedFileObjectNumber;
        return documentDirPath;
    }

    public void extractArchiveUnitsFromDocument(Document document, Path documentDirPath) throws SEDALibException {
        String recordGrpID = null;
        ArchiveUnit newArchiveUnit = new ArchiveUnit(dataObjectPackage);
        archiveUnitCounter++;
        DataObjectGroup dataObjectGroup = new DataObjectGroup(dataObjectPackage, null);

        for (SEDAMetadata sm : document.metadataList) {
            switch (sm.getXmlElementName()) {
                case "RecordGrpID":
                    recordGrpID = ((StringType) sm).getValue();
                    break;
                case "Content":
                    newArchiveUnit.setContent((Content) sm);
                    break;
                case "Management":
                    newArchiveUnit.setManagement((Management) sm);
                    break;
                case "FileObject":
                    BinaryDataObject bdo = new BinaryDataObject(dataObjectPackage);
                    bdo.dataObjectVersion = (StringType) ((FileObject) sm).getFirstNamedMetadata("DataObjectVersion");
                    bdo.uri = (StringType) ((FileObject) sm).getFirstNamedMetadata("URI");
                    bdo.messageDigest = (DigestType) ((FileObject) sm).getFirstNamedMetadata("MessageDigest");
                    bdo.size = (IntegerType) ((FileObject) sm).getFirstNamedMetadata("Size");
                    bdo.formatIdentification = (FormatIdentification) ((FileObject) sm).getFirstNamedMetadata("FormatIdentification");
                    bdo.fileInfo = (FileInfo) ((FileObject) sm).getFirstNamedMetadata("FileInfo");
                    bdo.setOnDiskPath(documentDirPath.resolve(bdo.uri.getValue()));
                    dataObjectGroup.addDataObject(bdo);
                    binaryDataObjectCounter++;
                    break;
                case "SubDocument":
                    newArchiveUnit.addChildArchiveUnit(extractArchiveUnitsFromSubDocument((SubDocument) sm, documentDirPath));
                    break;
                default:
                    throw new SEDALibException(MODULE + " metadonnée [" + sm.getXmlElementName()
                            + "] inattendue dans le Document");
            }
        }
        if (recordGrpID == null)
            throw new SEDALibException(MODULE + " le document [" + document.getSimpleMetadata(TITLE)
                    + "] n'a pas de lien avec l'arbre de positionnement");
        if (!dataObjectGroup.getBinaryDataObjectList().isEmpty())
            newArchiveUnit.addDataObjectById(dataObjectGroup.getInDataObjectPackageId());
        ArchiveUnit treeArchiveUnit = treeNodeNameArchiveUnitMap.get(recordGrpID);
        if (treeArchiveUnit == null)
            throw new SEDALibException(MODULE + "mauvaise référence à l'arbre de positionnement du document ["
                    + document.getSimpleMetadata(TITLE) + "]");
        treeArchiveUnit.addChildArchiveUnit(newArchiveUnit);
    }

    public ArchiveUnit extractArchiveUnitsFromSubDocument(SubDocument subDocument, Path documentDirPath) throws SEDALibException {
        ArchiveUnit newArchiveUnit = new ArchiveUnit(dataObjectPackage);
        archiveUnitCounter++;
        DataObjectGroup dataObjectGroup = new DataObjectGroup(dataObjectPackage, null);

        for (SEDAMetadata sm : subDocument.metadataList) {
            switch (sm.getXmlElementName()) {
                case "Content":
                    newArchiveUnit.setContent((Content) sm);
                    break;
                case "Management":
                    newArchiveUnit.setManagement((Management) sm);
                    break;
                case "FileObject":
                    BinaryDataObject bdo = new BinaryDataObject(dataObjectPackage);
                    bdo.dataObjectVersion = (StringType) ((FileObject) sm).getFirstNamedMetadata("DataObjectVersion");
                    bdo.uri = (StringType) ((FileObject) sm).getFirstNamedMetadata("URI");
                    bdo.messageDigest = (DigestType) ((FileObject) sm).getFirstNamedMetadata("MessageDigest");
                    bdo.size = (IntegerType) ((FileObject) sm).getFirstNamedMetadata("Size");
                    bdo.formatIdentification = (FormatIdentification) ((FileObject) sm).getFirstNamedMetadata("FormatIdentification");
                    bdo.fileInfo = (FileInfo) ((FileObject) sm).getFirstNamedMetadata("FileInfo");
                    bdo.setOnDiskPath(documentDirPath.resolve(bdo.uri.getValue()));
                    dataObjectGroup.addDataObject(bdo);
                    binaryDataObjectCounter++;
                    break;
                case "SubDocument":
                    newArchiveUnit.addChildArchiveUnit(extractArchiveUnitsFromSubDocument((SubDocument) sm, documentDirPath));
                    break;
                default:
                    throw new SEDALibException(MODULE + " metadonnée [" + sm.getXmlElementName()
                            + "] inattendue dans le Document");
            }
        }
        if (!dataObjectGroup.getBinaryDataObjectList().isEmpty())
            newArchiveUnit.addDataObjectById(dataObjectGroup.getInDataObjectPackageId());
        return newArchiveUnit;
    }

    public ArchiveUnit doDeCompact() throws SEDALibException, InterruptedException {
        try {
            Files.createDirectories(workDirectoryPath);
        } catch (Exception e) {
            throw new SEDALibException(
                    "Création du répertoire de décompression des conteneurs [" + workDirectoryPath + "] impossible\n->" + e.getMessage());
        }

        Date d = new Date();
        start = Instant.now();
        String log = "Début du decompactage de l'ArchiveUnit [" + archiveUnit.getInDataObjectPackageId() + "]\n";
        log += " avec export des fichiers dans le répertoire [" + workDirectoryPath + "]\n";
        log += " date=" + DateFormat.getDateTimeInstance().format(d);
        doProgressLog(sedaLibProgressLogger, SEDALibProgressLogger.GLOBAL, log, null);

        archiveUnitCounter = 0;
        binaryDataObjectCounter = 0;
        cumulatedPackExpectedDocumentNumber = 0;
        cumulatedPackExpectedFileObjectNumber = 0;
        dataObjectPackage.resetInOutCounter();
        RecordGrp tree = extractInformationAndRecordGrpFromDocumentContainer(archiveUnit);
        containerRecordGrpXMLData = tree.toString();
        decompactedRootArchiveUnit = constructArchiveUnitTree(tree);
        containerRecordGrpXMLData = tree.toString();

        for (ArchiveUnit packAU : archiveUnit.getChildrenAuList().getArchiveUnitList()) {
            Path packDirPath = extractInformationAndArchiveUnitsFromDocumentPack(packAU);
            if ((packAU.getTheDataObjectGroup() == null) ||
                    (packAU.getTheDataObjectGroup().getBinaryDataObjectList().isEmpty()))
                throw new SEDALibException(MODULE + "pas de fichier à décompresser dans l'ArchiveUnit du DocumentPack ["
                        + packAU.getInDataObjectPackageId() + "]");
            CompressUtility compressUtility = new CompressUtility(packAU.getTheDataObjectGroup().getBinaryDataObjectList().get(0).getOnDiskPath(), packDirPath, "UTF-8", sedaLibProgressLogger);
            compressUtility.unCompress();
        }

        if (cumulatedPackExpectedDocumentNumber != expectedDocumentNumber)
            doProgressLog(sedaLibProgressLogger, SEDALibProgressLogger.GLOBAL, MODULE
                    + " Erreur: le nombre de documents décompactés n'est pas celui prévu dans le DocumentContainer", null);
        if (cumulatedPackExpectedFileObjectNumber != expectedFileObjectNumber)
            doProgressLog(sedaLibProgressLogger, SEDALibProgressLogger.GLOBAL, MODULE
                    + " Erreur: le nombre de fichiers décompactés n'est pas celui prévu dans le DocumentContainer", null);

        dataObjectPackage.replaceArchiveUnitBy(archiveUnit, decompactedRootArchiveUnit);
        doProgressLog(sedaLibProgressLogger, SEDALibProgressLogger.GLOBAL,
                MODULE + "compactage d'une ArchiveUnit terminée.", null);
        end = Instant.now();
        return decompactedRootArchiveUnit;
    }

    /**
     * Gets the summary of the export process.
     *
     * @return the summary String
     */
    public String getSummary() {
        String result = "Decompactage de l'ArchiveUnit [" + archiveUnit.getInDataObjectPackageId() + "]\n";
        result += "  avec " + expectedDocumentNumber + " documents et "
                + expectedFileObjectNumber + " fichiers\n";
        result += "  développé en " + archiveUnitCounter + " ArchiveUnit(s) et "
                + binaryDataObjectCounter + " BinaryDataObjects\n";
        if ((start != null) && (end != null))
            result += "effectué en " + Duration.between(start, end).toString().substring(2) + "\n";
        return result;
    }
}

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
package fr.gouv.vitam.tools.resip.service;

import fr.gouv.vitam.tools.mailextractlib.core.StoreExtractor;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractProgressLogger;
import fr.gouv.vitam.tools.resip.data.Work;
import fr.gouv.vitam.tools.resip.inout.MailImporter;
import fr.gouv.vitam.tools.resip.parameters.DiskImportContext;
import fr.gouv.vitam.tools.resip.parameters.ExportContext;
import fr.gouv.vitam.tools.resip.parameters.MailImportContext;
import fr.gouv.vitam.tools.resip.parameters.Preferences;
import fr.gouv.vitam.tools.resip.parameters.ZipImportContext;
import fr.gouv.vitam.tools.resip.utils.ResipException;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.core.DataObject;
import fr.gouv.vitam.tools.sedalib.core.DataObjectGroup;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;
import fr.gouv.vitam.tools.sedalib.inout.importer.CompressedFileToArchiveTransferImporter;
import fr.gouv.vitam.tools.sedalib.inout.importer.DiskToArchiveTransferImporter;
import fr.gouv.vitam.tools.sedalib.inout.importer.DiskToDataObjectPackageImporter;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;
import fr.gouv.vitam.tools.sedaschema.SedaVersion;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.GLOBAL;
import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.doProgressLog;

/**
 * Adds content to a work, without any user interface: dropped files, as a new work or under an
 * ArchiveUnit, and the expansion of a compressed file or a mail container.
 * <p>
 * The content is first read in a package of its own, which can be done in the background, then
 * added to the work, to be done where the work is shown.
 */
public class AddService {

    /**
     * The creation context source of a work made of dropped files.
     */
    public static final String DRAG_AND_DROP = "DragAndDrop";

    private static final String ARCHIVE_UNIT_METADATA_FILENAME = "__ArchiveUnitMetadata.xml";

    private final Preferences preferences;
    private final TmpDirResolver tmpDirResolver;

    /**
     * Instantiates a new add service.
     *
     * @param preferences    the preferences, giving the default import contexts
     * @param tmpDirResolver the choice of the temporary directory when expanding a file
     */
    public AddService(Preferences preferences, TmpDirResolver tmpDirResolver) {
        this.preferences = preferences;
        this.tmpDirResolver = tmpDirResolver;
    }

    /**
     * Makes a new work of dropped files.
     *
     * @param paths       the dropped files and directories
     * @param sedaVersion the SEDA version of the new work
     * @param spl         the progress logger, or null
     * @return the new work
     * @throws SEDALibException     if the files can't be imported
     * @throws InterruptedException if the import is interrupted
     */
    public Work importNewWork(List<Path> paths, SedaVersion sedaVersion, SEDALibProgressLogger spl)
        throws SEDALibException, InterruptedException {
        DiskImportContext diskImportContext = new DiskImportContext(preferences);
        DiskToDataObjectPackageImporter di = importPaths(paths, diskImportContext, sedaVersion, spl);
        diskImportContext.setModelVersion(di.getModelVersion());
        diskImportContext.setOnDiskInput(DRAG_AND_DROP);
        diskImportContext.setSummary(di.getSummary());
        return new ImportResult(di.getDataObjectPackage(), null, di.getSummary()).toWork(
            diskImportContext,
            new ExportContext(preferences)
        );
    }

    /**
     * Reads dropped files to be added to a work, in its SEDA version and with its disk import
     * options, or the default ones if it was not imported from disk.
     *
     * @param work  the work the files are added to
     * @param paths the dropped files and directories
     * @param spl   the progress logger, or null
     * @return the read package, to be added by {@link #addTo(Work, ArchiveUnit, DataObjectPackage)}
     * @throws SEDALibException     if the files can't be imported
     * @throws InterruptedException if the import is interrupted
     */
    public ImportResult importFiles(Work work, List<Path> paths, SEDALibProgressLogger spl)
        throws SEDALibException, InterruptedException {
        DiskImportContext diskImportContext = work.getCreationContext() instanceof DiskImportContext
            ? (DiskImportContext) work.getCreationContext()
            : new DiskImportContext(preferences);
        DiskToDataObjectPackageImporter di = importPaths(
            paths,
            diskImportContext,
            work.getDataObjectPackage().getSedaVersion(),
            spl
        );
        return new ImportResult(di.getDataObjectPackage(), null, di.getSummary());
    }

    private static DiskToDataObjectPackageImporter importPaths(
        List<Path> paths,
        DiskImportContext diskImportContext,
        SedaVersion sedaVersion,
        SEDALibProgressLogger spl
    ) throws SEDALibException, InterruptedException {
        DiskToDataObjectPackageImporter di = new DiskToDataObjectPackageImporter(
            sedaVersion,
            paths,
            diskImportContext.isNoLinkFlag(),
            null,
            spl
        );
        for (String ip : diskImportContext.getIgnorePatternList()) di.addIgnorePattern(ip);
        di.doImport();
        return di;
    }

    /**
     * @param bdo a BinaryDataObject to expand
     * @return the subdirectory of the work directory its content is extracted in
     */
    public static String tmpSubDirOf(BinaryDataObject bdo) {
        return (
            Paths.get(bdo.getOnDiskPathToString()).getFileName().toString() +
            "-" +
            bdo.getInDataObjectPackageId() +
            "-tmpdir"
        );
    }

    /**
     * Reads the content of a compressed BinaryDataObject, in its SEDA version.
     *
     * @param bdo the compressed BinaryDataObject
     * @param spl the progress logger, or null
     * @return the read package, to be added by
     * {@link #replaceByExpansion(Work, ArchiveUnit, BinaryDataObject, DataObjectPackage, SEDALibProgressLogger)}
     * @throws ResipException       if the expansion is abandoned
     * @throws SEDALibException     if the file can't be read
     * @throws InterruptedException if the expansion is interrupted
     */
    public ImportResult expandCompressedFile(BinaryDataObject bdo, SEDALibProgressLogger spl)
        throws ResipException, SEDALibException, InterruptedException {
        logExpansion("Expansion", bdo, spl);
        ZipImportContext zic = new ZipImportContext(preferences);
        String target = tmpDirResolver.resolve(zic.getWorkDir(), tmpSubDirOf(bdo));
        CompressedFileToArchiveTransferImporter zi = new CompressedFileToArchiveTransferImporter(
            bdo.getSedaVersion(),
            bdo.getOnDiskPathToString(),
            target,
            null,
            null,
            spl
        );
        for (String ip : zic.getIgnorePatternList()) zi.addIgnorePattern(ip);
        zi.doImport();
        return new ImportResult(zi.getArchiveTransfer().getDataObjectPackage(), null, zi.getSummary());
    }

    /**
     * Reads the messages of a mail container BinaryDataObject, in its SEDA version.
     *
     * @param bdo  the mail container BinaryDataObject
     * @param spl  the progress logger, or null
     * @param mepl the progress logger of the mail extraction
     * @return the read package, to be added by
     * {@link #replaceByExpansion(Work, ArchiveUnit, BinaryDataObject, DataObjectPackage, SEDALibProgressLogger)}
     * @throws ResipException       if the extraction is abandoned or its result can't be listed
     * @throws SEDALibException     if the messages can't be read
     * @throws InterruptedException if the extraction is interrupted
     */
    public ImportResult extractMails(BinaryDataObject bdo, SEDALibProgressLogger spl, MailExtractProgressLogger mepl)
        throws ResipException, SEDALibException, InterruptedException {
        Objects.requireNonNull(mepl, "Le journal de l'extraction de courriels est obligatoire");
        logExpansion("Extraction de messages", bdo, spl);
        MailImportContext mic = new MailImportContext(preferences);
        String target = tmpDirResolver.resolve(mic.getWorkDir(), tmpSubDirOf(bdo));
        MailImporter mi = new MailImporter(
            mic.isExtractMessageTextFile(),
            mic.isExtractMessageTextMetadata(),
            mic.isExtractAttachmentTextFile(),
            mic.isExtractAttachmentTextMetadata(),
            StoreExtractor.getProtocolFromDroidFormat(
                bdo.getMetadataFormatIdentification().getSimpleMetadata("FormatId")
            ),
            mic.getDefaultCharsetName(),
            bdo.getOnDiskPathToString(),
            "",
            target,
            mepl
        );
        mi.doExtract();
        doProgressLog(spl, GLOBAL, "resip: extraction de messages terminée\n" + mi.getSummary(), null);

        List<Path> lp = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(mi.getTarget()))) {
            for (Path path : stream) if (!path.toString().endsWith(ARCHIVE_UNIT_METADATA_FILENAME)) lp.add(path);
        } catch (IOException e) {
            throw new ResipException("Impossible de lister les messages extraits en [" + mi.getTarget() + "]", e);
        }
        DiskToArchiveTransferImporter di = new DiskToArchiveTransferImporter(bdo.getSedaVersion(), lp, spl);
        for (String ip : new DiskImportContext(preferences).getIgnorePatternList()) di.addIgnorePattern(ip);
        di.doImport();
        return new ImportResult(
            di.getArchiveTransfer().getDataObjectPackage(),
            null,
            mi.getSummary() + "\n" + di.getSummary()
        );
    }

    private static void logExpansion(String operation, BinaryDataObject bdo, SEDALibProgressLogger spl)
        throws InterruptedException {
        doProgressLog(
            spl,
            GLOBAL,
            operation +
            " du BinaryDataObject " +
            bdo.getInDataObjectPackageId() +
            ", fichier [" +
            bdo.getMetadataFileInfo().getSimpleMetadata("Filename") +
            "]",
            null
        );
    }

    /**
     * Adds a read package under an ArchiveUnit of a work.
     *
     * @param work     the work
     * @param target   the ArchiveUnit of the work the content is added under
     * @param imported the read package, emptied by the addition
     * @return the added ArchiveUnits, the roots of the read package
     */
    public List<ArchiveUnit> addTo(Work work, ArchiveUnit target, DataObjectPackage imported) {
        List<ArchiveUnit> added = new ArrayList<>(imported.getGhostRootAu().getChildrenAuList().getArchiveUnitList());
        target.getDataObjectPackage().moveContentFromDataObjectPackage(imported, target);
        work.getCreationContext().setStructureChanged(true);
        return added;
    }

    /**
     * Adds the expanded content of a BinaryDataObject under its ArchiveUnit, in place of it: the
     * ArchiveUnit becomes a RecordGrp when it has no more DataObject.
     *
     * @param work     the work
     * @param target   the ArchiveUnit of the expanded BinaryDataObject
     * @param expanded the expanded BinaryDataObject
     * @param imported the expanded content, emptied by the addition
     * @param spl      the progress logger, or null
     * @return the added ArchiveUnits, the roots of the expanded content
     */
    public List<ArchiveUnit> replaceByExpansion(
        Work work,
        ArchiveUnit target,
        BinaryDataObject expanded,
        DataObjectPackage imported,
        SEDALibProgressLogger spl
    ) {
        List<ArchiveUnit> added = addTo(work, target, imported);
        DataObject dataObject = target.getDataObjectRefList().getDataObjectList().get(0);
        if (dataObject instanceof DataObjectGroup) {
            DataObjectGroup dog = (DataObjectGroup) dataObject;
            dog.removeDataObject(expanded);
            if (
                ((dog.getPhysicalDataObjectList() == null) || (dog.getPhysicalDataObjectList().isEmpty())) &&
                dog.getBinaryDataObjectList().isEmpty()
            ) {
                target.removeEmptyDataObjectGroup();
                try {
                    target.getContent().addNewMetadata("DescriptionLevel", "RecordGrp");
                } catch (SEDALibException ignored) {
                    // the ArchiveUnit keeps its description level
                }
            }
        }
        try {
            target.getDataObjectPackage().removeUnusedDataObjects(spl);
        } catch (InterruptedException e) {
            // the content is added, only the cleaning is cut short
            Thread.currentThread().interrupt();
        }
        return added;
    }
}

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

import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractProgressLogger;
import fr.gouv.vitam.tools.resip.data.Work;
import fr.gouv.vitam.tools.resip.inout.MailImporter;
import fr.gouv.vitam.tools.resip.parameters.*;
import fr.gouv.vitam.tools.resip.utils.ResipException;
import fr.gouv.vitam.tools.sedalib.core.ArchiveDeliveryRequestReply;
import fr.gouv.vitam.tools.sedalib.core.ArchiveTransfer;
import fr.gouv.vitam.tools.sedalib.inout.importer.*;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;
import fr.gouv.vitam.tools.sedaschema.SedaVersion;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.GLOBAL;
import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.doProgressLog;

/**
 * Imports a work from a creation context, whatever its kind, without any user interface: the
 * graphic application and the command line both import through it.
 */
public class ImportService {

    /**
     * Extracts in the temporary directory of the imported file, as it is: the choice of the
     * command line.
     */
    public static final TmpDirResolver USE_AS_IS = (workDir, subDir) -> workDir + File.separator + subDir;

    private final Preferences preferences;
    private final TmpDirResolver tmpDirResolver;

    /**
     * Instantiates a new import service.
     *
     * @param preferences    the preferences, giving the default export context and the ignore
     *                       patterns of a mail import
     * @param tmpDirResolver the choice of the temporary directory when extracting a file
     */
    public ImportService(Preferences preferences, TmpDirResolver tmpDirResolver) {
        this.preferences = preferences;
        this.tmpDirResolver = tmpDirResolver;
    }

    /**
     * @param sourcePath the path of the imported file
     * @return the subdirectory of the work directory this file is extracted in
     */
    public static String tmpSubDirOf(String sourcePath) {
        return Paths.get(sourcePath).getFileName().toString() + "-tmpdir";
    }

    /**
     * Imports the package a creation context designates.
     *
     * @param creationContext the creation context, which kind gives the kind of import
     * @param sedaVersion     the SEDA version of the package, but for a SIP or a DIP which is
     *                        read in the version of its manifest
     * @param spl             the progress logger, or null
     * @param mepl            the progress logger of a mail import, mandatory for it only
     * @return the import result
     * @throws ResipException       if the kind of import is unknown or the import abandoned
     * @throws SEDALibException     if the import fails
     * @throws InterruptedException if the import is interrupted
     */
    public ImportResult doImport(
        CreationContext creationContext,
        SedaVersion sedaVersion,
        SEDALibProgressLogger spl,
        MailExtractProgressLogger mepl
    ) throws ResipException, SEDALibException, InterruptedException {
        // a zip import context is a disk one, so it is tested first
        if (creationContext instanceof ZipImportContext) return doZipImport(
            (ZipImportContext) creationContext,
            sedaVersion,
            spl
        );
        if (creationContext instanceof DiskImportContext) return doDiskImport(
            (DiskImportContext) creationContext,
            sedaVersion,
            spl
        );
        if (creationContext instanceof SIPImportContext) return doSIPImport((SIPImportContext) creationContext, spl);
        if (creationContext instanceof DIPImportContext) return doDIPImport((DIPImportContext) creationContext, spl);
        if (creationContext instanceof CSVTreeImportContext) return doCSVTreeImport(
            (CSVTreeImportContext) creationContext,
            sedaVersion,
            spl
        );
        if (creationContext instanceof CSVMetadataImportContext) return doCSVMetadataImport(
            (CSVMetadataImportContext) creationContext,
            sedaVersion,
            spl
        );
        if (creationContext instanceof TmpDirImportContext) return doTmpDirImport(
            (TmpDirImportContext) creationContext,
            sedaVersion,
            spl
        );
        if (creationContext instanceof MailImportContext) return doMailImport(
            (MailImportContext) creationContext,
            sedaVersion,
            spl,
            mepl
        );
        throw new ResipException(
            "Type d'import inconnu [" +
            (creationContext == null ? null : creationContext.getClass().getSimpleName()) +
            "]"
        );
    }

    /**
     * Imports a work, as the graphic application does: the package is checked and normalised for
     * Vitam, and the summary of the import kept in the creation context.
     *
     * @param creationContext the creation context, which kind gives the kind of import
     * @param sedaVersion     the SEDA version of the package, but for a SIP or a DIP which is
     *                        read in the version of its manifest
     * @param spl             the progress logger, or null
     * @param mepl            the progress logger of a mail import, mandatory for it only
     * @return the imported work
     * @throws ResipException       if the kind of import is unknown or the import abandoned
     * @throws SEDALibException     if the import fails
     * @throws InterruptedException if the import is interrupted
     */
    public Work importWork(
        CreationContext creationContext,
        SedaVersion sedaVersion,
        SEDALibProgressLogger spl,
        MailExtractProgressLogger mepl
    ) throws ResipException, SEDALibException, InterruptedException {
        ImportResult result = doImport(creationContext, sedaVersion, spl, mepl);
        Work work = result.toWork(creationContext, new ExportContext(preferences));
        creationContext.setSummary(result.getSummary() + "\n" + work.doVitamNormalize(spl));
        return work;
    }

    private static ImportResult of(ArchiveTransfer archiveTransfer, String summary) {
        return new ImportResult(archiveTransfer.getDataObjectPackage(), archiveTransfer.getGlobalMetadata(), summary);
    }

    private ImportResult doZipImport(ZipImportContext zic, SedaVersion sedaVersion, SEDALibProgressLogger spl)
        throws ResipException, InterruptedException, SEDALibException {
        String target = tmpDirResolver.resolve(zic.getWorkDir(), tmpSubDirOf(zic.getOnDiskInput()));
        //TODO add preferences for compressed filename import
        String encoding = zic.getOnDiskInput().endsWith("zip") ? "CP850" : "UTF8";
        CompressedFileToArchiveTransferImporter zi = new CompressedFileToArchiveTransferImporter(
            sedaVersion,
            zic.getOnDiskInput(),
            target,
            encoding,
            null,
            spl
        );
        for (String ip : zic.getIgnorePatternList()) zi.addIgnorePattern(ip);
        zi.doImport();
        return of(zi.getArchiveTransfer(), zi.getSummary());
    }

    private ImportResult doDiskImport(DiskImportContext dic, SedaVersion sedaVersion, SEDALibProgressLogger spl)
        throws SEDALibException, InterruptedException {
        DiskToArchiveTransferImporter di = new DiskToArchiveTransferImporter(
            sedaVersion,
            dic.getOnDiskInput(),
            dic.isNoLinkFlag(),
            null,
            spl
        );
        for (String ip : dic.getIgnorePatternList()) di.addIgnorePattern(ip);
        di.doImport();
        dic.setModelVersion(di.getModelVersion());
        return of(di.getArchiveTransfer(), di.getSummary());
    }

    private ImportResult doSIPImport(SIPImportContext sic, SEDALibProgressLogger spl)
        throws SEDALibException, InterruptedException, ResipException {
        String target = tmpDirResolver.resolve(sic.getWorkDir(), tmpSubDirOf(sic.getOnDiskInput()));
        SIPToArchiveTransferImporter si = new SIPToArchiveTransferImporter(sic.getOnDiskInput(), target, spl);
        si.doImport();
        return of(si.getArchiveTransfer(), si.getSummary());
    }

    private ImportResult doDIPImport(DIPImportContext dic, SEDALibProgressLogger spl)
        throws ResipException, InterruptedException, SEDALibException {
        String target = tmpDirResolver.resolve(dic.getWorkDir(), tmpSubDirOf(dic.getOnDiskInput()));
        DIPToArchiveDeliveryRequestReplyImporter di = new DIPToArchiveDeliveryRequestReplyImporter(
            dic.getOnDiskInput(),
            target,
            spl
        );
        di.doImport();
        ArchiveDeliveryRequestReply reply = di.getArchiveDeliveryRequestReply();
        return new ImportResult(reply.getDataObjectPackage(), reply.getGlobalMetadata(), di.getSummary());
    }

    private ImportResult doCSVTreeImport(CSVTreeImportContext ctic, SedaVersion sedaVersion, SEDALibProgressLogger spl)
        throws SEDALibException, InterruptedException {
        CSVTreeToDataObjectPackageImporter cti = new CSVTreeToDataObjectPackageImporter(
            sedaVersion,
            ctic.getOnDiskInput(),
            ctic.getCsvCharsetName(),
            ctic.getDelimiter(),
            spl
        );
        cti.doImport();
        return new ImportResult(cti.getDataObjectPackage(), null, cti.getSummary());
    }

    private ImportResult doCSVMetadataImport(
        CSVMetadataImportContext cmic,
        SedaVersion sedaVersion,
        SEDALibProgressLogger spl
    ) throws SEDALibException, InterruptedException {
        CSVMetadataToDataObjectPackageImporter cmi = new CSVMetadataToDataObjectPackageImporter(
            sedaVersion,
            cmic.getOnDiskInput(),
            cmic.getCsvCharsetName(),
            cmic.getDelimiter(),
            spl
        );
        cmi.doImport();
        return new ImportResult(cmi.getDataObjectPackage(), null, cmi.getSummary());
    }

    /**
     * Finds the manifest a SIP or a DIP import extracted in a directory, the one whose name holds
     * "manifest" as in the importers.
     *
     * @param dir the directory
     * @return the manifest, or null if there is none
     * @throws ResipException if there are several
     */
    public static File findManifest(File dir) throws ResipException {
        File manifest = null;
        File[] files = dir.listFiles();
        if (files != null) for (File f : files) {
            if (f.isFile() && f.getName().toLowerCase().matches(".*manifest.*\\.xml")) {
                if (manifest != null) throw new ResipException(
                    "Plusieurs fichiers manifest potentiels dans le répertoire [" + dir + "]"
                );
                manifest = f;
            }
        }
        return manifest;
    }

    // the root element tells a SIP manifest from a DIP one
    private static String rootElementOf(File manifest) throws ResipException {
        try (InputStream is = new FileInputStream(manifest)) {
            XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(is);
            try {
                while (reader.hasNext()) {
                    XMLEvent event = reader.nextEvent();
                    if (event.isStartElement()) return event.asStartElement().getName().getLocalPart();
                }
            } finally {
                reader.close();
            }
        } catch (IOException | XMLStreamException e) {
            throw new ResipException("Impossible de lire le manifest [" + manifest + "]", e);
        }
        throw new ResipException("Le manifest [" + manifest + "] est vide");
    }

    private ImportResult doTmpDirImport(TmpDirImportContext tdic, SedaVersion sedaVersion, SEDALibProgressLogger spl)
        throws ResipException, InterruptedException, SEDALibException {
        File dir = new File(tdic.getOnDiskInput());
        if (!dir.isDirectory()) throw new ResipException("Le répertoire [" + dir + "] n'existe pas");
        File manifest = findManifest(dir);
        if (manifest == null) {
            // the links are ignored as when the compressed file was imported, they point outside
            DiskImportContext dic = new DiskImportContext(
                new DiskImportContext(preferences).getIgnorePatternList(),
                true,
                tdic.getOnDiskInput(),
                tdic.getWorkDir()
            );
            return doDiskImport(dic, sedaVersion, spl);
        }
        String rootDir = dir.getAbsolutePath();
        try (
            FileInputStream fis = new FileInputStream(manifest);
            SEDAXMLEventReader xmlReader = new SEDAXMLEventReader(fis)
        ) {
            if ("ArchiveDeliveryRequestReply".equals(rootElementOf(manifest))) {
                ArchiveDeliveryRequestReply reply = ArchiveDeliveryRequestReply.fromSedaXml(xmlReader, rootDir, spl);
                return new ImportResult(
                    reply.getDataObjectPackage(),
                    reply.getGlobalMetadata(),
                    "DIP extrait réimporté depuis [" + dir + "]\n" + reply.getDescription()
                );
            }
            ArchiveTransfer archiveTransfer = ArchiveTransfer.fromSedaXml(xmlReader, rootDir, spl);
            return of(
                archiveTransfer,
                "SIP extrait réimporté depuis [" + dir + "]\n" + archiveTransfer.getDescription()
            );
        } catch (IOException | XMLStreamException e) {
            throw new SEDALibException("Impossible d'importer le fichier [" + manifest + "] comme manifest", e);
        }
    }

    private ImportResult doMailImport(
        MailImportContext mic,
        SedaVersion sedaVersion,
        SEDALibProgressLogger spl,
        MailExtractProgressLogger mepl
    ) throws ResipException, InterruptedException, SEDALibException {
        Objects.requireNonNull(mepl, "Le journal de l'extraction de courriels est obligatoire");
        String target = tmpDirResolver.resolve(mic.getWorkDir(), tmpSubDirOf(mic.getOnDiskInput()));
        MailImporter mi = new MailImporter(
            mic.isExtractMessageTextFile(),
            mic.isExtractMessageTextMetadata(),
            mic.isExtractAttachmentTextFile(),
            mic.isExtractAttachmentTextMetadata(),
            mic.getProtocol(),
            mic.getDefaultCharsetName(),
            mic.getOnDiskInput(),
            mic.getMailFolder(),
            target,
            mepl
        );
        mi.doExtract();
        doProgressLog(spl, GLOBAL, "resip: extraction terminée\n" + mi.getSummary(), null);

        List<Path> lp = List.of(Paths.get(mi.getTarget()));
        DiskToArchiveTransferImporter di = new DiskToArchiveTransferImporter(sedaVersion, lp, spl);
        for (String ip : new DiskImportContext(preferences).getIgnorePatternList()) di.addIgnorePattern(ip);
        di.doImport();
        return of(di.getArchiveTransfer(), mi.getSummary() + "\n" + di.getSummary());
    }
}

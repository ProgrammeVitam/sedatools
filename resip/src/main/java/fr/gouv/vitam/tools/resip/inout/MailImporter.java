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
/*
 *
 */
package fr.gouv.vitam.tools.resip.inout;

import fr.gouv.vitam.tools.mailextractlib.core.StoreExtractor;
import fr.gouv.vitam.tools.mailextractlib.core.StoreExtractorOptions;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractProgressLogger;
import fr.gouv.vitam.tools.resip.app.ResipGraphicApp;
import fr.gouv.vitam.tools.resip.utils.ResipException;
import fr.gouv.vitam.tools.resip.utils.ResipLogger;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;

/**
 * The Class MailImporter.
 */
public class MailImporter {

    /**
     * The end.
     */
    Instant start, /**
     * The End.
     */end;

    /**
     * The protocol.
     */
    String protocol;

    /**
     * The container.
     */
    String container;

    /**
     * The url string.
     */
    String urlString;

    /**
     * The mailfolder.
     */
    String mailfolder;

    /**
     * The target.
     */
    String target;

    /**
     * The summary.
     */
    String summary;

    /**
     * The store extractor options.
     */
    StoreExtractorOptions storeExtractorOptions;

    /**
     * The store extractor.
     */
    StoreExtractor storeExtractor;

    /**
     * The MailExtract progress logger.
     */
    MailExtractProgressLogger mailExtractProgressLogger;

    /**
     * Instantiates a new mail importer.
     *
     * @param extractMessageTextFile     the extract message text file
     * @param extractMessageTextMetadata the extract message text metadata
     * @param extractAttachmentTextFile  the extract attachment text file
     * @param extractAttachmentMetadata  the extract attachment metadata
     * @param protocol                   the protocol
     * @param defaultCharsetName         the default charset name
     * @param container                  the container
     * @param mailfolder                 the mailfolder
     * @param target                     the work dir
     * @param mailExtractProgressLogger  the logge
     */
    public MailImporter(
        boolean extractMessageTextFile,
        boolean extractMessageTextMetadata,
        boolean extractAttachmentTextFile,
        boolean extractAttachmentMetadata,
        String protocol,
        String defaultCharsetName,
        String container,
        String mailfolder,
        String target,
        MailExtractProgressLogger mailExtractProgressLogger
    ) {
        this.protocol = protocol;
        this.container = container;
        this.urlString = StoreExtractor.composeStoreURL(protocol, null, null, null, container);
        this.mailfolder = mailfolder;
        this.summary = null;

        this.storeExtractorOptions = new StoreExtractorOptions(
            true,
            true,
            ResipGraphicApp.getTheApp().interfaceParameters.isDebugFlag(),
            12,
            defaultCharsetName,
            true,
            extractMessageTextFile,
            extractMessageTextMetadata,
            extractAttachmentTextFile,
            extractAttachmentMetadata,
            2
        );
        this.target = target;
        this.mailExtractProgressLogger = mailExtractProgressLogger;
    }

    /**
     * Readable file size.
     *
     * @param size the size
     * @return the string
     */
    private static String readableFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    /**
     * Do extract.
     *
     * @throws ResipException the resip exception
     */
    public void doExtract() throws ResipException {
        try {
            start = Instant.now();

            storeExtractor = StoreExtractor.createStoreExtractor(
                urlString,
                mailfolder,
                target,
                storeExtractorOptions,
                mailExtractProgressLogger
            );
            storeExtractor.extractAllFolders();
            summary = "Extraction " + storeExtractor.getSummary();
            storeExtractor.endStoreExtractor();
            end = Instant.now();
            ResipLogger.getGlobalLogger().log(ResipLogger.GLOBAL, getSummary(), null);
        } catch (Exception e) {
            throw new ResipException("Erreur d'extraction", e);
        }
    }

    /**
     * Gets the summary.
     *
     * @return the summary
     */
    public String getSummary() {
        String result = "resip: extraction depuis un conteneur courriel sur disque\n";
        result += "en [" + container + "]\n";
        result += "encodé selon le format [" + protocol + "]\n";
        result +=
        "génération de fichier en forme texte des messages: " +
        (storeExtractorOptions.extractMessageTextFile ? "oui" : "non") +
        "\n";
        result +=
        "extraction des métadonnées texte des messages: " +
        (storeExtractorOptions.extractMessageTextMetadata ? "oui" : "non") +
        "\n";
        result +=
        "génération de fichier en forme texte des pièces jointes: " +
        (storeExtractorOptions.extractFileTextFile ? "oui" : "non") +
        "\n";
        result +=
        "extraction des métadonnées texte des pièces jointes: " +
        (storeExtractorOptions.extractFileTextMetadata ? "oui" : "non") +
        "\n";
        result += "résultat: " + storeExtractor.getSummary();
        if (start != null) result += "en " + Duration.between(start, end).toString().substring(2) + "\n";
        return result;
    }

    /**
     * Gets the target.
     *
     * @return the target
     */
    public String getTarget() {
        return target;
    }
}

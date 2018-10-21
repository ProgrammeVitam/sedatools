/*
 * 
 */
package fr.gouv.vitam.tools.resip.inout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.logging.Logger;

import fr.gouv.vitam.tools.mailextract.lib.core.StoreExtractor;
import fr.gouv.vitam.tools.mailextract.lib.core.StoreExtractorOptions;
import fr.gouv.vitam.tools.resip.utils.ResipLogger;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

/**
 * The Class MailImporter.
 */
public class MailImporter {

	/** The end. */
	Instant start, end;

	/** The protocol. */
	String protocol;
	
	/** The container. */
	String container;
	
	/** The url string. */
	String urlString;
	
	/** The mailfolder. */
	String mailfolder;
	
	/** The target. */
	String target;
	
	/** The summary. */
	String summary;
	
	/** The store extractor options. */
	StoreExtractorOptions storeExtractorOptions;
	
	/** The store extractor. */
	StoreExtractor storeExtractor;

	/**
	 * Instantiates a new mail importer.
	 *
	 * @param extractMessageTextFile the extract message text file
	 * @param extractMessageTextMetadata the extract message text metadata
	 * @param extractAttachmentTextFile the extract attachment text file
	 * @param extractAttachmentMetadata the extract attachment metadata
	 * @param protocol the protocol
	 * @param container the container
	 * @param mailfolder the mailfolder
	 * @param workDir the work dir
	 */
	public MailImporter(boolean extractMessageTextFile, boolean extractMessageTextMetadata,
			boolean extractAttachmentTextFile, boolean extractAttachmentMetadata, String protocol, String container,
			String mailfolder, String workDir) {
		this.protocol = protocol;
		this.container = container;
		this.urlString = StoreExtractor.composeStoreURL(protocol, null, null, null, container);
		this.mailfolder = mailfolder;
		this.summary = null;

		this.storeExtractorOptions = new StoreExtractorOptions(true, false, true, 12, true, extractMessageTextFile,
				extractMessageTextMetadata, extractAttachmentTextFile, extractAttachmentMetadata, 2);
		this.target = workDir + File.separator + Paths.get(container).getFileName().toString() + "-tmpdir";
	}

	/**
	 * Readable file size.
	 *
	 * @param size the size
	 * @return the string
	 */
	private static String readableFileSize(long size) {
		if (size <= 0)
			return "0";
		final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
		int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
		return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}

	/**
	 * Do extract.
	 *
	 * @throws SEDALibException the resipt exception
	 */
	public void doExtract() throws SEDALibException {
		try {
			start = Instant.now();
			Path targetPath = Paths.get(target);
			Files.createDirectories(targetPath);
			PrintStream psExtractList = new PrintStream(new FileOutputStream(target + File.separator + "MailList.csv"));

			storeExtractor = StoreExtractor.createStoreExtractor(urlString, mailfolder, target,
					storeExtractorOptions, Logger.getLogger("mailextract"), psExtractList);
			storeExtractor.extractAllFolders();
			summary = "Extraction de " + Integer.toString(storeExtractor.getFolderTotalCount()) + " dossiers, "
					+ Integer.toString(storeExtractor.getTotalElementsCount()) + " messages, pour un taille totale de "
					+ readableFileSize(storeExtractor.getTotalRawSize()) + " et "
					+ Integer.toString(storeExtractor.getTotalAttachedMessagesCount()) + " pièces jointes";
			storeExtractor.endStoreExtractor();
			end = Instant.now();
			ResipLogger.getGlobalLogger().log(ResipLogger.GLOBAL,getSummary());
		} catch (Exception e) {
			throw new SEDALibException("Resip.InOut: Erreur d'extraction\n->" + e.getMessage());
		}
	}

	/**
	 * Gets the summary.
	 *
	 * @return the summary
	 */
	public String getSummary() {
		String result = "Extraction depuis un conteneur courriel sur disque\n";
		result += "en [" + container + "]\n";
		result += "encodé selon le format [" + protocol + "]\n";
		result += "génération de fichier en forme texte des messages: "
				+ (storeExtractorOptions.extractMessageTextFile ? "oui" : "non") + "\n";
		result += "extraction des métadonnées texte des messages: "
				+ (storeExtractorOptions.extractMessageTextMetadata ? "oui" : "non") + "\n";
		result += "génération de fichier en forme texte des pièces jointes: "
				+ (storeExtractorOptions.extractFileTextFile ? "oui" : "non") + "\n";
		result += "extraction des métadonnées texte des pièces jointes: "
				+ (storeExtractorOptions.extractFileTextMetadata ? "oui" : "non") + "\n";
		result+="Résultat: " + Integer.toString(storeExtractor.getFolderTotalCount()) + " dossiers, "
				+ Integer.toString(storeExtractor.getTotalElementsCount()) + " messages, pour un taille totale de "
				+ readableFileSize(storeExtractor.getTotalRawSize()) + " et "
				+ Integer.toString(storeExtractor.getTotalAttachedMessagesCount()) + " pièces jointes\n";
		if (start != null)
			result += "en " + Duration.between(start, end).toString().substring(2) + "\n";
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

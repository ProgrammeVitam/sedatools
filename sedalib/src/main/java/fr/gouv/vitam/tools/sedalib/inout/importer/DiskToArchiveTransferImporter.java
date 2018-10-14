/**
 * Copyright French Prime minister Office/DINSIC/Vitam Program (2015-2019)
 *
 * contact.vitam@programmevitam.fr
 * 
 * This software is developed as a validation helper tool, for constructing Submission Information Packages (archives 
 * sets) in the Vitam program whose purpose is to implement a digital archiving back-office system managing high 
 * volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA archiveTransfer the following URL "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */
package fr.gouv.vitam.tools.sedalib.inout.importer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Stream;

import fr.gouv.vitam.tools.sedalib.core.ArchiveTransfer;
import fr.gouv.vitam.tools.sedalib.core.GlobalMetadata;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.utils.ProgressLogger;

/**
 * The Class DiskToArchiveTransferImporter.
 * <p>
 * It will import from a directory content or from a list of files or
 * directories:
 * <ul>
 * <li>GlobalMetadata XML fragments from the __GlobalMetadata.xml file</li>
 * <li>ManagementMetadata XML element at the end of DataObjectPackage from the
 * __ManagementMetadata.xml file</li>
 * <li>each root ArchiveUnit from a sub directory, and recursively all the
 * DataObjectPackage structure (see {@link DiskToDataObjectPackageImporter} for
 * details)</li>
 * </ul>
 */

public class DiskToArchiveTransferImporter {

	/** The disk to DataObjectPackage importer. */
	private DiskToDataObjectPackageImporter diskToDataObjectPackageImporter;

	/** The on disk root paths. */
	private List<Path> onDiskRootPaths;

	/** The on disk root paths. */
	private Path onDiskGlobalMetadataPath;

	/** The archive transfer. */
	private ArchiveTransfer archiveTransfer;

	/** The start and end instants, for duration computation. */
	private Instant start, end;

	/** The progress logger. */
	private ProgressLogger progressLogger;

	/**
	 * Instantiates a new ArchiveTransfer importer from a single directory name.
	 * <p>
	 * It will consider each directory and each file in this directory as a root
	 * ArchiveUnit or a special metadata file
	 *
	 * @param directory      the directory name
	 * @param progressLogger the progress logger or null if no progress log expected
	 * @throws SEDALibException if not a directory
	 */
	public DiskToArchiveTransferImporter(String directory, ProgressLogger progressLogger)
			throws SEDALibException {
		Path path;
		Iterator<Path> pi;

		this.onDiskGlobalMetadataPath = null;
		this.archiveTransfer = new ArchiveTransfer();
		this.progressLogger = progressLogger;
		this.onDiskRootPaths = new ArrayList<Path>();

		path = Paths.get(directory);
		if (!Files.isDirectory(path, java.nio.file.LinkOption.NOFOLLOW_LINKS))
			throw new SEDALibException("[" + directory + "] n'est pas un répertoire");
		try (Stream<Path> sp = Files.list(path)) {
			pi = sp.iterator();
			while (pi.hasNext()) {
				path = pi.next();
				if (path.getFileName().toString().equals("__GlobalMetadata.xml"))
					this.onDiskGlobalMetadataPath = path;
				else
					this.onDiskRootPaths.add(path);
			}
		} catch (IOException e) {
			throw new SEDALibException(
					"Impossible de lister les fichiers du répertoire [" + directory + "]\n->" + e.getMessage());
		}

		this.diskToDataObjectPackageImporter = new DiskToDataObjectPackageImporter(this.onDiskRootPaths,
				progressLogger);
	}

	/**
	 * Instantiates a new ArchiveTransfer importer from a list of paths.
	 * <p>
	 * It will consider each directory and each file in this list as a root
	 * ArchiveUnit or a special metadata file
	 *
	 * @param paths          the paths
	 * @param progressLogger the progress logger or null if no progress log expected
	 */
	public DiskToArchiveTransferImporter(List<Path> paths, ProgressLogger progressLogger) {
		this.onDiskGlobalMetadataPath = null;
		this.archiveTransfer = new ArchiveTransfer();
		this.progressLogger = progressLogger;
		this.onDiskRootPaths = new ArrayList<Path>();

		for (Path path : paths) {
			if (path.getFileName().toString().equals("__GlobalMetadata"))
				this.onDiskGlobalMetadataPath = path;
			else
				this.onDiskRootPaths.add(path);
		}

		this.diskToDataObjectPackageImporter = new DiskToDataObjectPackageImporter(this.onDiskRootPaths,
				progressLogger);
	}

	/**
	 * Adds the ignore pattern.
	 *
	 * @param patternString the pattern string
	 */
	public void addIgnorePattern(String patternString) {
		diskToDataObjectPackageImporter.addIgnorePattern(patternString);
	}

	/**
	 * Process the GlobalMetadata file.
	 *
	 * @param path the path
	 * @return the ArchiveUnit generated from path
	 * @throws SEDALibException if reading file has failed
	 */
	private GlobalMetadata processGlobalMetadata(Path path) throws SEDALibException {
		GlobalMetadata atgm = new GlobalMetadata();
		try {
			atgm.fromSedaXmlFragments(new String(Files.readAllBytes(path), "UTF-8"));
		} catch (SEDALibException | IOException e) {
			throw new SEDALibException("Lecture des métadonnées globales à partir du fichier [" + path
					+ "] impossible\n->" + e.getMessage());
		}
		return atgm;
	}

	/**
	 * Do import the disk structure to ArchiveTransfer. It will import from a
	 * directory content or from a list of files or directories:
	 * <ul>
	 * <li>GlobalMetadata XML fragments from the __GlobalMetadata.xml file</li>
	 * <li>ManagementMetadata XML element at the end of DataObjectPackage from the
	 * __ManagementMetadata.xml file</li>
	 * <li>each root ArchiveUnit from a sub directory, and recursively all the
	 * DataObjectPackage structure (see {@link DiskToDataObjectPackageImporter} for
	 * details)</li>
	 * </ul>
	 * 
	 * @throws SEDALibException     any import exception
	 * @throws InterruptedException if export process is interrupted
	 */
	public void doImport() throws SEDALibException, InterruptedException {
		Date d = new Date();
		start = Instant.now();
		String log = "Début de l'import d'un ArchiveTransfer depuis une hiérarchie sur disque\n";
		log += "en [";
		boolean first = true;
		for (Path path : onDiskRootPaths) {
			if (first)
				first = false;
			else
				log += ",\n";
			log += path.toString();
		}
		log += "]";
		log += " date=" + DateFormat.getDateTimeInstance().format(d);
		if (progressLogger!=null)
			progressLogger.log(Level.INFO,log);

		if (onDiskGlobalMetadataPath != null)
			archiveTransfer.setGlobalMetadata(processGlobalMetadata(onDiskGlobalMetadataPath));
		diskToDataObjectPackageImporter.doImport();
		archiveTransfer.setDataObjectPackage(diskToDataObjectPackageImporter.getDataObjectPackage());
		end = Instant.now();
		if (progressLogger!=null)
			progressLogger.log(Level.INFO,getSummary());
}

	/**
	 * Gets the ArchiveTransfer
	 *
	 * @return the ArchiveTransfer
	 */
	public ArchiveTransfer getArchiveTransfer() {
		return archiveTransfer;
	}

	/**
	 * Gets the model version.
	 *
	 * @return the model version
	 */
	public int getModelVersion() {
		return diskToDataObjectPackageImporter.getModelVersion();
	}

	/**
	 * Gets the summary of the import process.
	 *
	 * @return the summary
	 */
	public String getSummary() {
		String result = "Import d'un ArchiveTransfer depuis une hiérarchie sur disque\n";
		result += "en [";
		boolean first = true;
		for (Path path : onDiskRootPaths) {
			if (first)
				first = false;
			else
				result += ",\n";
			result += path.toString();
		}
		result += "]\n";
		result += archiveTransfer.getDescription() + "\n";
		switch (getModelVersion()) {
		case 0:
			result += "encodé selon un modèle neutre de la structure\n";
			break;
		case 1:
			result += "encodé selon un modèle V1 de la structure\n";
			break;
		case 2:
			result += "encodé selon un modèle V2 de la structure\n";
			break;
		case 3:
			result += "encodé selon un modèle hybride V1/V2 de la structure\n";
			break;
		}
		if ((start != null) && (end != null))
			result += "chargé en " + Duration.between(start, end).toString().substring(2) + "\n";
		return result;
	}
}

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
package fr.gouv.vitam.tools.sedalib.droid;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.utils.ProgressLogger;
import uk.gov.nationalarchives.droid.core.BinarySignatureIdentifier;
import uk.gov.nationalarchives.droid.core.SignatureParseException;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResult;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.archive.IdentificationRequestFactory;
import uk.gov.nationalarchives.droid.core.interfaces.resource.FileSystemIdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;
import uk.gov.nationalarchives.droid.core.signature.FileFormat;
import uk.gov.nationalarchives.droid.core.signature.FileFormatCollection;
import uk.gov.nationalarchives.droid.core.signature.droid6.FFSignatureFile;
import uk.gov.nationalarchives.droid.container.ContainerFileIdentificationRequestFactory;
import uk.gov.nationalarchives.droid.container.ContainerSignatureDefinitions;
import uk.gov.nationalarchives.droid.container.ContainerSignatureSaxParser;
import uk.gov.nationalarchives.droid.container.TriggerPuid;
import uk.gov.nationalarchives.droid.container.ole2.Ole2IdentifierEngine;
import uk.gov.nationalarchives.droid.container.zip.ZipIdentifierEngine;

/**
 * The Class DroidIdentifier.
 * <p>
 * Singleton class for managing the DROID identifications
 */
public class DroidIdentifier {

	/** Singleton. */
	private static DroidIdentifier instance = null;

	/** The config directory. */
	private String configDir;

	/** DROID structures used for format identification. */
	private FFSignatureFile binarySignatureFile;

	/** The container signature definitions. */
	private ContainerSignatureDefinitions containerSignatureDefinitions;

	/** The binary signature identifier. */
	private BinarySignatureIdentifier binarySignatureIdentifier;

	/** The container content identifier map. */
	private HashMap<String, ContainerDroidIdentifier> containerContentIdentierMap;

	/**
	 * Instantiates a new DROID identifier.
	 *
	 * @param progressLogger the progress logger or null if no progress log expected
	 * @param configDir      the config dir
	 * @throws SEDALibException if the identifier can't be initialised, may be due to wrong signatures files
	 */
	private DroidIdentifier(ProgressLogger progressLogger, String configDir) throws SEDALibException {
		this .configDir=configDir;
		initSignatureDroid(progressLogger);
		initContainerDroid(progressLogger);
	}

	/**
	 * Initialises the single instance of DroidIdentifier.
	 *
	 * @param progressLogger the progress logger or null if no progress log expected
	 * @param configDir      the config dir
	 * @return single instance of DroidIdentifier
	 * @throws SEDALibException if the identifier can't be initialised, may be due to wrong signatures files
	 */
	static public DroidIdentifier init(ProgressLogger progressLogger, String configDir) throws SEDALibException {
		instance = new DroidIdentifier(progressLogger,configDir);
		return instance;
	}

	/**
	 * Gets the single instance of DroidIdentifier.
	 *
	 * @return single instance of DroidIdentifier
	 */
	static public DroidIdentifier getInstance() {
		if (instance == null)
			try {
				instance = new DroidIdentifier(null,"./config");
			} catch (SEDALibException e) {
				System.err.println(e.getMessage());
				System.exit(1);
			}
		return instance;
	}

	/**
	 * Gets the binary signature file name.
	 * <p>
	 * Search for a DROID_SignatureFileVxxx.xml in "config" directory or extract it
	 * from resources to the "config" directory.
	 *
	 * @return the binary signature file name
	 * @throws SEDALibException if unable to get signature file from disk and from
	 *                          resources
	 */
	// search for a DROID_SignatureFile or extract from resources
	private String getBinarySignatureFileName(ProgressLogger progressLogger) throws SEDALibException {
		String result = null;

		FilenameFilter droidFilter = (dir, name) -> {
            if (name.startsWith("DROID_SignatureFile_V") && name.endsWith(".xml")) {
                String serial = name.substring(21, name.lastIndexOf(".xml"));
                try {
                    Integer.parseInt(serial);
                } catch (NumberFormatException e) {
                    return false;
                }
                return true;
            }
            return false;
        };

		File dir = new File(configDir);
		if (dir.isFile())
			throw new SEDALibException("Panic! Can't create config directory");
		else if (!dir.exists())
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
		String[] fileList = dir.list(droidFilter);
		if ((fileList==null) || (fileList.length == 0)) {
			if (progressLogger != null)
				progressLogger.log(Level.INFO,
						"Can't find a DROID signature file, copy from ressource to file DROID_SignatureFile_V88.xml");
			try (InputStream is = Thread.currentThread().getContextClassLoader()
					.getResourceAsStream("DROID_SignatureFile_V88.xml")) {
				File targetFile = new File(
						"." + File.separator + "config" + File.separator + "DROID_SignatureFile_V88.xml");
				System.out.println("is=" + is + " toPath=" + targetFile.toPath());
				java.nio.file.Files.copy(is, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				throw new SEDALibException("Panic! Can't extract a DROID signature file, stop");
			}
			result = "DROID_SignatureFile_V88.xml";
		} else {
			int serialNum = -1;
			for (String name : fileList) {
				int j = Integer.parseInt(name.substring(21, name.lastIndexOf(".xml")));
				if (j > serialNum) {
					serialNum = j;
					result = name;
				}
			}
		}
		return "." + File.separator + "config" + File.separator + result;
	}

	/**
	 * Inits the signature identifier context.
	 *
	 * @throws SEDALibException if unable to parse the signature file
	 */
	private void initSignatureDroid(ProgressLogger progressLogger) throws SEDALibException {
		binarySignatureIdentifier = new BinarySignatureIdentifier();

		String sigFileName = getBinarySignatureFileName(progressLogger);
		binarySignatureIdentifier.setSignatureFile(sigFileName);
		try {
			binarySignatureIdentifier.init();
		} catch (SignatureParseException x) {
			System.err.println("Panic: Can't parse file: '" + sigFileName + "'");
			System.exit(1);
		}
		binarySignatureFile = binarySignatureIdentifier.getSigFile();
	}

	/**
	 * Gets the container signature file name.
	 * <p>
	 * Search for a container-signature-xxxx.xml in "config" directory or extract it
	 * from resources to the "config" directory.
	 *
	 * @return the container signature file name
	 * @throws SEDALibException if unable to get container signature file from disk
	 *                          and from resources
	 */
	private String getContainerSignatureFileName(ProgressLogger progressLogger) throws SEDALibException {
		String result = null;

		FilenameFilter droidFilter = (dir, name) -> {
            if (name.startsWith("container-signature-") && name.endsWith(".xml")) {
                String serial = name.substring(20, name.lastIndexOf(".xml"));
                try {
                    Integer.parseInt(serial);
                } catch (NumberFormatException e) {
                    return false;
                }
                return true;
            }
            return false;
        };

		File dir = new File(configDir);
		if (dir.isFile())
			throw new SEDALibException("Panic! Can't create config directory");
		else if (!dir.exists())
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
		String[] fileList = dir.list(droidFilter);
		if ((fileList==null) || (fileList.length == 0)) {
			if (progressLogger != null)
				progressLogger.log(Level.INFO,
						"Can't find a DROID container signature file, copy from ressource to file container-signature-20171130.xml");
			try (InputStream is = Thread.currentThread().getContextClassLoader()
					.getResourceAsStream("container-signature-20171130.xml")) {
				File targetFile = new File(
						"." + File.separator + "config" + File.separator + "container-signature-20171130.xml");
				java.nio.file.Files.copy(is, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				throw new SEDALibException("Panic! Can't extract a DROID signature file, stop");
			}
			result = "container-signature-20171130.xml";
		} else {
			int serialNum = -1;
			for (String name : fileList) {
				int j = Integer.parseInt(name.substring(21, name.lastIndexOf(".xml")));
				if (j > serialNum) {
					serialNum = j;
					result = name;
				}
			}
		}
		return "." + File.separator + "config" + File.separator + result;
	}

	/** The Constant OLE2_CONTAINER. */
	private static final String OLE2_CONTAINER = "OLE2";

	/** The Constant ZIP_CONTAINER. */
	private static final String ZIP_CONTAINER = "ZIP";

	/**
	 * Inits the container signature identifier context.
	 *
	 * @throws SEDALibException if unable to parse the container file
	 */
	@SuppressWarnings("rawtypes")
	private void initContainerDroid(ProgressLogger progressLogger) throws SEDALibException {
		// get container signature definitions
		String containerSigFileName = getContainerSignatureFileName(progressLogger);
		try {
			InputStream in = new FileInputStream(containerSigFileName);
			ContainerSignatureSaxParser parser = new ContainerSignatureSaxParser();
			containerSignatureDefinitions = parser.parse(in);
		} catch (SignatureParseException e) {
			throw new SEDALibException("Panic! Can't parse container signature file");
		} catch (Exception e) {
			throw new SEDALibException("Panic! Can't open container signature file");
		}

		containerContentIdentierMap = new HashMap<String, ContainerDroidIdentifier>();

		// create container content identifier for OLE2
		IdentificationRequestFactory ole2RequestFactory = new ContainerFileIdentificationRequestFactory();
		ContainerDroidIdentifier ole2Identifier = new ContainerDroidIdentifier(containerSignatureDefinitions,
				binarySignatureFile, OLE2_CONTAINER);
		Ole2IdentifierEngine ole2IdentifierEngine = new Ole2IdentifierEngine();
		ole2IdentifierEngine.setRequestFactory(ole2RequestFactory);
		ole2Identifier.setIdentifierEngine(ole2IdentifierEngine);
		containerContentIdentierMap.put(OLE2_CONTAINER, ole2Identifier);

		// create container content identifier for ZIP
		IdentificationRequestFactory zipRequestFactory = new ContainerFileIdentificationRequestFactory();
		ContainerDroidIdentifier zipIdentifier = new ContainerDroidIdentifier(containerSignatureDefinitions,
				binarySignatureFile, ZIP_CONTAINER);
		ZipIdentifierEngine zipIdentifierEngine = new ZipIdentifierEngine();
		zipIdentifierEngine.setRequestFactory(zipRequestFactory);
		zipIdentifier.setIdentifierEngine(zipIdentifierEngine);
		containerContentIdentierMap.put(ZIP_CONTAINER, zipIdentifier);

	}

	/**
	 * Gets the TriggerPuid by puid, or null if no match.
	 * <p>
	 * TriggerPuid gives the link between container identifier and puid of outer
	 * format
	 *
	 * @param puid the puid
	 * @return the TriggerPuid by puid
	 */
// get the container ID for the given PUID if any
	public TriggerPuid getTriggerPuidByPuid(final String puid) {
		for (final TriggerPuid tp : containerSignatureDefinitions.getTiggerPuids()) {
			if (tp.getPuid().equals(puid)) {
				return tp;
			}
		}
		return null;
	}

	/**
	 * Can use container identification.
	 *
	 * @return true, if successful
	 */
	public boolean canAnalyzeContainer() {
		return containerSignatureDefinitions != null;
	}

	/**
	 * Gets the identification result.
	 *
	 * @param path the path of file to identify
	 * @return the identification result
	 * @throws SEDALibException if the file can't be read
	 */
	public IdentificationResult getIdentificationResult(Path path) throws SEDALibException {
		List<IdentificationResult> irl;
		String filename = path.normalize().toString();
		FileSystemIdentificationRequest request;

		RequestMetaData metaData;
		try {
			metaData = new RequestMetaData(Files.size(path), Files.getLastModifiedTime(path).toMillis(), filename);
			RequestIdentifier identifier = new RequestIdentifier(path.toUri());
			identifier.setParentId(1L);
			request = new FileSystemIdentificationRequest(metaData, identifier);
			request.open(path);
		} catch (IOException e) {
			throw new SEDALibException("Impossible d'accéder au fichier [" + filename + "]");
		}

		IdentificationResultCollection resultsSignatureCollection = getSignatureResults(request);
		IdentificationResultCollection resultsContainerCollection;
		try {
			resultsContainerCollection = getContainerResults(resultsSignatureCollection, request);
		} catch (SEDALibException e) {
			throw new SEDALibException("Erreur dans l'identification par container du fichier [" + filename + "]");
		}
		if ((resultsContainerCollection != null) && !resultsContainerCollection.getResults().isEmpty()) {
			irl = resultsContainerCollection.getResults();
		} else if (!resultsSignatureCollection.getResults().isEmpty()) {
			irl = resultsSignatureCollection.getResults();
		} else {
			irl = getExtensionResults(request).getResults();
		}

		if ((irl != null) && (!irl.isEmpty())) {
			String fileExtension = "";
			if (filename.lastIndexOf('.') != -1)
				fileExtension = filename.substring(filename.lastIndexOf('.') + 1);
			return selectBestResult(irl, fileExtension);
		} else
			return null;
	}

	/**
	 * Gets the signature (no container) format identification if any.
	 *
	 * @param request the request
	 * @return the signature results
	 */
	IdentificationResultCollection getSignatureResults(
			@SuppressWarnings("rawtypes") final IdentificationRequest request) {

		IdentificationResultCollection results = binarySignatureIdentifier.matchBinarySignatures(request);
		binarySignatureIdentifier.checkForExtensionsMismatches(results, request.getExtension());
		return results;
	}

	/**
	 * Gets the extension format identification if any.
	 *
	 * @param request the request
	 * @return the extension results
	 */
	IdentificationResultCollection getExtensionResults(
			@SuppressWarnings("rawtypes") final IdentificationRequest request) {

		return binarySignatureIdentifier.matchExtensions(request, true);
	}

	/**
	 * Gets the container format identification if any, for any type of container.
	 *
	 * @param signatureResults the signature results
	 * @param request          the request
	 * @return the container results
	 * @throws SEDALibException the SEDALibException
	 */
	IdentificationResultCollection getContainerResults(final IdentificationResultCollection signatureResults,
			@SuppressWarnings("rawtypes") final IdentificationRequest request) throws SEDALibException {
		IdentificationResultCollection containerResults = new IdentificationResultCollection(request);

		if (!signatureResults.getResults().isEmpty() && canAnalyzeContainer()) {
			for (IdentificationResult identResult : signatureResults.getResults()) {
				String filePuid = identResult.getPuid();
				if (filePuid != null) {
					TriggerPuid containerPuid = getTriggerPuidByPuid(filePuid);
					if (containerPuid != null) {

						String containerType = containerPuid.getContainerType();

						ContainerDroidIdentifier cci = containerContentIdentierMap.get(containerType);
						if (cci != null) {
							try {
								containerResults = cci.getContainerIdentification(request.getSourceInputStream(),
										containerResults);
							} catch (IOException e) { // go on after problems
								throw new SEDALibException(
										"Impossible d'analyser en conteneur le format du fichier [" + request.getFileName() + "]");
							}
						}
					}
				}
			}
		}
		return containerResults;
	}

	/**
	 * Select best identification result.
	 * <p>
	 * Use the signature file priorities to sort the different formats, and the
	 * extension to select the best format if there are more than one with no
	 * priority at the higher level.
	 *
	 * @param irl           the list of all identification results
	 * @param fileExtension the file extension of original file
	 * @return the best identification result, or null if the list was empty
	 */
// use the signature file priorities and the extension to select the best format
	protected IdentificationResult selectBestResult(List<IdentificationResult> irl, String fileExtension) {
		// special quick return cases
		int numResults = irl.size();
		if (numResults == 0)
			return null;
		else if (numResults == 1) {
			return irl.get(0);
		}

		// Build a set of format ids the results have priority over:
		FileFormatCollection allFormats = binarySignatureFile.getFileFormatCollection();
		Set<Integer> lowerPriorityIDs = new HashSet<Integer>();
		for (int i = 0; i < numResults; i++) {
			final IdentificationResult result = irl.get(i);
			final String resultPUID = result.getPuid();
			final FileFormat format = allFormats.getFormatForPUID(resultPUID);
			lowerPriorityIDs.addAll(format.getFormatIdsHasPriorityOver());
		}

		// if a result has an id in this set, add it to the remove list
		List<IdentificationResult> lowerPriorityResults = new ArrayList<IdentificationResult>();
		for (int i = 0; i < numResults; i++) {
			final IdentificationResult tmp = irl.get(i);
			final String resultPUID = tmp.getPuid();
			final FileFormat format = allFormats.getFormatForPUID(resultPUID);
			if (lowerPriorityIDs.contains(format.getID())) {
				lowerPriorityResults.add(tmp);
			}
		}

		// now remove any lower priority results from the list
		numResults = lowerPriorityResults.size();
		for (int i = 0; i < numResults; i++) {
			irl.remove(lowerPriorityResults.get(i));
		}

		// different return cases
		numResults = irl.size();
		if (numResults == 0)
			return null;
		else if (numResults == 1) {
			return irl.get(0);
		} else {
			// if multiple results use extension to choose
			for (int i = 0; i < numResults; i++) {
				final FileFormat format = allFormats.getFormatForPUID(irl.get(i).getPuid());
				if (format.hasMatchingExtension(fileExtension)) {
					return irl.get(i);
				}
			}
			// if no matching extension choose the first in the list
			return irl.get(0);
		}
	}
}

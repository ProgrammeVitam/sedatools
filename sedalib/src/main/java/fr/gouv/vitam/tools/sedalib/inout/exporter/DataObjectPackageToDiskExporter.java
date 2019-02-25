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
 * circulated by CEA, CNRS and INRIA dataObjectPackage the following URL "http://www.cecill.info".
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
package fr.gouv.vitam.tools.sedalib.inout.exporter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.core.DataObject;
import fr.gouv.vitam.tools.sedalib.core.DataObjectGroup;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;
import fr.gouv.vitam.tools.sedalib.core.DataObjectRefList;
import fr.gouv.vitam.tools.sedalib.core.PhysicalDataObject;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;
import fr.gouv.vitam.tools.sedalib.xml.IndentXMLTool;
import mslinks.ShellLink;

/**
 * The Class DataObjectPackageToDiskExporter.
 * <p>
 * Class for DataObjectPackage object export on disk hierarchy.
 * <p>
 * The general principles are:
 * <ul>
 * <li>the directory imported contains all the directories and files that
 * represent one root ArchiveUnit in the ArchiveTransfer</li>
 * <li>each sub-directory in the hierarchy represent an ArchiveUnit</li>
 * <li>each file represent an ArchiveUnit containing a BinaryDataObject for the
 * file itself, being the BinaryMaster_1 and with format identification
 * compliant to PRONOM register</li>
 * <li>the title of ArchiveUnit is the directory/file name, if no other metadata
 * is defined</li>
 * </ul>
 * For example, if you export this ArchiveUnit:
 * <p>
 * ArchiveUnit (Title="TestDir1")
 * <p>
 * |---ArchiveUnit (Title="NodeFile1.1.jpg")
 * <p>
 * |---BinaryDataObject(DataObjectVersion="BinaryMaster_1"
 * FileName="NodeFile1.1.jpg"...)
 * <p>
 * ArchiveUnit (Title="NodeFile2.pdf")
 * <p>
 * |---BinaryDataObject(DataObjectVersion="BinaryMaster_1"
 * FileName="NodeFile2.pdf"...)
 * <p>
 * the Disk Hierarchy will contain:
 * <p>
 * RootDir
 * <p>
 * |---TestDir1
 * <p>
 * |---NodeFile1.1.jpg
 * <p>
 * |---__BinaryMaster_1_NodeFile1.1.jpg
 * <p>
 * |---__BinaryMaster_1_BinaryDataObjectMetadata
 * <p>
 * |---NodeFile2.pdf
 * <p>
 * |---__BinaryMaster_1_NodeFile1.2.pdf
 * <p>
 * |---__BinaryMaster_1_BinaryDataObjectMetadata
 * <p>
 * the export is compliant to Model V2-Extended model
 * ({@link fr.gouv.vitam.tools.sedalib.inout.importer.DiskToArchiveTransferImporter}
 */
public class DataObjectPackageToDiskExporter {

	/** The Constant emptyPath. */
	static final Path emptyPath = Paths.get("");

	/** The archive transfer. */
	private DataObjectPackage dataObjectPackage;

	/** The is windows. */
	private boolean isWindows;

	/** The progress logger. */
	private SEDALibProgressLogger sedaLibProgressLogger;

	/**
	 * The ArchiveUnit path string map, used to manage symbolic links.
	 */
	private HashMap<ArchiveUnit, Path> auPathStringMap;

	/**
	 * The DataObjectGroup path string map, used to manage symbolic links.
	 */
	private HashMap<DataObjectGroup, Path> dogPathStringMap;

	/** The set of all generated files paths to detect collision. */
	private Set<Path> filesPathSet;

	/**
	 * Instantiates a new DataObjectPackage to disk exporter.
	 *
	 * @param sedaLibProgressLogger the progress logger or null if no progress log expected
	 */
	private DataObjectPackageToDiskExporter(SEDALibProgressLogger sedaLibProgressLogger) {
		this.dataObjectPackage = null;
		this.sedaLibProgressLogger = sedaLibProgressLogger;
		this.auPathStringMap = new HashMap<ArchiveUnit, Path>();
		this.dogPathStringMap = new HashMap<DataObjectGroup, Path>();
		this.filesPathSet = new HashSet<Path>();
		isWindows = System.getProperty("os.name").toLowerCase().contains("win");
	}

	/**
	 * Instantiates a new DataObjectPackage to disk exporter.
	 *
	 * @param dataObjectPackage the archive transfer
	 * @param sedaLibProgressLogger    the progress logger
	 */
	public DataObjectPackageToDiskExporter(DataObjectPackage dataObjectPackage, SEDALibProgressLogger sedaLibProgressLogger) {
		this(sedaLibProgressLogger);
		this.dataObjectPackage = dataObjectPackage;
	}

	/**
	 * Construct file name for BinaryDataObject .
	 *
	 * @param binaryDataObject the BinaryDataObject
	 * @return the file name String
	 */
	private String constructFileName(BinaryDataObject binaryDataObject) {
		String result = "__" + binaryDataObject.dataObjectVersion + "__";
		if ((binaryDataObject.fileInfo != null) && (binaryDataObject.fileInfo.filename != null)) {
			result += binaryDataObject.fileInfo.filename;
		} else
			result += "NoName";
		return stripFileName(result);
	}

	/**
	 * Construct file name for BinaryDataObject metadata.
	 *
	 * @param binaryDataObject the BinaryDataObject
	 * @return the file name
	 */
	private String constructMetadataFileName(BinaryDataObject binaryDataObject) {
		String result = "__" + binaryDataObject.dataObjectVersion + "__BinaryDataObjectMetadata.xml";
		return stripFileName(result);
	}

	/**
	 * Construct file name for PhysicalDataObject metadata.
	 *
	 * @param physicalDataObject the PhysicalDataObject
	 * @return the file name
	 */
	private String constructMetadataFileName(PhysicalDataObject physicalDataObject) {
		String result = "__" + physicalDataObject.dataObjectVersion + "__PhysicalDataObjectMetadata.xml";
		return stripFileName(result);
	}

	/**
	 * Construct directory name for ArchiveUnit.
	 *
	 * @param au the ArchiveUnit
	 * @return the file name
	 */
	private String constructDirectoryName(ArchiveUnit au) throws SEDALibException {
		String result = "";
		if (au.getContent() != null) {
			result = au.getContent().getSimpleMetadata("Title");
			if (result==null)
				result="NoTitle";
			else if (result.length() > 12)
				result = result.substring(0, 11);
			result += "_";
		}
		result += au.getInDataObjectPackageId();
		return stripFileName(result);
	}

	/**
	 * Strip a String of all characters not allowed in a file name.
	 *
	 * @param fileName the file name
	 * @return the string
	 */
	@SuppressWarnings("Annotator")
	private String stripFileName(String fileName) {
		return fileName.replaceAll("[\\/|\\\\|\\*|\\:|\\||\"|\'|\\<|\\>|\\{|\\}|\\?|\\%|,]", "_");
	}

	/**
	 * Export DataObjectGroup management metadata.
	 *
	 * @param xmlData       the xml ManagementMetadata data
	 * @param containerPath the container path
	 * @throws SEDALibException if writing has failed
	 */
	public void exportManagementMetadata(String xmlData, Path containerPath) throws SEDALibException {
		Path targetOnDiskPath;
		String identXml;
		// write binary file
		targetOnDiskPath = containerPath.resolve("__ManagementMetadata.xml");
		filesPathSet.add(targetOnDiskPath);
		try (FileOutputStream fos = new FileOutputStream(targetOnDiskPath.toFile());
				Writer rawWriter = new OutputStreamWriter(fos, "UTF-8")) {
			try {
				identXml = IndentXMLTool.getInstance(2).indentString(xmlData);
			} catch (Exception e) {
				identXml = xmlData;
			}
			rawWriter.write(identXml);
		} catch (Exception e) {
			throw new SEDALibException(
					"Ecriture des ManagementMetadata [" + targetOnDiskPath + "] impossible\n->" + e.getMessage());
		}
	}

	/**
	 * Test if DataObjectGroup in ArchiveUnit has to be explicit (that is to say at
	 * least one reference link exists to this DataObjectGroup) and does it for all
	 * his childs.
	 *
	 * @param archiveUnit the ArchiveUnit
	 */
	private void determineExplicitDataObjectGroupInArchiveUnit(ArchiveUnit archiveUnit) {
		boolean complexDataObjectRefList = (archiveUnit.getDataObjectRefList().getDataObjectList().size() > 1);
		for (DataObject zdo : archiveUnit.getDataObjectRefList().getDataObjectList()) {
			if (zdo instanceof DataObjectGroup) {
				if (complexDataObjectRefList
						|| (dataObjectPackage.isTouchedInDataObjectPackageId(zdo.getInDataObjectPackageId())))
					dogPathStringMap.put((DataObjectGroup) zdo, emptyPath);
				else
					dataObjectPackage.addTouchedInDataObjectPackageId(zdo.getInDataObjectPackageId());
			}
		}
		for (ArchiveUnit childAu : archiveUnit.getChildrenAuList().getArchiveUnitList()) {
			determineExplicitDataObjectGroupInArchiveUnit(childAu);
		}

	}

	/**
	 * Determine the explicit DataObjectGroup map.
	 */
	private void determineExplicitDataObjectGroupMap() {
		dataObjectPackage.resetTouchedInDataObjectPackageIdMap();
		for (ArchiveUnit au : dataObjectPackage.getGhostRootAu().getChildrenAuList().getArchiveUnitList())
			determineExplicitDataObjectGroupInArchiveUnit(au);
	}

	/**
	 * Export BinaryDataObject to disk (binary and metadata).
	 *
	 * @param bdo           the BinaryDataObject
	 * @param containerPath the container path
	 * @throws SEDALibException if writing has failed
	 */
	public void exportBinaryDataObject(BinaryDataObject bdo, Path containerPath) throws SEDALibException {
		Path targetOnDiskPath;
		// write binary file
		targetOnDiskPath = containerPath.resolve(constructFileName(bdo));
		if (filesPathSet.contains(targetOnDiskPath))
			throw new SEDALibException("Collision de fichiers de représentation [" + targetOnDiskPath.toString()
					+ "] détectée sur le BinaryDataObject [" + bdo.getInDataObjectPackageId() + "]");
		filesPathSet.add(targetOnDiskPath);
		try {
			Files.copy(bdo.getOnDiskPath(), targetOnDiskPath, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new SEDALibException("Ecriture du BinaryDataObject [" + bdo.getInDataObjectPackageId()
					+ "] impossible\n->" + e.getMessage());
		}
		// write metadata file
		targetOnDiskPath = containerPath.resolve(constructMetadataFileName(bdo));
		if (filesPathSet.contains(targetOnDiskPath))
			throw new SEDALibException("Collision de fichiers de représentation [" + targetOnDiskPath.toString()
					+ "] détectée sur le BinaryDataObject [" + bdo.getInDataObjectPackageId() + "]");
		filesPathSet.add(targetOnDiskPath);
		try {
			Files.write(targetOnDiskPath, bdo.toSedaXmlFragments().getBytes("UTF-8"));
		} catch (Exception e) {
			throw new SEDALibException("Ecriture des métadonnées du BinaryDataObject [" + bdo.getInDataObjectPackageId()
					+ "] impossible\n->" + e.getMessage());
		}
	}

	/**
	 * Export PhysicalDataObject to disk (metadata).
	 *
	 * @param pdo           the PhysicalDataObject
	 * @param containerPath the container path
	 * @throws SEDALibException if writing has failed
	 */
	private void exportPhysicalDataObject(PhysicalDataObject pdo, Path containerPath) throws SEDALibException {
		Path targetOnDiskPath;
		// write metadata file
		targetOnDiskPath = containerPath.resolve(constructMetadataFileName(pdo));
		if (filesPathSet.contains(targetOnDiskPath))
			throw new SEDALibException("Collision de fichiers de représentation [" + targetOnDiskPath.toString()
					+ "] détectée sur le PhysicalDataObject [" + pdo.getInDataObjectPackageId() + "]");
		filesPathSet.add(targetOnDiskPath);
		try {
			Files.write(targetOnDiskPath, pdo.toSedaXmlFragments().getBytes("UTF-8"));
		} catch (Exception e) {
			throw new SEDALibException("Ecriture des métadonnées du PhysicalDataObject ["
					+ pdo.getInDataObjectPackageId() + "] impossible\n->" + e.getMessage());
		}
	}

	/**
	 * Export DataObjectGroup content to disk.
	 *
	 * @param dog           the DataObjectGroup
	 * @param containerPath the container path
	 * @throws SEDALibException if writing has failed
	 */
	private void exportDataObjectGroupContent(DataObjectGroup dog, Path containerPath) throws SEDALibException {
		for (BinaryDataObject bdo : dog.getBinaryDataObjectList())
			exportBinaryDataObject(bdo, containerPath);
		for (PhysicalDataObject pdo : dog.getPhysicalDataObjectList())
			exportPhysicalDataObject(pdo, containerPath);
	}

	/**
	 * Export explicit DataObjectGroupas a directory ##DataObjectGroup## to disk.
	 *
	 * @param dog           the DataObjectGroup
	 * @param containerPath the container path
	 * @return the path of directory representing the DataObjectGroup
	 * @throws SEDALibException if writing has failed
	 */
	private Path exportDataObjectGroup(DataObjectGroup dog, Path containerPath) throws SEDALibException {
		Path targetOnDiskPath;
		// create directory
		targetOnDiskPath = containerPath.resolve("##DataObjectGroup-"+dog.getInDataObjectPackageId()+"##");
		try {
			Files.createDirectories(targetOnDiskPath);
		} catch (Exception e) {
			throw new SEDALibException(
					"Création du répertoire [" + targetOnDiskPath + "] impossible\n->" + e.getMessage());
		}
		exportDataObjectGroupContent(dog, targetOnDiskPath);
		return targetOnDiskPath;
	}

	/**
	 * Export link to disk.
	 * <p>
	 * Try to link as a symbolic link, and if this is not possible, on Windows, a
	 * shortcut is created.
	 *
	 * @param target        the target file or directory name
	 * @param containerPath the container path
	 * @param newLink       the new link
	 * @throws SEDALibException if writing has failed, this can be because the user
	 *                          rights are not high enough.
	 */
	private void exportLink(Path target, Path containerPath, String newLink) throws SEDALibException {
		Path linkOnDiskPath;
		// write link
		linkOnDiskPath = containerPath.resolve(newLink);
		if (filesPathSet.contains(linkOnDiskPath))
			throw new SEDALibException("Collision de fichiers de représentation [" + linkOnDiskPath.toString()
					+ "] pour la création d'un lien");
		filesPathSet.add(linkOnDiskPath);
		try {
			if (Files.exists(linkOnDiskPath, LinkOption.NOFOLLOW_LINKS))
				Files.delete(linkOnDiskPath);
			Files.createSymbolicLink(linkOnDiskPath, linkOnDiskPath.toAbsolutePath().getParent().relativize(target.toAbsolutePath()));
		} catch (Exception e) {
			if (isWindows) {
				if (sedaLibProgressLogger !=null)
					sedaLibProgressLogger.log(SEDALibProgressLogger.OBJECTS_WARNINGS,
						"La création de lien n'a pas pu avoir lieu, essai de création de raccourci sous Windows");
				ShellLink sl = new ShellLink();
				sl.setTarget(target.toString());
				try {
					if (Files.exists(Paths.get(linkOnDiskPath.toString() + ".lnk"), LinkOption.NOFOLLOW_LINKS))
						Files.delete(Paths.get(linkOnDiskPath.toString() + ".lnk"));
					sl.saveTo(linkOnDiskPath.toString() + ".lnk");
				} catch (IOException e1) {
					throw new SEDALibException("Création du lien et du raccourci [" + linkOnDiskPath
							+ "] impossible\n->" + e.getMessage());
				}
			} else
				throw new SEDALibException("Création du lien [" + linkOnDiskPath + "] impossible\n->" + e.getMessage());
		}
	}

	/**
	 * Export DataObjectRefList to disk.
	 *
	 * @param dorl   the DataObjectRefList
	 * @param auPath the ArchiveUnit path
	 * @throws SEDALibException if writing has failed
	 */
	private void exportDataObjectRefList(DataObjectRefList dorl, Path auPath) throws SEDALibException {
		for (DataObject zdo : dorl.getDataObjectList())
			if (zdo instanceof DataObjectGroup) {
				Path dogPath = dogPathStringMap.get(zdo);
				if (dogPath == null)
					exportDataObjectGroupContent((DataObjectGroup) zdo, auPath);
				else if (dogPath == emptyPath)
					dogPathStringMap.put((DataObjectGroup) zdo, exportDataObjectGroup((DataObjectGroup) zdo, auPath));

				else
					exportLink(dogPath, auPath, "##DataObjectGroup##");
			} else if (zdo instanceof BinaryDataObject)
				exportBinaryDataObject((BinaryDataObject) zdo, auPath);
			else if (zdo instanceof PhysicalDataObject)
				exportPhysicalDataObject((PhysicalDataObject) zdo, auPath);
	}

	/**
	 * Export archive unit to disk.
	 *
	 * @param au            the ArchiveUnit
	 * @param containerPath the container path
	 * @throws SEDALibException     if writing has failed
	 * @throws InterruptedException the interrupted exception
	 */
	private void exportArchiveUnit(ArchiveUnit au, Path containerPath) throws SEDALibException, InterruptedException {
		Path targetOnDiskPath, auPath;

		if (auPathStringMap.containsKey(au))
			exportLink(auPathStringMap.get(au), containerPath, constructDirectoryName(au));
		else {
			// create directory
			targetOnDiskPath = containerPath.resolve(constructDirectoryName(au));
			try {
				Files.createDirectories(targetOnDiskPath);
			} catch (Exception e) {
				throw new SEDALibException("Création du répertoire [" + targetOnDiskPath + "] pour l'ArchiveUnit ["
						+ au.getInDataObjectPackageId() + "] impossible\n->" + e.getMessage());
			}
			auPath = targetOnDiskPath;
			// write metadata
			targetOnDiskPath = auPath.resolve("__ArchiveUnitMetadata.xml");
			if (filesPathSet.contains(targetOnDiskPath))
				throw new SEDALibException("Collision de fichiers de représentation [" + targetOnDiskPath.toString()
						+ "] détectée sur l'ArchiveUnit [" + au.getInDataObjectPackageId() + "]");
			filesPathSet.add(targetOnDiskPath);
			try {
				Files.write(targetOnDiskPath, au.toSedaXmlFragments().getBytes("UTF-8"));
			} catch (Exception e) {
				throw new SEDALibException("Ecriture des métadonnées de l'ArchiveUnit [" + au.getInDataObjectPackageId()
						+ "] impossible\n->" + e.getMessage());
			}
			// write all DataObjects
			exportDataObjectRefList(au.getDataObjectRefList(), auPath);

			auPathStringMap.put(au, auPath);

			for (ArchiveUnit childAu : au.getChildrenAuList().getArchiveUnitList())
				exportArchiveUnit(childAu, auPath);

			int counter = dataObjectPackage.getNextInOutCounter();
			if (sedaLibProgressLogger !=null)
				sedaLibProgressLogger.progressLogIfStep(SEDALibProgressLogger.OBJECTS_GROUP, counter,
						Integer.toString(counter) + " ArchiveUnit/DataObject exportés");
		}
	}

	/**
	 * Do export the DataObjectPackage to a disk hierarchy.
	 * <p>
	 * It will export in the output directory:
	 * <ul>
	 * <li>ManagementMetadata XML element at the end of DataObjectPackage in the
	 * __ManagementMetadata.xml file</li>
	 * <li>each root ArchiveUnit as a sub directory, and recursively all the
	 * DataObjectPackage structure</li>
	 * </ul>
	 *
	 * @param directoryName the export directory name
	 * @throws SEDALibException     if writing has failed
	 * @throws InterruptedException if export process is interrupted
	 */
	public void doExport(String directoryName) throws SEDALibException, InterruptedException {
		Path exportPath = Paths.get(directoryName);
		try {
			Files.createDirectories(exportPath);
		} catch (Exception e) {
			throw new SEDALibException(
					"Création du répertoire d'export [" + exportPath + "] impossible\n->" + e.getMessage());
		}

		dataObjectPackage.resetInOutCounter();
		determineExplicitDataObjectGroupMap();

		if (dataObjectPackage.getManagementMetadataXmlData() != null)
			exportManagementMetadata(dataObjectPackage.getManagementMetadataXmlData(), exportPath);
		for (ArchiveUnit au : dataObjectPackage.getGhostRootAu().getChildrenAuList().getArchiveUnitList())
			exportArchiveUnit(au, exportPath);

		if (sedaLibProgressLogger !=null)
			sedaLibProgressLogger.progressLog(SEDALibProgressLogger.OBJECTS_GROUP,
				Integer.toString(dataObjectPackage.getInOutCounter()) + " ArchiveUnit/DataObject exportées\n"
						+ dataObjectPackage.getDescription());
	}
}

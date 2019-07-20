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
package fr.gouv.vitam.tools.resip.data;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

import fr.gouv.vitam.tools.resip.parameters.CreationContext;
import fr.gouv.vitam.tools.resip.parameters.ExportContext;
import fr.gouv.vitam.tools.resip.utils.ResipLogger;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.core.DataObjectGroup;
import fr.gouv.vitam.tools.sedalib.core.PhysicalDataObject;
import fr.gouv.vitam.tools.sedalib.core.json.DataObjectPackageDeserializer;
import fr.gouv.vitam.tools.sedalib.core.json.DataObjectPackageSerializer;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;

/**
 * The Class Work.
 */
public class Work {

	// Inner Object

	/** The Constant CURRENT_SERIALIZATION_VERSION. */
	static final String CURRENT_SERIALIZATION_VERSION = "1.0";

	/**
	 * The version of this object used for to distinct serialization in prefs or on
	 * disk.
	 */
	@SuppressWarnings("FieldCanBeLocal")
    private String serializationVersion;

	/** The SIP export context. */
	private ExportContext exportContext;

	/** The creation context. */
	private CreationContext creationContext;

	/** The DataObjectPackage. */
	private DataObjectPackage dataObjectPackage;

	/**
	 * Instantiates a new work for json serialization.
	 */
	public Work() {
		this(null, null, null);
	}

	/**
	 * Instantiates a new work.
	 *
	 * @param dataObjectPackage the archive transfer
	 * @param creationContext   the creation context
	 * @param exportContext     the export context
	 */
	public Work(DataObjectPackage dataObjectPackage, CreationContext creationContext, ExportContext exportContext) {
		this.serializationVersion = CURRENT_SERIALIZATION_VERSION;
		this.dataObjectPackage = dataObjectPackage;
		this.creationContext = creationContext;
		this.exportContext = exportContext;
	}

	/**
	 * Do vitam normalize.
	 *
	 * @param spl the spl
	 * @return the string
	 * @throws SEDALibException the SEDA lib exception
	 * @throws InterruptedException the interrupted exception
	 */
	public String doVitamNormalize(SEDALibProgressLogger spl)
			throws SEDALibException, InterruptedException {
		Date d = new Date();
		Instant start = Instant.now();
		String log = "Début de la vérification de l'absence de cycle et de la normalisation Vitam de la structure\n";
		log += " date=" + DateFormat.getDateTimeInstance().format(d);
		spl.log(SEDALibProgressLogger.GLOBAL,log);

		dataObjectPackage.vitamNormalize(spl);
		
		spl.progressLog(SEDALibProgressLogger.STEP,
				Integer.toString(dataObjectPackage.getArchiveUnitCount()) + " ArchiveUnits normalisées");
		Instant end = Instant.now();
		String result = "Vérification de l'absence de cycle et normalisation de la structure conforme Vitam\n";
		result += "effectuée en " + Duration.between(start, end).toString().substring(2) + "\n";
		spl.log(SEDALibProgressLogger.GLOBAL,result);
		return result;
	}

	/**
	 * Creates the from file.
	 *
	 * @param file the file
	 * @return the work
	 * @throws SEDALibException the resip exception
	 */
	public static Work createFromFile(String file) throws SEDALibException {
		Work ow;
		try (ZipInputStream zis = new ZipInputStream(new FileInputStream(file))) {
			ObjectMapper mapper = new ObjectMapper();
			SimpleModule module = new SimpleModule();
			module.addSerializer(DataObjectPackage.class, new DataObjectPackageSerializer());
			module.addDeserializer(DataObjectPackage.class, new DataObjectPackageDeserializer());
			mapper.registerModule(module);
			ZipEntry ze = zis.getNextEntry();
			if (!ze.getName().equals("work.json"))
				throw new SEDALibException(
						"Resip.InOut: Le fichier [" + file + "] n'est pas une sauvegarde de session Resip");
			ow = mapper.readValue(zis, Work.class);

			// some fields need to be computed or defined after the load phase from Json
			ow.getDataObjectPackage().getGhostRootAu().setDataObjectPackage(ow.getDataObjectPackage());
			for (Map.Entry<String, ArchiveUnit> pair : ow.getDataObjectPackage().getAuInDataObjectPackageIdMap()
					.entrySet()) {
				pair.getValue().setDataObjectPackage(ow.getDataObjectPackage());
			}
			for (Map.Entry<String, DataObjectGroup> pair : ow.getDataObjectPackage().getDogInDataObjectPackageIdMap()
					.entrySet()) {
				DataObjectGroup dog = pair.getValue();
				dog.setDataObjectPackage(ow.getDataObjectPackage());
				for (BinaryDataObject bdo : dog.getBinaryDataObjectList()) {
					bdo.setDataObjectPackage(ow.getDataObjectPackage());
					bdo.setDataObjectGroup(dog);
				}
				for (PhysicalDataObject pdo : dog.getPhysicalDataObjectList()) {
					pdo.setDataObjectPackage(ow.getDataObjectPackage());
					pdo.setDataObjectGroup(dog);
				}
			}
		} catch (IOException e) {
			throw new SEDALibException("Resip.InOut: La lecture du fichier [" + file
					+ "] ne permet pas de retrouver une session Resip\n->" + e.getMessage());
		}
		return ow;
	}

	/**
	 * Save.
	 *
	 * @param file the file
	 */
	public void save(String file) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			SimpleModule module = new SimpleModule();
			module.addSerializer(DataObjectPackage.class, new DataObjectPackageSerializer());
			module.addDeserializer(DataObjectPackage.class, new DataObjectPackageDeserializer());
			mapper.registerModule(module);
			mapper.enable(SerializationFeature.INDENT_OUTPUT);
			ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file));
			ZipEntry e = new ZipEntry("work.json");
			zos.putNextEntry(e);
			mapper.writeValue(zos, this);
			zos.closeEntry();
			zos.close();
		} catch (IOException e) {
			ResipLogger.getGlobalLogger().log(ResipLogger.STEP,"Impossible de sauvegarder la session\n->" + e.getMessage());
		}

	}

	/**
	 * Gets the archive transfer.
	 *
	 * @return the archive transfer
	 */
	public DataObjectPackage getDataObjectPackage() {
		return dataObjectPackage;
	}

	/**
	 * Sets the archive transfer.
	 *
	 * @param dataObjectPackage the new archive transfer
	 */
	public void setDataObjectPackage(DataObjectPackage dataObjectPackage) {
		this.dataObjectPackage = dataObjectPackage;
	}

	/**
	 * Gets the creation context.
	 *
	 * @return the creation context
	 */
	public CreationContext getCreationContext() {
		return creationContext;
	}

	/**
	 * Sets the creation context.
	 *
	 * @param creationContext the new creation context
	 */
	public void setCreationContext(CreationContext creationContext) {
		this.creationContext = creationContext;
	}

	/**
	 * Gets the global metadata context.
	 *
	 * @return the global metadata context
	 */
	public ExportContext getExportContext() {
		return exportContext;
	}

	/**
	 * Sets the global metadata context.
	 *
	 * @param exportContext the new global metadata context
	 */
	public void setExportContext(ExportContext exportContext) {
		this.exportContext = exportContext;
	}
}

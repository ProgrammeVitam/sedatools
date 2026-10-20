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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import fr.gouv.vitam.tools.resip.data.NullDeserializer;
import fr.gouv.vitam.tools.resip.data.Work;
import fr.gouv.vitam.tools.resip.parameters.CreationContext;
import fr.gouv.vitam.tools.resip.parameters.ExportContext;
import fr.gouv.vitam.tools.resip.utils.ResipException;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.core.DataObjectGroup;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;
import fr.gouv.vitam.tools.sedalib.core.PhysicalDataObject;
import fr.gouv.vitam.tools.sedalib.core.json.DataObjectPackageDeserializer;
import fr.gouv.vitam.tools.sedalib.core.json.DataObjectPackageSerializer;
import fr.gouv.vitam.tools.sedaschema.SedaVersion;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Saves a work as a ReSIP session file, a zip holding its json, and loads it back, without any
 * user interface.
 */
public final class SessionService {

    /**
     * The name of the json of the work in a session file.
     */
    static final String JSON_FILENAME = "work.json";

    /**
     * The SEDA version of a session saved before the version was part of it.
     */
    public static final SedaVersion LEGACY_SEDA_VERSION = SedaVersion.V2_1;

    private SessionService() {}

    private static ObjectMapper mapper(SimpleModule module) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return mapper;
    }

    private static ZipInputStream openJson(ZipInputStream zis, String file) throws IOException, ResipException {
        ZipEntry ze = zis.getNextEntry();
        if ((ze == null) || !ze.getName().equals(JSON_FILENAME)) throw new ResipException(
            "Resip: Le fichier [" + file + "] n'est pas une sauvegarde de session Resip"
        );
        return zis;
    }

    private static ResipException unreadable(String file, IOException e) {
        return new ResipException(
            "Resip: La lecture du fichier [" + file + "] ne permet pas de retrouver une session Resip",
            e
        );
    }

    /**
     * Reads the SEDA version of a session file, without loading its content.
     *
     * @param file the session file
     * @return the SEDA version of the session, {@link #LEGACY_SEDA_VERSION} for an old one
     * @throws ResipException if the file is not a readable session
     */
    public static SedaVersion readSedaVersion(String file) throws ResipException {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(DataObjectPackage.class, new NullDeserializer<DataObjectPackage>());
        module.addDeserializer(ExportContext.class, new NullDeserializer<ExportContext>());
        module.addDeserializer(CreationContext.class, new NullDeserializer<CreationContext>());
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(file))) {
            Work work = mapper(module).readValue(openJson(zis, file), Work.class);
            return work.getVersion() == null ? LEGACY_SEDA_VERSION : work.getVersion();
        } catch (IOException e) {
            throw unreadable(file, e);
        }
    }

    /**
     * Loads a session file.
     *
     * @param file the session file
     * @return the work of the session
     * @throws ResipException if the file is not a readable session
     */
    public static Work load(String file) throws ResipException {
        // a DataObjectPackage saved before it held its version is in the one of the session
        SedaVersion savedVersion = readSedaVersion(file);
        SimpleModule module = new SimpleModule();
        module.addDeserializer(DataObjectPackage.class, new DataObjectPackageDeserializer());
        Work work;
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(file))) {
            work = mapper(module)
                .readerFor(Work.class)
                .withAttribute(DataObjectPackageDeserializer.SEDA_VERSION_ATTRIBUTE, savedVersion)
                .readValue(openJson(zis, file));
        } catch (IOException e) {
            throw unreadable(file, e);
        }
        reconnect(work.getDataObjectPackage());
        return work;
    }

    // the json doesn't hold the references of the elements to their package and group
    private static void reconnect(DataObjectPackage dataObjectPackage) {
        dataObjectPackage.getGhostRootAu().setDataObjectPackage(dataObjectPackage);
        for (ArchiveUnit au : dataObjectPackage.getAuInDataObjectPackageIdMap().values()) au.setDataObjectPackage(
            dataObjectPackage
        );
        for (DataObjectGroup dog : dataObjectPackage.getDogInDataObjectPackageIdMap().values()) {
            dog.setDataObjectPackage(dataObjectPackage);
            for (BinaryDataObject bdo : dog.getBinaryDataObjectList()) {
                bdo.setDataObjectPackage(dataObjectPackage);
                bdo.setDataObjectGroup(dog);
            }
            for (PhysicalDataObject pdo : dog.getPhysicalDataObjectList()) {
                pdo.setDataObjectPackage(dataObjectPackage);
                pdo.setDataObjectGroup(dog);
            }
        }
    }

    /**
     * Saves a work in a session file.
     *
     * @param work the work
     * @param file the session file, replaced if it exists
     * @throws ResipException if the file can't be written
     */
    public static void save(Work work, String file) throws ResipException {
        SimpleModule module = new SimpleModule();
        module.addSerializer(DataObjectPackage.class, new DataObjectPackageSerializer());
        ObjectMapper mapper = mapper(module);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file))) {
            zos.putNextEntry(new ZipEntry(JSON_FILENAME));
            mapper.writeValue(zos, work);
            zos.closeEntry();
        } catch (IOException e) {
            throw new ResipException("Resip: Impossible de sauvegarder la session dans [" + file + "]", e);
        }
    }
}

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
package fr.gouv.vitam.tools.resip.parameters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import fr.gouv.vitam.tools.resip.UseTestFiles;
import fr.gouv.vitam.tools.resip.data.Work;
import fr.gouv.vitam.tools.resip.utils.ResipException;
import fr.gouv.vitam.tools.resip.utils.ResipLogger;
import fr.gouv.vitam.tools.sedalib.core.ArchiveTransfer;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;
import fr.gouv.vitam.tools.sedalib.core.json.DataObjectPackageDeserializer;
import fr.gouv.vitam.tools.sedalib.core.json.DataObjectPackageSerializer;
import fr.gouv.vitam.tools.sedalib.inout.importer.DiskToArchiveTransferImporter;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The type Work test.
 */
class WorkTest implements UseTestFiles {

    private void setWorkFromArchiveTransfer(Work work, ArchiveTransfer archiveTransfer) {
        work.setDataObjectPackage(archiveTransfer.getDataObjectPackage());
        ExportContext newExportContext = new ExportContext();
        newExportContext.setDefaultPrefs();
        newExportContext.setArchiveTransferGlobalMetadata(archiveTransfer.getGlobalMetadata());
        newExportContext.setManagementMetadataXmlData(
            archiveTransfer.getDataObjectPackage().getManagementMetadataXmlData()
        );
        work.setExportContext(newExportContext);
    }

    /**
     * Test resip work serialization deserialization.
     *
     * @throws ResipException        the resip exception
     * @throws FileNotFoundException the file not found exception
     * @throws IOException           the io exception
     * @throws InterruptedException  the interrupted exception
     * @throws SEDALibException      the seda lib exception
     */
    @Test
    void TestResipWorkSerializationDeserialization()
        throws ResipException, FileNotFoundException, IOException, InterruptedException, SEDALibException {
        String destLog = "./target/tmpJunit/" + File.separator + "junit_log.log";

        SEDALibProgressLogger spl = new SEDALibProgressLogger(
            ResipLogger.getGlobalLogger().getLogger(),
            SEDALibProgressLogger.OBJECTS_GROUP
        );

        List<String> ignorePatternList = new ArrayList<>(2);
        ignorePatternList.add("Thumbs.db");
        ignorePatternList.add("pagefile.sys");
        ExportContext gmc = new ExportContext("src/test/resources/PacketSamples/ExportContext.config");
        CreationContext oic = new DiskImportContext(
            ignorePatternList,
            false,
            "src/test/resources/PacketSamples/SampleWithoutLinksModelV2",
            destLog
        );
        Work ow = new Work(null, oic, gmc);

        DiskToArchiveTransferImporter di = new DiskToArchiveTransferImporter(
            ow.getCreationContext().getOnDiskInput(),
            spl
        );
        for (String ip : ((DiskImportContext) ow.getCreationContext()).getIgnorePatternList()) di.addIgnorePattern(ip);
        di.doImport();
        ((DiskImportContext) ow.getCreationContext()).setModelVersion(di.getModelVersion());
        setWorkFromArchiveTransfer(ow, di.getArchiveTransfer());

        // assert macro results
        assertEquals(22, ow.getDataObjectPackage().getAuInDataObjectPackageIdMap().size());
        assertEquals(11, ow.getDataObjectPackage().getDogInDataObjectPackageIdMap().size());

        // create jackson object mapper
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(DataObjectPackage.class, new DataObjectPackageSerializer());
        module.addDeserializer(DataObjectPackage.class, new DataObjectPackageDeserializer());
        mapper.registerModule(module);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        String s = mapper.writeValueAsString(gmc);
        System.out.println(s);

        // assert archiveTransfer serialization/deserialization
        String ssip = mapper.writeValueAsString(ow);
        mapper.writeValue(new FileOutputStream("./target/tmpJunit/junit_resiptWork.json"), ow);
        Work dssip = mapper.readValue(ssip, Work.class);
        String sdssip = mapper.writeValueAsString(dssip);
        mapper.writeValue(new FileOutputStream("./target/tmpJunit/junit_resiptWork_after.json"), dssip);
        assertEquals(ssip, sdssip);
    }
}

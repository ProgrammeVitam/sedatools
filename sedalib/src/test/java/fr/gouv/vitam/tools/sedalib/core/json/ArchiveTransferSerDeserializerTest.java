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
package fr.gouv.vitam.tools.sedalib.core.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import fr.gouv.vitam.tools.sedalib.SedaContextExtension;
import fr.gouv.vitam.tools.sedalib.UseTestFiles;
import fr.gouv.vitam.tools.sedalib.core.ArchiveTransfer;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;
import fr.gouv.vitam.tools.sedalib.inout.importer.DiskToArchiveTransferImporter;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibJsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SedaContextExtension.class)
class ArchiveTransferSerDeserializerTest implements UseTestFiles {

    @Test
    void TestSipSerializationDeserialization() throws SEDALibException, IOException, InterruptedException {
        // do import of test directory
        DiskToArchiveTransferImporter di = new DiskToArchiveTransferImporter(
            "src/test/resources/PacketSamples" + "/SampleWithoutLinksModelV1",
            null
        );
        di.addIgnorePattern("Thumbs.db");
        di.addIgnorePattern("pagefile.sys");
        di.doImport();

        // assert macro results
        assertEquals(22, di.getArchiveTransfer().getDataObjectPackage().getAuInDataObjectPackageIdMap().size());
        assertEquals(11, di.getArchiveTransfer().getDataObjectPackage().getDogInDataObjectPackageIdMap().size());

        // create jackson object mapper
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(DataObjectPackage.class, new DataObjectPackageSerializer());
        module.addDeserializer(DataObjectPackage.class, new DataObjectPackageDeserializer());
        mapper.registerModule(module);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        // assert archiveTransfer serialization/deserialization
        String ssip = mapper.writeValueAsString(di.getArchiveTransfer());
        ArchiveTransfer dssip = mapper.readValue(ssip, ArchiveTransfer.class);
        String sdssip = mapper.writeValueAsString(dssip);
        //		mapper.writeValue(new FileOutputStream("target/tmpJunit/junit_sip.json"), di.getArchiveTransfer());
        //		mapper.writeValue(new FileOutputStream("target/tmpJunit/junit_sip_after.json"), dssip);
        assertEquals(ssip, sdssip);
    }

    @Test
    void TestDeserializationKO() throws SEDALibException, IOException, InterruptedException {
        // create jackson object mapper
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(DataObjectPackage.class, new DataObjectPackageSerializer());
        module.addDeserializer(DataObjectPackage.class, new DataObjectPackageDeserializer());
        mapper.registerModule(module);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        // assert archiveTransfer serialization/deserialization
        assertThatThrownBy(() -> mapper.readValue("{Toto}", ArchiveTransfer.class)).hasMessageContaining(
            "Unexpected character"
        );
    }

    @Test
    void TestSEDALibJsonProcessingException() {
        assertThatThrownBy(() -> {
            throw new SEDALibJsonProcessingException("Test it");
        }).hasMessage("Test it");
    }
}

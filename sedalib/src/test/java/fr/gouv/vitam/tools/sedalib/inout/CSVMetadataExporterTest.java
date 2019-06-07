package fr.gouv.vitam.tools.sedalib.inout;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import fr.gouv.vitam.tools.sedalib.TestUtilities;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;
import fr.gouv.vitam.tools.sedalib.core.json.DataObjectPackageDeserializer;
import fr.gouv.vitam.tools.sedalib.core.json.DataObjectPackageSerializer;
import fr.gouv.vitam.tools.sedalib.inout.exporter.DataObjectPackageToCSVMetadataExporter;
import fr.gouv.vitam.tools.sedalib.inout.importer.CSVMetadataToDataObjectPackageImporter;
import fr.gouv.vitam.tools.sedalib.inout.importer.DiskToArchiveTransferImporter;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.junit.jupiter.api.Test;

import static fr.gouv.vitam.tools.sedalib.TestUtilities.eraseAll;
import static fr.gouv.vitam.tools.sedalib.inout.exporter.DataObjectPackageToCSVMetadataExporter.ALL_DATAOBJECTS;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class CSVMetadataExporterTest {

	@Test
	void exportOK() throws SEDALibException, InterruptedException, JsonProcessingException {
		// do import of test directory
		DiskToArchiveTransferImporter di;
		di = new DiskToArchiveTransferImporter("src/test/resources/PacketSamples/SampleWithLinksModelV2", null);

		di.addIgnorePattern("Thumbs.db");
		di.addIgnorePattern("pagefile.sys");
		di.doImport();

		DataObjectPackageToCSVMetadataExporter cme;

		// When loaded with the csv OK test file
		eraseAll("target/tmpJunit/CSVMetadataExporter");
		cme= new DataObjectPackageToCSVMetadataExporter(di.getArchiveTransfer().getDataObjectPackage(),"UTF8",';',ALL_DATAOBJECTS,0,null);
		cme.doExport("target/tmpJunit/CSVMetadataExporter/metadata.csv");


//		assertThat(TestUtilities.LineEndNormalize(sau)).isEqualTo(TestUtilities.LineEndNormalize(testAu));
	}

}

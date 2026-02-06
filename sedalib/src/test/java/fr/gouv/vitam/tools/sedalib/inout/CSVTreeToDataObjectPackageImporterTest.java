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
package fr.gouv.vitam.tools.sedalib.inout;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import fr.gouv.vitam.tools.sedalib.SedaContextExtension;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;
import fr.gouv.vitam.tools.sedalib.core.json.DataObjectPackageDeserializer;
import fr.gouv.vitam.tools.sedalib.core.json.DataObjectPackageSerializer;
import fr.gouv.vitam.tools.sedalib.inout.importer.CSVTreeToDataObjectPackageImporter;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(SedaContextExtension.class)
class CSVTreeToDataObjectPackageImporterTest {

	@Test
	void importOKCSV() throws SEDALibException, InterruptedException, JsonProcessingException {
		// Given
		ObjectMapper mapper = new ObjectMapper();
		SimpleModule module = new SimpleModule();
		module.addSerializer(DataObjectPackage.class, new DataObjectPackageSerializer());
		module.addDeserializer(DataObjectPackage.class, new DataObjectPackageDeserializer());
		mapper.registerModule(module);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);

		CSVTreeToDataObjectPackageImporter cti;

		// When loaded with the csv OK test file
		cti= new CSVTreeToDataObjectPackageImporter(
				"src/test/resources/PacketSamples/TestOK.csv", "Cp1252",';',null);
		cti.doImport();

		// Then
		String testAu = "{\n" +
				"  \"archiveUnitProfileXmlData\" : null,\n" +
				"  \"managementXmlData\" : null,\n" +
				"  \"contentXmlData\" : \"<Content>\\n  <DescriptionLevel>Subseries</DescriptionLevel>\\n  <Title>Justice</Title>\\n  <OriginatingAgencyArchiveUnitIdentifier>REP.7.</OriginatingAgencyArchiveUnitIdentifier>\\n</Content>\",\n" +
				"  \"childrenAuList\" : {\n" +
				"    \"inDataObjectPackageIdList\" : [ \"ID36\", \"ID37\", \"ID38\", \"ID39\", \"ID40\", \"ID41\", \"ID42\", \"ID43\", \"ID44\", \"ID45\", \"ID46\", \"ID47\", \"ID48\", \"ID49\" ]\n" +
				"  },\n" +
				"  \"dataObjectRefList\" : {\n" +
				"    \"inDataObjectPackageIdList\" : [ ]\n" +
				"  },\n" +
				"  \"inDataObjectPackageId\" : \"ID35\",\n" +
				"  \"onDiskPath\" : null\n" +
				"}";
		ArchiveUnit au = cti.getDataObjectPackage().getArchiveUnitById("ID35");
		String sau = mapper.writeValueAsString(au);
		assertThat(sau).isEqualToNormalizingNewlines(testAu);
	}

	@Test
	void importKOCSV() throws SEDALibException {
		// Given
		ObjectMapper mapper = new ObjectMapper();
		SimpleModule module = new SimpleModule();
		module.addSerializer(DataObjectPackage.class, new DataObjectPackageSerializer());
		module.addDeserializer(DataObjectPackage.class, new DataObjectPackageDeserializer());
		mapper.registerModule(module);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);

		CSVTreeToDataObjectPackageImporter cti;

		// When loaded with the csv KO test file with a line out of all defined series
		cti= new CSVTreeToDataObjectPackageImporter(
				"src/test/resources/PacketSamples/TestKO1.csv", "Cp1252",';',null);

		// Test message identify the wrong line
		assertThatThrownBy(cti::doImport)
				.hasMessageContaining("2b"); // for StringType;
	}
}

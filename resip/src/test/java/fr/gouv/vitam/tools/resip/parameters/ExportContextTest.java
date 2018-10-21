package fr.gouv.vitam.tools.resip.parameters;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.nio.file.Files;
import java.nio.file.Paths;

import fr.gouv.vitam.tools.resip.TestUtilities;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

class ExportContextTest {

	@Test
	void test() throws Exception {
		ExportContext gmc=new ExportContext();
		gmc.setDefaultPrefs();
		
		// create jackson object mapper
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);

		String ssc = mapper.writeValueAsString(gmc);
		ExportContext reloadSipContext=mapper.readValue(ssc, ExportContext.class);
		String dssc = mapper.writeValueAsString(reloadSipContext);
		
		String fromfile = new String(Files.readAllBytes(Paths.get("src/test/resources/PacketSamples/ExportContext.config")),"UTF-8");
		
		assertThat(TestUtilities.LineEndNormalize(dssc)).isEqualTo(TestUtilities.LineEndNormalize(fromfile));
	}
}

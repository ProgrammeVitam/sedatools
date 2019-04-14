package fr.gouv.vitam.tools.resip.parameters;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fr.gouv.vitam.tools.resip.UseTestFiles;
import fr.gouv.vitam.tools.resip.utils.ResipLogger;
import fr.gouv.vitam.tools.sedalib.core.ArchiveTransfer;
import fr.gouv.vitam.tools.sedalib.inout.importer.DiskToArchiveTransferImporter;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

import fr.gouv.vitam.tools.sedalib.core.json.DataObjectPackageDeserializer;
import fr.gouv.vitam.tools.sedalib.core.json.DataObjectPackageSerializer;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.resip.data.Work;
import fr.gouv.vitam.tools.resip.utils.ResipException;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;

public class WorkTest implements UseTestFiles {

	private void setWorkFromArchiveTransfer(Work work, ArchiveTransfer archiveTransfer) {
		work.setDataObjectPackage(archiveTransfer.getDataObjectPackage());
		ExportContext newExportContext = new ExportContext(Prefs.getInstance().getPrefsContextNode());
		newExportContext.setArchiveTransferGlobalMetadata(archiveTransfer.getGlobalMetadata());
		newExportContext.setManagementMetadataXmlData(
				archiveTransfer.getDataObjectPackage().getManagementMetadataXmlData());
		work.setExportContext(newExportContext);
	}

	@Test
	void TestResipWorkSerializationDeserialization() throws ResipException, FileNotFoundException, IOException, InterruptedException, SEDALibException  {
		String destLog = "./target/tmpJunit/" + File.separator + "junit_log.log";

		SEDALibProgressLogger spl= new SEDALibProgressLogger(ResipLogger.getGlobalLogger().getLogger(), SEDALibProgressLogger.OBJECTS_GROUP);
		
		List<String> ignorePatternList=new ArrayList<String>(2);
		ignorePatternList.add("Thumbs.db");
		ignorePatternList.add("pagefile.sys");
		ExportContext gmc=new ExportContext("src/test/resources/PacketSamples/ExportContext.config");
		CreationContext oic=new DiskImportContext(ignorePatternList,false, "src/test/resources/PacketSamples/SampleWithoutLinksModelV2", destLog);
		Work ow=new Work(null,oic,gmc);

		DiskToArchiveTransferImporter di = new DiskToArchiveTransferImporter(ow.getCreationContext().getOnDiskInput(),
				spl);
		for (String ip : ((DiskImportContext) ow.getCreationContext()).getIgnorePatternList())
			di.addIgnorePattern(ip);
		di.doImport();
		((DiskImportContext) ow.getCreationContext()).setModelVersion(di.getModelVersion());
		setWorkFromArchiveTransfer(ow,di.getArchiveTransfer());

		// assert macro results
		assertEquals(ow.getDataObjectPackage().getAuInDataObjectPackageIdMap().size(),22);
		assertEquals(ow.getDataObjectPackage().getDogInDataObjectPackageIdMap().size(),11);

		// create jackson object mapper
		ObjectMapper mapper = new ObjectMapper();
		SimpleModule module = new SimpleModule();
		module.addSerializer(DataObjectPackage.class, new DataObjectPackageSerializer());
		module.addDeserializer(DataObjectPackage.class, new DataObjectPackageDeserializer());
		mapper.registerModule(module);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);

		String s= mapper.writeValueAsString(gmc);
		System.out.println(s);

		// assert archiveTransfer serialization/deserialization
		String ssip = mapper.writeValueAsString(ow);
		mapper.writeValue(new FileOutputStream("./target/tmpJunit/junit_resiptWork.json"), ow);
		Work dssip = mapper.readValue(ssip, Work.class);
		String sdssip = mapper.writeValueAsString(dssip);
		mapper.writeValue(new FileOutputStream("./target/tmpJunit/junit_resiptWork_after.json"), dssip);
		assertEquals(ssip,sdssip);
		
	}

}

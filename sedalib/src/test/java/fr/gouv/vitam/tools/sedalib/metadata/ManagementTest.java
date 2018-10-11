package fr.gouv.vitam.tools.sedalib.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.jupiter.api.Test;

import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

class ManagementTest {

	static SimpleDateFormat daySdf = new SimpleDateFormat("yyyy-MM-dd");

	@Test
	void testConstructors() throws SEDALibException {
		Management m = new Management();

		AccessRule accessRule = new AccessRule();
		accessRule.addRule("TestAccRule1", daySdf.format(new Date(0)));
		accessRule.setPreventInheritance(true);
		accessRule.addRule("TestAccRule2");
		accessRule.addRefNonRuleId("TestAccRule3");

		AppraisalRule appraisalRule = new AppraisalRule();
		appraisalRule.addRule("TestAppRule1", daySdf.format(new Date(0)));
		appraisalRule.setPreventInheritance(true);
		appraisalRule.addRule("TestAppRule2");
		appraisalRule.addRefNonRuleId("TestAppRule3");
		appraisalRule.setFinalAction("Keep");

		ClassificationRule classificationRule = new ClassificationRule();
		classificationRule.addRule("TestRule1", daySdf.format(new Date(0)));
		classificationRule.setPreventInheritance(true);
		classificationRule.addRule("TestRule2");
		classificationRule.addRefNonRuleId("TestRule3");
		classificationRule.setClassificationLevel("TestCD");
		classificationRule.setClassificationOwner("TestOwner");
		classificationRule.setClassificationReassessingDate(daySdf.format(new Date(0)));
		classificationRule.setNeedReassessingAuthorization(true);

		DisseminationRule disseminationRule = new DisseminationRule();
		disseminationRule.addRule("TestDisRule1", daySdf.format(new Date(0)));
		disseminationRule.setPreventInheritance(true);
		disseminationRule.addRule("TestDisRule2");
		disseminationRule.addRefNonRuleId("TestDisRule3");

		ReuseRule reuseRule = new ReuseRule();
		reuseRule.addRule("TestReuRule1", daySdf.format(new Date(0)));
		reuseRule.setPreventInheritance(true);
		reuseRule.addRule("TestReuRule2");
		reuseRule.addRefNonRuleId("TestReuRule3");

		StorageRule storageRule = new StorageRule();
		storageRule.addRule("TestStoRule1", daySdf.format(new Date(0)));
		storageRule.setPreventInheritance(true);
		storageRule.addRule("TestStoRule2");
		storageRule.addRefNonRuleId("TestStoRule3");
		storageRule.setFinalAction("Copy");

		UpdateOperation updateOperation = new UpdateOperation();
		updateOperation.setMetadata("TestMetadataName", "TestMetadataValue");

		m.addMetadata(updateOperation);
		m.addMetadata(accessRule);
		m.addMetadata(appraisalRule);
		m.addMetadata(classificationRule);
		m.addMetadata(disseminationRule);
		m.addMetadata(reuseRule);
		m.addMetadata(storageRule);

		String mOut = m.toString();
		System.out.println(mOut);
		Management mNext = (Management) SEDAMetadata.fromString(mOut, Management.class);
		String mNextOut = mNext.toString();
		System.out.println(mNextOut);

		assertEquals(mOut, mNextOut);

		String testOut = "<Management>\n" + "  <StorageRule>\n" + "    <Rule>TestStoRule1</Rule>\n"
				+ "    <StartDate>1970-01-01</StartDate>\n" + "    <Rule>TestStoRule2</Rule>\n"
				+ "    <PreventInheritance>true</PreventInheritance>\n"
				+ "    <RefNonRuleId>TestStoRule3</RefNonRuleId>\n" + "    <FinalAction>Copy</FinalAction>\n"
				+ "  </StorageRule>\n" + "  <AppraisalRule>\n" + "    <Rule>TestAppRule1</Rule>\n"
				+ "    <StartDate>1970-01-01</StartDate>\n" + "    <Rule>TestAppRule2</Rule>\n"
				+ "    <PreventInheritance>true</PreventInheritance>\n"
				+ "    <RefNonRuleId>TestAppRule3</RefNonRuleId>\n" + "    <FinalAction>Keep</FinalAction>\n"
				+ "  </AppraisalRule>\n" + "  <AccessRule>\n" + "    <Rule>TestAccRule1</Rule>\n"
				+ "    <StartDate>1970-01-01</StartDate>\n" + "    <Rule>TestAccRule2</Rule>\n"
				+ "    <PreventInheritance>true</PreventInheritance>\n"
				+ "    <RefNonRuleId>TestAccRule3</RefNonRuleId>\n" + "  </AccessRule>\n" + "  <DisseminationRule>\n"
				+ "    <Rule>TestDisRule1</Rule>\n" + "    <StartDate>1970-01-01</StartDate>\n"
				+ "    <Rule>TestDisRule2</Rule>\n" + "    <PreventInheritance>true</PreventInheritance>\n"
				+ "    <RefNonRuleId>TestDisRule3</RefNonRuleId>\n" + "  </DisseminationRule>\n" + "  <ReuseRule>\n"
				+ "    <Rule>TestReuRule1</Rule>\n" + "    <StartDate>1970-01-01</StartDate>\n"
				+ "    <Rule>TestReuRule2</Rule>\n" + "    <PreventInheritance>true</PreventInheritance>\n"
				+ "    <RefNonRuleId>TestReuRule3</RefNonRuleId>\n" + "  </ReuseRule>\n" + "  <ClassificationRule>\n"
				+ "    <Rule>TestRule1</Rule>\n" + "    <StartDate>1970-01-01</StartDate>\n"
				+ "    <Rule>TestRule2</Rule>\n" + "    <PreventInheritance>true</PreventInheritance>\n"
				+ "    <RefNonRuleId>TestRule3</RefNonRuleId>\n"
				+ "    <ClassificationLevel>TestCD</ClassificationLevel>\n"
				+ "    <ClassificationOwner>TestOwner</ClassificationOwner>\n"
				+ "    <ClassificationReassessingDate>1970-01-01</ClassificationReassessingDate>\n"
				+ "    <NeedReassessingAuthorization>true</NeedReassessingAuthorization>\n"
				+ "  </ClassificationRule>\n" + "  <UpdateOperation>\n" + "    <ArchiveUnitIdentifierKey>\n"
				+ "      <MetadataName>TestMetadataName</MetadataName>\n"
				+ "      <MetadataValue>TestMetadataValue</MetadataValue>\n" + "    </ArchiveUnitIdentifierKey>\n"
				+ "  </UpdateOperation>\n" + "</Management>";

		assertEquals(testOut, mNextOut);
	}

	@Test
	void testAddNewMetadata() throws SEDALibException {
		Management m = new Management();

		m.addNewMetadata("AccessRule", "TestAccRule1", daySdf.format(new Date(0)));
		m.addNewMetadata("AppraisalRule", "TestAppRule1", daySdf.format(new Date(0)), "Keep");
		m.addNewMetadata("ClassificationRule", "TestCD", "TestOwner");
		m.addNewMetadata("DisseminationRule", "TestDisRule1", daySdf.format(new Date(0)));
		m.addNewMetadata("ReuseRule", "TestReuRule1", daySdf.format(new Date(0)));
		m.addNewMetadata("StorageRule", "TestStoRule1", daySdf.format(new Date(0)), "Copy");
		m.addNewMetadata("UpdateOperation", "TestMetadataName", "TestMetadataValue");

		String mOut = m.toString();
		System.out.println(mOut);
		Management mNext = (Management) SEDAMetadata.fromString(mOut, Management.class);
		String mNextOut = mNext.toString();
		System.out.println(mNextOut);

		assertEquals(mOut, mNextOut);

		String testOut = "<Management>\n" + "  <StorageRule>\n" + "    <Rule>TestStoRule1</Rule>\n"
				+ "    <StartDate>1970-01-01</StartDate>\n" + "    <FinalAction>Copy</FinalAction>\n"
				+ "  </StorageRule>\n" + "  <AppraisalRule>\n" + "    <Rule>TestAppRule1</Rule>\n"
				+ "    <StartDate>1970-01-01</StartDate>\n" + "    <FinalAction>Keep</FinalAction>\n"
				+ "  </AppraisalRule>\n" + "  <AccessRule>\n" + "    <Rule>TestAccRule1</Rule>\n"
				+ "    <StartDate>1970-01-01</StartDate>\n" + "  </AccessRule>\n" + "  <DisseminationRule>\n"
				+ "    <Rule>TestDisRule1</Rule>\n" + "    <StartDate>1970-01-01</StartDate>\n"
				+ "  </DisseminationRule>\n" + "  <ReuseRule>\n" + "    <Rule>TestReuRule1</Rule>\n"
				+ "    <StartDate>1970-01-01</StartDate>\n" + "  </ReuseRule>\n" + "  <ClassificationRule>\n"
				+ "    <ClassificationLevel>TestCD</ClassificationLevel>\n"
				+ "    <ClassificationOwner>TestOwner</ClassificationOwner>\n" + "  </ClassificationRule>\n"
				+ "  <UpdateOperation>\n" + "    <ArchiveUnitIdentifierKey>\n"
				+ "      <MetadataName>TestMetadataName</MetadataName>\n"
				+ "      <MetadataValue>TestMetadataValue</MetadataValue>\n" + "    </ArchiveUnitIdentifierKey>\n"
				+ "  </UpdateOperation>\n" + "</Management>";

		assertEquals(testOut, mNextOut);
	}

}

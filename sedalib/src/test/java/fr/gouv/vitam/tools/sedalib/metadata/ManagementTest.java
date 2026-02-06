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
package fr.gouv.vitam.tools.sedalib.metadata;

import fr.gouv.vitam.tools.sedalib.SedaContextExtension;
import fr.gouv.vitam.tools.sedalib.metadata.management.*;
import fr.gouv.vitam.tools.sedalib.utils.ResourceUtils;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@ExtendWith(SedaContextExtension.class)
class ManagementTest {

	@Test
	void testConstructors() throws SEDALibException, FileNotFoundException {
		// Given
		Management m = new Management();

		// When loaded with all different kind of metadata

		// Test StorageRule and RuleType subclass newmetadata
		StorageRule storageRule = new StorageRule("TestStoRule1",LocalDate.of(1970,1,1),"Transfer");
		m.addMetadata(storageRule);

		// Test AppraisalRule and RuleType subclass metadata add
		AppraisalRule appraisalRule = new AppraisalRule();
		appraisalRule.addRule("TestAppRule1", LocalDate.of(1970,1,1));
		appraisalRule.setPreventInheritance(true);
		appraisalRule.addRule("TestAppRule2");
		appraisalRule.addRefNonRuleId("TestAppRule3");
		appraisalRule.setFinalAction("Keep");
		m.addMetadata(appraisalRule);

		// Test AccessRule and RuleType subclass metadata add
		AccessRule accessRule = new AccessRule();
		accessRule.addRule("TestAccRule1", LocalDate.of(1970,1,1));
		accessRule.setPreventInheritance(true);
		accessRule.addRule("TestAccRule2");
		accessRule.addRefNonRuleId("TestAccRule3");
		m.addMetadata(accessRule);

		// Test DisseminationRule and RuleType subclass metadata add
		DisseminationRule disseminationRule = new DisseminationRule();
		disseminationRule.addRule("TestDisRule1", LocalDate.of(1970,1,1));
		disseminationRule.setPreventInheritance(true);
		disseminationRule.addRule("TestDisRule2");
		disseminationRule.addRefNonRuleId("TestDisRule3");
		m.addMetadata(disseminationRule);

		// Test ReuseRule and RuleType subclass metadata add
		ReuseRule reuseRule = new ReuseRule();
		reuseRule.addRule("TestReuRule1", LocalDate.of(1970,1,1));
		reuseRule.setPreventInheritance(true);
		reuseRule.addRule("TestReuRule2");
		reuseRule.addRefNonRuleId("TestReuRule3");
		m.addMetadata(reuseRule);

		// Test ClassificationRule and RuleType subclass metadata add
		ClassificationRule classificationRule = new ClassificationRule();
		classificationRule.addRule("TestRule1", LocalDate.of(1970,1,1));
		classificationRule.setPreventInheritance(true);
		classificationRule.addRule("TestRule2");
		classificationRule.addRefNonRuleId("TestRule3");
		classificationRule.addNewMetadata("ClassificationLevel","TestCD");
		classificationRule.addNewMetadata("ClassificationOwner","TestOwner");
		classificationRule.addNewMetadata("ClassificationReassessingDate",LocalDate.of(1970,1,1));
		classificationRule.addNewMetadata("NeedReassessingAuthorization",true);
		m.addMetadata(classificationRule);

		// Test HoldRule and RuleType subclass metadata add
		HoldRule holdRule = new HoldRule();
		holdRule.addRule("TestHoldRule1", LocalDate.of(1970, 1, 1));
		holdRule.addNewMetadata("HoldOwner", "TestHoldOwner1");
		holdRule.setPreventInheritance(true);
		holdRule.addRule("TestHoldRule2");
		holdRule.addRefNonRuleId("TestHoldRule4");
		holdRule.addNewMetadata("HoldEndDate", LocalDate.of(1970, 1, 1));
		holdRule.addNewMetadata("HoldOwner", "TestHoldOwner2");
		holdRule.addNewMetadata("HoldReassessingDate", LocalDate.of(1970, 1, 1));
		holdRule.addNewMetadata("PreventRearrangement", false);
		holdRule.addRule("TestHoldRule3");
		m.addMetadata(holdRule);

		// Test LogBook
		LogBook logBook=new LogBook();
		logBook.addNewMetadata("Event","ID-00001","Request",LocalDateTime.of(2000,1,1,13,10),"OK");
		m.addMetadata(logBook);

		// Test GenerixXMLBlock
		m.addNewMetadata("RawXML","<Try>Here</Try>");

		// Test UpdateOperation
		UpdateOperation updateOperation = new UpdateOperation("TestSystemId");
		m.addMetadata(updateOperation);
		UpdateOperation updateOperation2 = new UpdateOperation("TestMetadataName", "TestMetadataValue"); // verify that uniq metadata is overwritten
		m.addMetadata(updateOperation2);

		String mOut = m.toString();

		// Test read write in XML string format
		Management mNext = (Management) SEDAMetadata.fromString(mOut, Management.class);
		String mNextOut = mNext.toString();

		// Then
        String testOut = ResourceUtils.getResourceAsString("metadata/metadate_out.xml");
		assertThat(mNextOut).isEqualToIgnoringWhitespace(testOut);
	}

	@Test
	void testAddNewMetadata() throws SEDALibException {
		// Given
		Management m = new Management();

		// When loaded with all different kind of metadata

		// Test RuleType subclass metadata add
		m.addNewMetadata("AccessRule","TestAccessRule",LocalDate.of(1970,1,1));
		m.addNewMetadata("AppraisalRule","TestAppraisalRule",LocalDate.of(1970,1,1)); // no value in result
		m.addNewMetadata("AppraisalRule","TestAppraisalRule",LocalDate.of(1970,1,1),"Keep");
		m.addNewMetadata("ClassificationRule","TestLevel","TestOwner");
		m.addNewMetadata("DisseminationRule","TestDisseminationRule",LocalDate.of(1970,1,1));
		m.addNewMetadata("ReuseRule","TestReuseRule",LocalDate.of(1970,1,1));
		m.addNewMetadata("StorageRule","TestStorageRule",LocalDate.of(1970,1,1),"Copy");
		m.addNewMetadata("HoldRule", "TestHoldRule", LocalDate.of(1970, 1, 1),
			Collections.singletonMap("HoldEndDate", LocalDate.of(1970, 1, 1)));

		// Test UpdateOperation
		m.addNewMetadata("UpdateOperation","TestMetadataName","TestMetadataValue");

		String mOut = m.toString();

		// Test read write in XML string format
		Management mNext = (Management) SEDAMetadata.fromString(mOut, Management.class);
		String mNextOut = mNext.toString();

		// Then
		String testOut = "<Management>\n" +
				"  <StorageRule>\n" +
				"    <Rule>TestStorageRule</Rule>\n" +
				"    <StartDate>1970-01-01</StartDate>\n" +
				"    <FinalAction>Copy</FinalAction>\n" +
				"  </StorageRule>\n" +
				"  <AppraisalRule>\n" +
				"    <Rule>TestAppraisalRule</Rule>\n" +
				"    <StartDate>1970-01-01</StartDate>\n" +
				"    <FinalAction>Keep</FinalAction>\n" +
				"  </AppraisalRule>\n" +
				"  <AccessRule>\n" +
				"    <Rule>TestAccessRule</Rule>\n" +
				"    <StartDate>1970-01-01</StartDate>\n" +
				"  </AccessRule>\n" +
				"  <DisseminationRule>\n" +
				"    <Rule>TestDisseminationRule</Rule>\n" +
				"    <StartDate>1970-01-01</StartDate>\n" +
				"  </DisseminationRule>\n" +
				"  <ReuseRule>\n" +
				"    <Rule>TestReuseRule</Rule>\n" +
				"    <StartDate>1970-01-01</StartDate>\n" +
				"  </ReuseRule>\n" +
				"  <ClassificationRule>\n" +
				"    <ClassificationLevel>TestLevel</ClassificationLevel>\n" +
				"    <ClassificationOwner>TestOwner</ClassificationOwner>\n" +
				"  </ClassificationRule>\n" +
				"  <HoldRule>\n" +
				"    <Rule>TestHoldRule</Rule>\n" +
				"    <StartDate>1970-01-01</StartDate>\n" +
				"    <HoldEndDate>1970-01-01</HoldEndDate>\n" +
				"  </HoldRule>\n" +
				"  <UpdateOperation>\n" +
				"    <ArchiveUnitIdentifierKey>\n" +
				"      <MetadataName>TestMetadataName</MetadataName>\n" +
				"      <MetadataValue>TestMetadataValue</MetadataValue>\n" +
				"    </ArchiveUnitIdentifierKey>\n" +
				"  </UpdateOperation>\n" +
				"</Management>";
		assertThat(mNextOut).isEqualTo(testOut);
	}
}

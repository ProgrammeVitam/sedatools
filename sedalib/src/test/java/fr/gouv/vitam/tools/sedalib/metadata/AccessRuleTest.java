package fr.gouv.vitam.tools.sedalib.metadata;

import static org.junit.jupiter.api.Assertions.*;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.jupiter.api.Test;

class AccessRuleTest {

	static SimpleDateFormat daySdf = new SimpleDateFormat("yyyy-MM-dd");

	@Test
	void test() {
		AccessRule accessRule = new AccessRule();

		accessRule.addRule("TestRule1", daySdf.format(new Date(0)));
		accessRule.setPreventInheritance(true);
		accessRule.addRule("TestRule2");
		accessRule.addRefNonRuleId("TestRule3");
// 		sample out		
		System.out.println(accessRule.toString());

		String testar = "<AccessRule>\n" + "  <Rule>TestRule1</Rule>\n"
				+ "  <StartDate>1970-01-01</StartDate>\n" + "  <Rule>TestRule2</Rule>\n"
				+ "  <PreventInheritance>true</PreventInheritance>\n" + "  <RefNonRuleId>TestRule3</RefNonRuleId>\n"
				+ "</AccessRule>";
		assertEquals(testar, accessRule.toString());

		accessRule = new AccessRule();
		accessRule.addRule("TestRule1", daySdf.format(new Date(0)));
		accessRule.setPreventInheritance(true);
		accessRule.addRule("TestRule2");
		accessRule.addRefNonRuleId("TestRule3");

		testar = "<AccessRule>\n" + "  <Rule>TestRule1</Rule>\n" + "  <StartDate>1970-01-01</StartDate>\n"
				+ "  <Rule>TestRule2</Rule>\n" + "  <PreventInheritance>true</PreventInheritance>\n"
				+ "  <RefNonRuleId>TestRule3</RefNonRuleId>\n" + "</AccessRule>";
		assertEquals(testar, accessRule.toString());

		accessRule = new AccessRule();
		accessRule.addRule("TestRule2");
		accessRule.addRule("TestRule1", daySdf.format(new Date(0)));

		testar = "<AccessRule>\n" + "  <Rule>TestRule2</Rule>\n" + "  <Rule>TestRule1</Rule>\n"
				+ "  <StartDate>1970-01-01</StartDate>\n" + "</AccessRule>";
		assertEquals(testar, accessRule.toString());

	}

}

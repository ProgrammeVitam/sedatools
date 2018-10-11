package fr.gouv.vitam.tools.sedalib.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

class GlobalMetadataTest {

	static public Logger createLogger(Level logLevel) {
		Logger logger;

		Properties props = System.getProperties();
		props.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s%n");// "[%1$tc] %4$s: %5$s%n");
		logger = Logger.getLogger("SEDALibTest");
		logger.setLevel(logLevel);

		return logger;
	}

	static String XMLStrip(String xmlData) {
	StringBuilder sb=new StringBuilder();
	boolean inString=false;
	
	char[] chars = xmlData.toCharArray();
	for (int i = 0, n = chars.length; i < n; i++) {
	    char c = chars[i];
		if (inString && (c=='"'))
			inString=!inString;
		else if (c=='\\')
				i++;
		else if (!inString && Character.isWhitespace(c))
			continue;
		sb.append(c);
		}
		return sb.toString();
	}

	@Test
	void test() throws SEDALibException {
		GlobalMetadata atgm;
		String atgmXmlData = "  <Comment>Avec valeurs utilisables sur environnement de démo Vitam V2</Comment>\r\n" + 
				"  <Date>2018-09-15T01:38:56</Date>\r\n" + 
				"  <MessageIdentifier>SIP SEDA de test V2</MessageIdentifier>\r\n" + 
				"  <ArchivalAgreement>ArchivalAgreement0</ArchivalAgreement>  <CodeListVersions>\r\n" + 
				"    <ReplyCodeListVersion>ReplyCodeListVersion0</ReplyCodeListVersion>\r\n" + 
				"    <MessageDigestAlgorithmCodeListVersion>MessageDigestAlgorithmCodeListVersion0</MessageDigestAlgorithmCodeListVersion>\r\n" + 
				"    <MimeTypeCodeListVersion>MimeTypeCodeListVersion0</MimeTypeCodeListVersion>\r\n" + 
				"    <EncodingCodeListVersion>EncodingCodeListVersion0</EncodingCodeListVersion>\r\n" + 
				"    <FileFormatCodeListVersion>FileFormatCodeListVersion0</FileFormatCodeListVersion>\r\n" + 
				"    <CompressionAlgorithmCodeListVersion>CompressionAlgorithmCodeListVersion0</CompressionAlgorithmCodeListVersion>\r\n" + 
				"    <DataObjectVersionCodeListVersion>DataObjectVersionCodeListVersion0</DataObjectVersionCodeListVersion>\r\n" + 
				"    <StorageRuleCodeListVersion>StorageRuleCodeListVersion0</StorageRuleCodeListVersion>\r\n" + 
				"    <AppraisalRuleCodeListVersion>AppraisalRuleCodeListVersion0</AppraisalRuleCodeListVersion>\r\n" + 
				"    <AccessRuleCodeListVersion>AccessRuleCodeListVersion0</AccessRuleCodeListVersion>\r\n" + 
				"    <DisseminationRuleCodeListVersion>DisseminationRuleCodeListVersion0</DisseminationRuleCodeListVersion>\r\n" + 
				"    <ReuseRuleCodeListVersion>ReuseRuleCodeListVersion0</ReuseRuleCodeListVersion>\r\n" + 
				"    <ClassificationRuleCodeListVersion>ClassificationRuleCodeListVersion0</ClassificationRuleCodeListVersion>\r\n" + 
				"    <AuthorizationReasonCodeListVersion>AuthorizationReasonCodeListVersion0</AuthorizationReasonCodeListVersion>\r\n" + 
				"    <RelationshipCodeListVersion>RelationshipCodeListVersion0</RelationshipCodeListVersion>\r\n" + 
				"  </CodeListVersions>\r\n" + 
				"  <TransferRequestReplyIdentifier>Identifier3</TransferRequestReplyIdentifier>\r\n" + 
				"  <ArchivalAgency>\r\n" + 
				"    <Identifier>Identifier4</Identifier>\r\n" + 
				"  </ArchivalAgency>\r\n" + 
				"  <TransferringAgency>\r\n" + 
				"    <Identifier>Identifier5</Identifier>\r\n" + 
				"  </TransferringAgency>";

		atgm=new GlobalMetadata();
		atgm.fromSedaXmlFragments(atgmXmlData);
		String out = atgm.toSedaXmlFragments();
		assertEquals(XMLStrip(atgmXmlData), XMLStrip(out));
	}
}

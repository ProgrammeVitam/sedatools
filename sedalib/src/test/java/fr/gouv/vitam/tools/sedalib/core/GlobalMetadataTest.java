package fr.gouv.vitam.tools.sedalib.core;

import static fr.gouv.vitam.tools.sedalib.TestUtilities.LineEndNormalize;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;

import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

class GlobalMetadataTest {

	@Test
	void test() throws SEDALibException {

		// Given
		GlobalMetadata gm;
		String gmFragments = "  <Comment>Avec valeurs utilisables sur environnement de démo Vitam V2</Comment>\r\n" +
				"  <Date>2018-09-15T01:38:56</Date>\r\n" + 
				"  <MessageIdentifier>SIP SEDA de test V2</MessageIdentifier>\r\n" + 
				"  <ArchivalAgreement>ArchivalAgreement0</ArchivalAgreement>\r\n" +
				"  <CodeListVersions>\r\n" +
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

		gm=new GlobalMetadata();

		// When test read write fragments in XML string format
		gm.fromSedaXmlFragments(gmFragments);
		String gmOut = gm.toSedaXmlFragments();

		// Then
		assertThat(LineEndNormalize(gmOut)).isEqualTo(LineEndNormalize(gmFragments));
	}
}

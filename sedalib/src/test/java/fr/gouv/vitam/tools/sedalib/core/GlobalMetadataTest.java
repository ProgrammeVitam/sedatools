package fr.gouv.vitam.tools.sedalib.core;

import fr.gouv.vitam.tools.sedalib.SedaContextExtension;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(SedaContextExtension.class)
class GlobalMetadataTest {

	@Test
	void test() throws SEDALibException {

		// Given
		GlobalMetadata gm;
		String gmFragments = "<Comment>Avec valeurs utilisables sur environnement de démo Vitam V2</Comment>\n" +
				"<Date>2018-09-15T01:38:56</Date>\n" +
				"<MessageIdentifier>SIP SEDA de test V2</MessageIdentifier>\n" +
				"<ArchivalAgreement>ArchivalAgreement0</ArchivalAgreement>\n" +
				"<CodeListVersions>\n" +
				"  <ReplyCodeListVersion>ReplyCodeListVersion0</ReplyCodeListVersion>\n" +
				"  <MessageDigestAlgorithmCodeListVersion>MessageDigestAlgorithmCodeListVersion0</MessageDigestAlgorithmCodeListVersion>\n" +
				"  <MimeTypeCodeListVersion>MimeTypeCodeListVersion0</MimeTypeCodeListVersion>\n" +
				"  <EncodingCodeListVersion>EncodingCodeListVersion0</EncodingCodeListVersion>\n" +
				"  <FileFormatCodeListVersion>FileFormatCodeListVersion0</FileFormatCodeListVersion>\n" +
				"  <CompressionAlgorithmCodeListVersion>CompressionAlgorithmCodeListVersion0</CompressionAlgorithmCodeListVersion>\n" +
				"  <DataObjectVersionCodeListVersion>DataObjectVersionCodeListVersion0</DataObjectVersionCodeListVersion>\n" +
				"  <StorageRuleCodeListVersion>StorageRuleCodeListVersion0</StorageRuleCodeListVersion>\n" +
				"  <AppraisalRuleCodeListVersion>AppraisalRuleCodeListVersion0</AppraisalRuleCodeListVersion>\n" +
				"  <AccessRuleCodeListVersion>AccessRuleCodeListVersion0</AccessRuleCodeListVersion>\n" +
				"  <DisseminationRuleCodeListVersion>DisseminationRuleCodeListVersion0</DisseminationRuleCodeListVersion>\n" +
				"  <ReuseRuleCodeListVersion>ReuseRuleCodeListVersion0</ReuseRuleCodeListVersion>\n" +
				"  <ClassificationRuleCodeListVersion>ClassificationRuleCodeListVersion0</ClassificationRuleCodeListVersion>\n" +
				"  <AuthorizationReasonCodeListVersion>AuthorizationReasonCodeListVersion0</AuthorizationReasonCodeListVersion>\n" +
				"  <RelationshipCodeListVersion>RelationshipCodeListVersion0</RelationshipCodeListVersion>\n" +
				"</CodeListVersions>\n" +
				"<TransferRequestReplyIdentifier>Identifier3</TransferRequestReplyIdentifier>\n" +
				"<ArchivalAgency>\n" +
				"  <Identifier>Identifier4</Identifier>\n" +
				"</ArchivalAgency>\n" +
				"<TransferringAgency>\n" +
				"  <Identifier>Identifier5</Identifier>\n" +
				"</TransferringAgency>";

		gm=new GlobalMetadata();

		// When test read write fragments in XML string format
		gm.fromSedaXmlFragments(gmFragments);
		String gmOut = gm.toSedaXmlFragments();

		// Then
		assertThat(gmOut).isEqualToNormalizingNewlines(gmFragments);
	}
}

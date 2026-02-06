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
        String gmFragments =
            "<Comment>Avec valeurs utilisables sur environnement de démo Vitam V2</Comment>\n" +
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

        gm = new GlobalMetadata();

        // When test read write fragments in XML string format
        gm.fromSedaXmlFragments(gmFragments);
        String gmOut = gm.toSedaXmlFragments();

        // Then
        assertThat(gmOut).isEqualToNormalizingNewlines(gmFragments);
    }
}

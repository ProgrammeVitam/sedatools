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
package fr.gouv.vitam.tools.sedalib.xml;

import fr.gouv.vitam.tools.sedalib.core.GlobalMetadata;
import fr.gouv.vitam.tools.sedalib.inout.importer.DiskToArchiveTransferImporter;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedaschema.SedaVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static fr.gouv.vitam.tools.sedalib.TestUtilities.readFileToString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SEDAValidationTest {

    @Test
    public void testSeda2_1ArchiveTransferCompliance() throws SEDALibException {
        // Given
        String sedaFilePath = "src/test/resources/xml/seda2_1ArchiveTransfer.xml"; // Modify with actual path
        SEDAXMLValidator validator = new SEDAXMLValidator();
        String sedaContent = readFileToString(sedaFilePath);

        // When
        boolean isValid = validator.checkWithXSDSchema(sedaContent, SEDAXMLValidator.getSEDASchema(SedaVersion.V2_1));

        // Then
        assertThat(isValid).as("Validation process on SEDA 2.1 failed").isTrue();
    }

    private static String manifestIn(SedaVersion sedaVersion) throws Exception {
        DiskToArchiveTransferImporter di = new DiskToArchiveTransferImporter(
            sedaVersion,
            "src/test/resources/PacketSamples/SampleWithoutLinksModelV1",
            null
        );
        di.doImport();
        GlobalMetadata globalMetadata = new GlobalMetadata();
        globalMetadata.messageIdentifier = "MessageIdentifier0";
        globalMetadata.archivalAgreement = "ArchivalAgreement0";
        globalMetadata.codeListVersionsXmlData =
            "<CodeListVersions><ReplyCodeListVersion>ReplyCodeListVersion0</ReplyCodeListVersion></CodeListVersions>";
        globalMetadata.archivalAgencyIdentifier = "Identifier4";
        globalMetadata.transferringAgencyIdentifier = "Identifier5";
        di.getArchiveTransfer().setGlobalMetadata(globalMetadata);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (SEDAXMLStreamWriter xmlWriter = new SEDAXMLStreamWriter(baos, 2)) {
            di.getArchiveTransfer().toSedaXml(xmlWriter, true, null);
        }
        return baos.toString(StandardCharsets.UTF_8);
    }

    @ParameterizedTest
    @EnumSource(SedaVersion.class)
    public void testVitamAcceptsSigningInformationAndPersistentIdentifier(SedaVersion sedaVersion) throws Exception {
        // Vitam back ports them from 2.3 to 2.1 and 2.2
        String manifest = manifestIn(sedaVersion).replaceFirst(
            "</Content>",
            "<SigningInformation><SigningRole>SignedDocument</SigningRole>" +
            "<Extended><ExtraField1>Val1</ExtraField1></Extended></SigningInformation>" +
            "<PersistentIdentifier><PersistentIdentifierType>ark</PersistentIdentifierType>" +
            "<PersistentIdentifierContent>ark:/12345/x</PersistentIdentifierContent></PersistentIdentifier>" +
            "</Content>"
        );

        assertThat(
            new SEDAXMLValidator().checkWithXSDSchema(manifest, SEDAXMLValidator.getSEDASchema(sedaVersion))
        ).isTrue();
    }

    @Test
    public void testSeda2_3ExtendedOfSigningInformationCompliance() throws Exception {
        // free metadata in the SEDA namespace, as ReSIP writes them
        String manifest = manifestIn(SedaVersion.V2_3).replaceFirst(
            "</Content>",
            "<SigningInformation><SigningRole>SignedDocument</SigningRole>" +
            "<Extended><ExtraField1>Val1</ExtraField1></Extended></SigningInformation></Content>"
        );
        SEDAXMLValidator validator = new SEDAXMLValidator();

        assertThat(validator.checkWithXSDSchema(manifest, SEDAXMLValidator.getSEDASchema(SedaVersion.V2_3))).isTrue();
        // the official XSD types Extended with the abstract OpenType, closed to the SEDA namespace
        assertThatThrownBy(
            () ->
                validator.checkWithXSDSchema(
                    manifest,
                    SEDAXMLValidator.getSchemaFromXSDResource(
                        SEDAValidationTest.class.getClassLoader().getResource(SedaVersion.V2_3.getMainXsdResource())
                    )
                )
        ).hasMessageContaining("Extended");
    }
}

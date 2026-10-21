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
package fr.gouv.vitam.tools.resip.sedaobjecteditor.model;

import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.core.DataObjectGroup;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;
import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.content.Content;
import fr.gouv.vitam.tools.sedalib.metadata.data.FileInfo;
import fr.gouv.vitam.tools.sedalib.metadata.management.LogBook;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.AgentType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.ComplexListType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.StringType;
import fr.gouv.vitam.tools.sedaschema.SedaVersion;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * How the composite editors put back together and summarise their objects, without the graphic
 * interface.
 */
class EditedObjectsTest {

    private static List<String> fieldNames(List<SEDAMetadata> fields) {
        return fields.stream().map(SEDAMetadata::getXmlElementName).collect(Collectors.toList());
    }

    @Test
    void copiesAMetadataEmptyInItsVersion() throws Exception {
        Content content = new Content(SedaVersion.V2_2);
        content.addNewMetadata("Title", "Title");
        AgentType writer = new AgentType(SedaVersion.V2_3, "Writer", "Fullname");

        SEDAMetadata emptyContent = EditedObjects.emptyCopyOf(content);
        SEDAMetadata emptyWriter = EditedObjects.emptyCopyOf(writer);

        assertThat(emptyContent).isInstanceOf(Content.class);
        assertThat(((ComplexListType) emptyContent).getSedaVersion()).isEqualTo(SedaVersion.V2_2);
        assertThat(((ComplexListType) emptyContent).getMetadataList()).isEmpty();
        assertThat(emptyWriter.getXmlElementName()).isEqualTo("Writer");
        assertThat(((ComplexListType) emptyWriter).getSedaVersion()).isEqualTo(SedaVersion.V2_3);
        assertThat(EditedObjects.emptyCopyOf(new StringType("Title", "Text")).getXmlElementName()).isEqualTo("Title");
    }

    @Test
    void rebuildsAComplexMetadataInSchemaOrder() throws Exception {
        Content edited = new Content(SedaVersion.V2_1);

        ComplexListType rebuilt = EditedObjects.rebuild(
            edited,
            List.of(new StringType("Title", "A title"), new StringType("DescriptionLevel", "Item"))
        );

        assertThat(rebuilt).isNotSameAs(edited).isInstanceOf(Content.class);
        assertThat(fieldNames(rebuilt.getMetadataList())).containsExactly("DescriptionLevel", "Title");
    }

    @Test
    void rebuildsAUnitaryDataObjectInSchemaOrder() throws Exception {
        BinaryDataObject edited = new BinaryDataObject(new DataObjectPackage(SedaVersion.V2_1));

        EditedObjects.rebuild(
            edited,
            List.of(new StringType("Uri", "content/file.txt"), new StringType("DataObjectVersion", "BinaryMaster_1"))
        );

        assertThat(fieldNames(edited.getMetadataList())).containsExactly("DataObjectVersion", "Uri");
    }

    @Test
    void rebuildsAnArchiveUnitWithItsPresentParts() throws Exception {
        DataObjectPackage dataObjectPackage = new DataObjectPackage(SedaVersion.V2_1);
        ArchiveUnit archiveUnit = new ArchiveUnit(dataObjectPackage);
        archiveUnit.setDefaultContent("Old", "Item");
        Content content = new Content(SedaVersion.V2_1);
        content.addNewMetadata("Title", "New");

        EditedObjects.rebuild(archiveUnit, List.of(content));

        assertThat(archiveUnit.getContent().getSimpleMetadata("Title")).isEqualTo("New");
        assertThat(archiveUnit.getManagement()).isNull();
        assertThat(archiveUnit.getArchiveUnitProfile()).isNull();
    }

    @Test
    void rebuildsADataObjectGroupWithItsEditedDataObjects() throws Exception {
        DataObjectPackage dataObjectPackage = new DataObjectPackage(SedaVersion.V2_1);
        DataObjectGroup dog = new DataObjectGroup(dataObjectPackage, null);
        BinaryDataObject removed = new BinaryDataObject(dataObjectPackage);
        dog.addDataObject(removed);
        BinaryDataObject added = MetadataSamples.binaryDataObjectSample(dataObjectPackage, true);
        LogBook logBook = new LogBook(SedaVersion.V2_1);

        EditedObjects.rebuild(dog, List.of(added, logBook));

        assertThat(dog.getBinaryDataObjectList()).containsExactly(added);
        assertThat(dataObjectPackage.getBdoInDataObjectPackageIdMap()).containsValue(added);
        assertThat(dog.getLogBook()).isSameAs(logBook);
    }

    @Test
    void summarisesAComplexMetadataByItsValues() throws Exception {
        Content content = new Content(SedaVersion.V2_1);
        content.addNewMetadata("DescriptionLevel", "Item");
        content.addNewMetadata("Title", "A title");
        content.addNewMetadata("Keyword", "Word", null, null);

        assertThat(EditedObjects.summary(content)).isEqualTo("Item, A title, {Word}");

        content.addNewMetadata("Description", "x".repeat(200));
        assertThat(EditedObjects.summary(content)).endsWith("...");
    }

    @Test
    void summarisesABinaryDataObjectWithItsFileAndFormat() throws Exception {
        BinaryDataObject bdo = new BinaryDataObject(new DataObjectPackage(SedaVersion.V2_1));
        bdo.addNewMetadata("DataObjectVersion", "BinaryMaster_1");
        FileInfo fileInfo = new FileInfo(SedaVersion.V2_1);
        fileInfo.addNewMetadata("Filename", "file.txt");
        bdo.addMetadata(fileInfo);

        assertThat(EditedObjects.summary(bdo, "?")).isEqualTo("BinaryMaster_1, file.txt, ?");
    }
}

package fr.gouv.vitam.tools.sedalib.inout;

import fr.gouv.vitam.tools.sedalib.TestUtilities;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.metadata.management.AppraisalRule;
import fr.gouv.vitam.tools.sedalib.metadata.content.Content;
import fr.gouv.vitam.tools.sedalib.metadata.content.Event;
import fr.gouv.vitam.tools.sedalib.metadata.management.Management;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.AgentType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Scanner;
import java.util.StringTokenizer;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class SIPBuilderTest {

    static private String stripFileName(String fileName) {
        String tmp = fileName.substring(fileName.indexOf("-") + 1);
        if (tmp.lastIndexOf('.') >= 0)
            return (tmp.substring(0, tmp.lastIndexOf('.')));
        else
            return tmp;
    }

    @Test
    void generateSimpleSIP() throws Exception {

        TestUtilities.createOrEraseAll("target/tmpJunit/SIPBuilder");

        SEDALibProgressLogger pl = new SEDALibProgressLogger(LoggerFactory.getLogger("SIPBuilderTest"), SEDALibProgressLogger.OBJECTS_GROUP);
        try (SIPBuilder sb = new SIPBuilder("target/tmpJunit/SIPBuilder/SIPBuilderTest.zip", pl)) {
            sb.setAgencies("FRAN_NP_000001", "FRAN_NP_000010", "FRAN_NP_000015", "FRAN_NP_000019");
            sb.setArchivalAgreement("IC-000001");
            sb.createRootArchiveUnit("Racine", "Subseries", "Procédure Cerfa-1244771",
                    "Procédure Cerfa-1244771 - DEMANDE D'AUTORISATION DE DETENTION DE GRENOUILLES CYBORG "
                            + "(Arrêté du 30 février 2104 fixant les règles générales de fonctionnement des installations d’élevage d’agrément d’animaux cyborg)");
            sb.addNewSubArchiveUnit("Racine", "Contexte", "RecordGrp", "Contexte",
                    "Ensemble des fichiers donnant le contexte de la procédure Cerfa-1244771");
            sb.addDiskSubTree("Contexte", "src/test/resources/Procedure/Contexte");
            if (System.getProperty("os.name").toLowerCase().startsWith("win"))
                sb.addCSVMetadataSubTree("Racine", "Cp1252", ';', "src/test/resources/MetadataCSV.csv");
            else
                sb.addCSVMetadataSubTree("Racine", "Cp1252", ';', "src/test/resources/UnixMetadataCSV.csv");

            // keep one file for history in Contexte part and without AppraisalRuleOld
            ArchiveUnit sampleAU = sb.findArchiveUnitBySimpleDescriptiveMetadata("OriginatingSystemId", "ID10000");
            Content c = sb.getContent(sampleAU.getInDataObjectPackageId());
            sb.addNewSubArchiveUnit("Contexte", "Exemple de dossier",
                    "RecordGrp", "Exemplaire de dossier",
                    "Ensemble des fichiers d'un dossier pour en conserver la forme.\nTitre original:" +
                            c.getSimpleMetadata("Title") + "\nDescription originale:" +
                            c.getSimpleMetadata("Description"));
            sb.addArchiveUnitSubTree("Exemple de dossier", sampleAU.getInDataObjectPackageId());

            sb.generateSIP();

//TODO Improve test quality
            assertThat(new File("target/tmpJunit/SIPBuilder/SIPBuilderTest.zip")).isFile();
            assertThat(new File("target/tmpJunit/SIPBuilder/SIPBuilderTest.zip").length()).isGreaterThan(10868000).isLessThan(10871000);
            assertAll(()->sb.seda21Validate());

        } catch (Exception e) {
            throw new SEDALibException("SIPBuilder test KO");
        }
    }

    @Test
    void generateComplexSIP() throws Exception {

        TestUtilities.createOrEraseAll("target/tmpJunit/SIPBuilder");

        SEDALibProgressLogger pl = new SEDALibProgressLogger(LoggerFactory.getLogger("SIPBuilderTest"), SEDALibProgressLogger.OBJECTS_GROUP);
        try (SIPBuilder sb = new SIPBuilder("target/tmpJunit/SIPBuilder/ComplexSIPBuilderTest.zip", pl)) {
            sb.setAgencies("FRAN_NP_000001", "FRAN_NP_000010", "FRAN_NP_000015", "FRAN_NP_000019");
            sb.setArchivalAgreement("IC-000001");
            sb.createSystemExistingRootArchiveUnit("Dossiers", "FilePlanPosition", "Dossiers-Cerfa-1244771",
                    "RecordGrp", "Dossiers");

            // iterate through csv
            Path procDir = Paths.get("src/test/resources/AutresDossiers");
            Scanner scanner = new Scanner(new File("src/test/resources/AutresDossiers.csv"));
            String procId = null;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                StringTokenizer st = new StringTokenizer(line, ";");
                String id = st.nextToken();
                LocalDateTime registeredDate = SEDAXMLEventReader.getDateTimeFromString(st.nextToken());
                String requirerId = st.nextToken();
                String birthname = st.nextToken();
                String firstname = st.nextToken();
                LocalDateTime resultDate = SEDAXMLEventReader.getDateTimeFromString(st.nextToken());
                String result = st.nextToken();
                procId = "Cerfa-1244771-" + id;

                // set metadata from csv on the procId ArchiveUnit
                sb.addNewSubArchiveUnit("Dossiers", procId, "RecordGrp", procId,
                        "Ensemble des fichiers de dossier " + procId);
                Content content = sb.getContent(procId);
                content.addNewMetadata("OriginatingSystemId", procId);
                content.addNewMetadata("RegisteredDate", registeredDate);
                Event event = new Event();
                event.addNewMetadata("EventTypeCode", "Avis administratif");
                event.addNewMetadata("EventDateTime", resultDate);
                event.addNewMetadata("Outcome", result);
                content.addMetadata(event);
                AgentType requirer = new AgentType("Requirer");
                requirer.addNewMetadata("Identifier", requirerId);
                requirer.addNewMetadata("FirstName", firstname);
                requirer.addNewMetadata("BirthName", birthname);
                content.addMetadata(requirer);
                sb.setContent(procId, content);
                Management management = sb.getManagement(procId);
                AppraisalRule appraisalRule = new AppraisalRule();
                appraisalRule.addRule("APP-1069001", resultDate.toLocalDate());
                appraisalRule.setFinalAction("Destroy");
                management.addMetadata(appraisalRule);
                sb.setManagement(procId, management);

                // put all proc files in the procId ArchiveUnit
                try (DirectoryStream<Path> ds = Files.newDirectoryStream(procDir, new DirectoryStream.Filter<Path>() {
                    @Override
                    public boolean accept(Path entry) throws IOException {
                        return entry.getFileName().toString().startsWith(id);
                    }
                })) {
                    for (Path p : ds) {
                        if (!p.getFileName().toString().endsWith(".xml"))
                            sb.addFileSubArchiveUnit(procId, p.toString(), p.getFileName().toString(), "File",
                                    stripFileName(p.getFileName().toString()), null);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            scanner.close();

            sb.generateSIP();

//TODO Improve test quality
            assertThat(new File("target/tmpJunit/SIPBuilder/ComplexSIPBuilderTest.zip")).isFile();
            assertThat(new File("target/tmpJunit/SIPBuilder/ComplexSIPBuilderTest.zip").length()).isGreaterThan(2183000).isLessThan(2185000);
        } catch (Exception e) {
            throw new SEDALibException("SIPBuilder test KO");
        }
    }
}
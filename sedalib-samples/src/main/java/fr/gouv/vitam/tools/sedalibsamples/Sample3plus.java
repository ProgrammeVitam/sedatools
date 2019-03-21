package fr.gouv.vitam.tools.sedalibsamples;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Scanner;
import java.util.StringTokenizer;

import fr.gouv.vitam.tools.sedalib.inout.SIPBuilder;
import fr.gouv.vitam.tools.sedalib.metadata.management.AppraisalRule;
import fr.gouv.vitam.tools.sedalib.metadata.content.Content;
import fr.gouv.vitam.tools.sedalib.metadata.content.Event;
import fr.gouv.vitam.tools.sedalib.metadata.management.Management;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.AgentType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;
import org.slf4j.LoggerFactory;

public class Sample3plus {

    static private String stripFileName(String fileName) {
        String tmp = fileName.substring(fileName.indexOf("-") + 1);
        if (tmp.lastIndexOf('.') >= 0)
            return (tmp.substring(0, tmp.lastIndexOf('.')));
        else
            return tmp;
    }

    static void run() throws Exception {
        SEDALibProgressLogger pl = new SEDALibProgressLogger(LoggerFactory.getLogger("sedalibsamples"), SEDALibProgressLogger.OBJECTS_GROUP);
        try (SIPBuilder sb = new SIPBuilder("sedalib-samples/samples/Sample3plus.zip", pl)) {
            sb.setAgencies("FRAN_NP_000001", "FRAN_NP_000010", "FRAN_NP_000015", "FRAN_NP_000019");
            sb.setArchivalAgreement("IC-000001");
            sb.createSystemExistingRootArchiveUnit("Dossiers", "FilePlanPosition", "Dossiers-Cerfa-1244771",
                    "RecordGrp", "Dossiers");

            // iterate through csv
            Path procDir = Paths.get("sedalib-samples/src/main/resources/AutresDossiers");
            Scanner scanner = new Scanner(new File("sedalib-samples/src/main/resources/AutresDossiers.csv"));
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
        }
    }
}

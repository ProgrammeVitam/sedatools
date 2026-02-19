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
package fr.gouv.vitam.tools.sedalibsamples;

import fr.gouv.vitam.tools.sedalib.inout.SIPBuilder;
import fr.gouv.vitam.tools.sedalib.metadata.content.Content;
import fr.gouv.vitam.tools.sedalib.metadata.content.Event;
import fr.gouv.vitam.tools.sedalib.metadata.management.AppraisalRule;
import fr.gouv.vitam.tools.sedalib.metadata.management.Management;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.AgentType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;
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

public class Sample3 {

    private static String stripFileName(String fileName) {
        String tmp = fileName.substring(fileName.indexOf("-") + 1);
        if (tmp.lastIndexOf('.') >= 0) return (tmp.substring(0, tmp.lastIndexOf('.')));
        else return tmp;
    }

    static void run() throws Exception {
        SEDALibProgressLogger pl = new SEDALibProgressLogger(
            LoggerFactory.getLogger("sedalibsamples"),
            SEDALibProgressLogger.OBJECTS_GROUP
        );
        try (SIPBuilder sb = new SIPBuilder("sedalib-samples/samples/Sample3.zip", pl)) {
            sb.setAgencies("FRAN_NP_000001", "FRAN_NP_000010", "FRAN_NP_000015", "FRAN_NP_000019");
            sb.setArchivalAgreement("IC-000001");
            sb.createRootArchiveUnit(
                "Racine",
                "Subseries",
                "Procédure Cerfa-1244771",
                "Procédure Cerfa-1244771 - DEMANDE D'AUTORISATION DE DETENTION DE GRENOUILLES CYBORG " +
                "(Arrêté du 30 février 2104 fixant les règles générales de fonctionnement des installations d’élevage d’agrément d’animaux cyborg)"
            );

            sb.addNewSubArchiveUnit(
                "Racine",
                "Contexte",
                "RecordGrp",
                "Contexte",
                "Ensemble des fichiers donnant le contexte de la procédure Cerfa-1244771"
            );
            sb.addDiskSubTree("Contexte", "sedalib-samples/src/main/resources/Procedure/Contexte");
            sb.addNewSubArchiveUnit(
                "Racine",
                "Dossiers",
                "RecordGrp",
                "Dossiers",
                "Ensemble des dossiers archivés de la procédure Cerfa-1244771"
            );
            sb.addNewContentMetadataInArchiveUnit("Dossiers", "FilePlanPosition", "Dossiers-Cerfa-1244771");

            // iterate through csv
            Path procDir = Paths.get("sedalib-samples/src/main/resources/Procedure/Dossiers");
            Scanner scanner = new Scanner(new File("sedalib-samples/src/main/resources/Procedure.csv"));
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
                sb.addNewSubArchiveUnit(
                    "Dossiers",
                    procId,
                    "RecordGrp",
                    procId,
                    "Ensemble des fichiers de dossier " + procId
                );
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
                try (
                    DirectoryStream<Path> ds = Files.newDirectoryStream(
                        procDir,
                        new DirectoryStream.Filter<Path>() {
                            @Override
                            public boolean accept(Path entry) throws IOException {
                                return entry.getFileName().toString().startsWith(id);
                            }
                        }
                    )
                ) {
                    for (Path p : ds) {
                        if (!p.getFileName().toString().endsWith(".xml")) sb.addFileSubArchiveUnit(
                            procId,
                            p.toString(),
                            p.getFileName().toString(),
                            "File",
                            stripFileName(p.getFileName().toString()),
                            null
                        );
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            scanner.close();

            // keep last file for history in Contexte part and without AppraisalRuleOld
            if (procId != null) {
                Content c = sb.getContent(procId);
                sb.addNewSubArchiveUnit(
                    "Contexte",
                    "Exemple de dossier",
                    "RecordGrp",
                    "Exemplaire de dossier",
                    "Ensemble des fichiers d'un dossier pour en conserver la forme.\nTitre original:" +
                    c.getSimpleMetadata("Title") +
                    "\nDescription originale:" +
                    c.getSimpleMetadata("Description")
                );
                sb.addArchiveUnitSubTree("Exemple de dossier", procId);
            }

            sb.generateSIP();
        }
    }
}

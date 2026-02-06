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
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class Sample2 {

    static private String stripFileName(String fileName) {
        String tmp = fileName.substring(fileName.indexOf("-") + 1);
        if (tmp.lastIndexOf('.') >= 0)
            return (tmp.substring(0, tmp.lastIndexOf('.')));
        else
            return tmp;
    }

    static void run() throws Exception {
        SEDALibProgressLogger pl = new SEDALibProgressLogger(LoggerFactory.getLogger("sedalibsamples"), SEDALibProgressLogger.OBJECTS_GROUP);
        try (SIPBuilder sb = new SIPBuilder("sedalib-samples/samples/Sample2.zip", pl)) {
            sb.setAgencies("FRAN_NP_000001", "FRAN_NP_000010", "FRAN_NP_000015", "FRAN_NP_000019");
            sb.setArchivalAgreement("IC-000001");
            sb.createRootArchiveUnit("Racine", "Subseries", "Procédure Cerfa-1244771",
                    "Procédure Cerfa-1244771 - DEMANDE D'AUTORISATION DE DETENTION DE GRENOUILLES CYBORG "
                            + "(Arrêté du 30 février 2104 fixant les règles générales de fonctionnement des installations d’élevage d’agrément d’animaux cyborg)");

            sb.addNewSubArchiveUnit("Racine", "Contexte", "RecordGrp", "Contexte",
                    "Ensemble des fichiers donnant le contexte de la procédure Cerfa-1244771");
            sb.addDiskSubTree("Contexte", "sedalib-samples/src/main/resources/Procedure/Contexte");
            sb.addNewSubArchiveUnit("Racine", "Dossiers", "RecordGrp", "Dossiers",
                    "Ensemble des dossiers archivés de la procédure Cerfa-1244771");

            // find proc IDs
            Path procDir = Paths.get("sedalib-samples/src/main/resources/Procedure/Dossiers");
            Iterator<Path> pi;
            List<String> procIdList = new ArrayList<String>();
            try (Stream<Path> sp = Files.list(procDir)) {
                pi = sp.iterator();
                while (pi.hasNext()) {
                    Path path = pi.next();
                    String filename = path.getFileName().toString();
                    if (!filename.matches("ID[0-9]+-.+"))
                        throw new SEDALibException("Fichier ne correspondant pas à un dossier");
                    String id = filename.substring(0, filename.indexOf("-"));
                    if (!procIdList.contains(id))
                        procIdList.add(id);
                }
            }

            // construct proc archives with metadata in group ArchiveUnit
            for (String procId : procIdList) {
                sb.addNewSubArchiveUnit("Dossiers", procId, "RecordGrp", procId,
                        "Ensemble des fichiers de dossier " + procId);
                try (DirectoryStream<Path> ds = Files.newDirectoryStream(procDir, new DirectoryStream.Filter<Path>() {
                    @Override
                    public boolean accept(Path entry) throws IOException {
                        return entry.getFileName().toString().startsWith(procId);
                    }
                })) {
                    for (Path p : ds) {
                        if (p.getFileName().toString().endsWith(".xml")) {
                            Content content = sb.getContent(procId);
                            content.addSedaXmlFragments(SEDAXMLEventReader.extractFragments("File", new String(Files.readAllBytes(p), "UTF-8")));
                            sb.setContent(procId, content);
                        } else
                            sb.addFileSubArchiveUnit(procId, p.toString(), p.getFileName().toString(), "File",
                                    stripFileName(p.getFileName().toString()), null);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

            }

            sb.generateSIP();
        }
    }
}
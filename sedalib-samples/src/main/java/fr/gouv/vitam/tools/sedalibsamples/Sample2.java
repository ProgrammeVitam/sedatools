package fr.gouv.vitam.tools.sedalibsamples;

import fr.gouv.vitam.tools.sedalib.inout.SIPBuilder;
import fr.gouv.vitam.tools.sedalib.metadata.Content;
import fr.gouv.vitam.tools.sedalib.utils.ProgressLogger;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
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
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
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
        ProgressLogger pl = new ProgressLogger(LoggerFactory.getLogger("sedalibsamples"), ProgressLogger.OBJECTS_GROUP);
        try (SIPBuilder sb = new SIPBuilder("samples/Sample2.zip", pl)) {
            sb.setAgencies("FRAN_NP_000001", "FRAN_NP_000010", "FRAN_NP_000015", "FRAN_NP_000019");
            sb.setArchivalAgreement("IC-000001");
            sb.createRootArchiveUnit("Racine", "Subseries", "Procédure Cerfa-1244771",
                    "Procédure Cerfa-1244771 - DEMANDE D'AUTORISATION DE DETENTION DE GRENOUILLES CYBORG "
                            + "(Arrêté du 30 février 2104 fixant les règles générales de fonctionnement des installations d’élevage d’agrément d’animaux cyborg)");

            sb.addNewSubArchiveUnit("Racine", "Contexte", "RecordGrp", "Contexte",
                    "Ensemble des fichiers donnant le contexte de la procédure Cerfa-1244771");
            sb.addDiskSubTree("Contexte", "src/main/resources/Procédure/Contexte");
            sb.addNewSubArchiveUnit("Racine", "Dossiers", "RecordGrp", "Dossiers",
                    "Ensemble des dossiers archivés de la procédure Cerfa-1244771");

            // find proc IDs
            Path procDir = Paths.get("src/main/resources/Procédure/Dossiers");
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
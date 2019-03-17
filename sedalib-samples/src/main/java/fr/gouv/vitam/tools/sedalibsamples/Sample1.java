package fr.gouv.vitam.tools.sedalibsamples;

import fr.gouv.vitam.tools.sedalib.inout.SIPBuilder;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;
import org.slf4j.LoggerFactory;

public class Sample1 {

    static void run() throws Exception {
        SEDALibProgressLogger pl = new SEDALibProgressLogger(LoggerFactory.getLogger("sedalibsamples"), SEDALibProgressLogger.OBJECTS_GROUP);
        try (SIPBuilder sb = new SIPBuilder("sedalib-samples/samples/Sample1.zip", pl)) {
            sb.setAgencies("FRAN_NP_000001", "FRAN_NP_000010", "FRAN_NP_000015", "FRAN_NP_000019");
            sb.setArchivalAgreement("IC-000001");
            sb.createRootArchiveUnit("Racine", "Subseries", "Procédure Cerfa-1244771",
                    "Procédure Cerfa-1244771 - DEMANDE D'AUTORISATION DE DETENTION DE GRENOUILLES CYBORG "
                            + "(Arrêté du 30 février 2104 fixant les règles générales de fonctionnement des installations d’élevage d’agrément d’animaux cyborg)");
            sb.addDiskSubTree("Racine", "sedalib-samples/src/main/resources/Procédure");
            sb.generateSIP();
        }
    }
}

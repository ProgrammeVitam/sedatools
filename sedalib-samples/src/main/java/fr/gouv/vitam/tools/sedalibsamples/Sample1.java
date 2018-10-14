package fr.gouv.vitam.tools.sedalibsamples;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.gouv.vitam.tools.sedalib.inout.SIPBuilder;
import fr.gouv.vitam.tools.sedalib.utils.ProgressLogger;

public class Sample1 {

    static public Logger createLogger(Level logLevel) {
        Logger logger;

        Properties props = System.getProperties();
        props.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s%n");
        logger = Logger.getLogger("Sample1");
        logger.setLevel(logLevel);

        return logger;
    }

    static void run() throws Exception {
        ProgressLogger pl = new ProgressLogger(createLogger(Level.ALL), ProgressLogger.STEP);
        try (SIPBuilder sb = new SIPBuilder("samples/Sample1.zip", pl)) {
            sb.setAgencies("FRAN_NP_000001", "FRAN_NP_000010", "FRAN_NP_000015", "FRAN_NP_000019");
            sb.setArchivalAgreement("IC-000001");
            sb.createRootArchiveUnit("Racine", "Subseries", "Procédure Cerfa-1244771",
                    "Procédure Cerfa-1244771 - DEMANDE D'AUTORISATION DE DETENTION DE GRENOUILLES CYBORG "
                            + "(Arrêté du 30 février 2104 fixant les règles générales de fonctionnement des installations d’élevage d’agrément d’animaux cyborg)");
            sb.addDiskSubTree("Racine", "src/main/resources/Procédure");
            sb.generateSIP();
        }
    }
}

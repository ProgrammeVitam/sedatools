package fr.gouv.vitam.tools.sedalibsamples;

import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.inout.SIPBuilder;
import fr.gouv.vitam.tools.sedalib.metadata.Content;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;
import org.slf4j.LoggerFactory;

public class Sample4 {

    static void run() throws Exception {
        SEDALibProgressLogger pl = new SEDALibProgressLogger(LoggerFactory.getLogger("sedalibsamples"), SEDALibProgressLogger.OBJECTS_GROUP);
        try (SIPBuilder sb = new SIPBuilder("sedalib-samples/samples/Sample4.zip", pl)) {
            sb.setAgencies("FRAN_NP_000001", "FRAN_NP_000010", "FRAN_NP_000015", "FRAN_NP_000019");
            sb.setArchivalAgreement("IC-000001");
            sb.createRootArchiveUnit("Racine", "Subseries", "Procédure Cerfa-1244771",
                    "Procédure Cerfa-1244771 - DEMANDE D'AUTORISATION DE DETENTION DE GRENOUILLES CYBORG "
                            + "(Arrêté du 30 février 2104 fixant les règles générales de fonctionnement des installations d’élevage d’agrément d’animaux cyborg)");

            sb.addNewSubArchiveUnit("Racine", "Contexte", "RecordGrp", "Contexte",
                    "Ensemble des fichiers donnant le contexte de la procédure Cerfa-1244771");
            sb.addDiskSubTree("Contexte", "sedalib-samples/src/main/resources/Procedure/Contexte");
            sb.addCSVMetadataSubTree("Racine", "Cp1252", ';', "sedalib-samples/src/main/resources/MetadataCSV.csv");

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
        }
    }
}

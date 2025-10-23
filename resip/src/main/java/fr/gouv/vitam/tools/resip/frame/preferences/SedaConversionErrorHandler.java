package fr.gouv.vitam.tools.resip.frame.preferences;

import fr.gouv.vitam.tools.resip.frame.UserInteractionDialog;
import fr.gouv.vitam.tools.resip.utils.ResipLogger;

import javax.swing.JFrame;
import java.awt.Window;

public class SedaConversionErrorHandler {

    private final JFrame owner;

    public SedaConversionErrorHandler(JFrame owner) {
        this.owner = owner;
    }

    public void handleFatalError(String versionLabel, Exception exception, Throwable threadError) {
        UserInteractionDialog.getUserAnswer(
            owner,
            "Impossible de faire la conversion en " + versionLabel + "\n->" + exception.getMessage(),
            "Erreur",
            UserInteractionDialog.ERROR_DIALOG,
            null
        );
        ResipLogger.getGlobalLogger().log(
            ResipLogger.ERROR,
            "resip.graphicapp: erreur fatale, impossible de faire la conversion en " + versionLabel,
            threadError
        );
    }

    public void handleNullResult(String versionLabel, Throwable error) {
        UserInteractionDialog.getUserAnswer(
            owner,
            "Impossible de faire la conversion en " + versionLabel +
                "\nFermez cet objet avant de changer la version du SEDA utilisé\n->" + error,
            "Erreur",
            UserInteractionDialog.ERROR_DIALOG,
            null
        );
        ResipLogger.getGlobalLogger().log(
            ResipLogger.ERROR,
            "resip.graphicapp: erreur, impossible de faire la conversion en " + versionLabel,
            error
        );
    }
}


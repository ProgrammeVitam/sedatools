package fr.gouv.vitam.tools.resip.threads;

import fr.gouv.vitam.tools.resip.utils.ResipLogger;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;

import javax.swing.JTextArea;

public class ThreadLoggerFactory {

    private final JTextArea loggingComponent;
    private final boolean isDebugMode;

    public ThreadLoggerFactory(JTextArea loggingComponent, boolean isDebugMode) {
        this.loggingComponent = loggingComponent;
        this.isDebugMode = isDebugMode;
    }

    public SEDALibProgressLogger getLogger() {
        int localLogLevel = this.getLogLevel();
        int localLogStep = this.localLogStep();

        return new SEDALibProgressLogger(
            ResipLogger.getGlobalLogger().getLogger(),
            localLogLevel,
            (count, log) -> {
                String newLog = loggingComponent.getText() + "\n" + log;
                loggingComponent.setText(newLog);
                loggingComponent.setCaretPosition(newLog.length());
            },
            localLogStep,
            2,
            SEDALibProgressLogger.OBJECTS_GROUP,
            1000
        );
    }

    public int getLogLevel() {
        if (isDebugMode) {
            return SEDALibProgressLogger.OBJECTS_WARNINGS;
        }

        return SEDALibProgressLogger.OBJECTS_GROUP;
    }

    public int localLogStep() {
        if (isDebugMode) {
            return 1;
        }

        return 1000;
    }
}

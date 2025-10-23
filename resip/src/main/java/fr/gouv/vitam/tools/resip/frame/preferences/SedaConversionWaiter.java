package fr.gouv.vitam.tools.resip.frame.preferences;

import javax.swing.SwingWorker;

public class SedaConversionWaiter {

    public void waitUntilFinished(SwingWorker<?, ?> thread) throws InterruptedException {
        while (!thread.isDone()) {
            Thread.sleep(100);
        }
    }
}

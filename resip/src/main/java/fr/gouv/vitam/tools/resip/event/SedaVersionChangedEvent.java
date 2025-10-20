package fr.gouv.vitam.tools.resip.event;

import fr.gouv.vitam.tools.sedalib.core.seda.SedaVersion;

public class SedaVersionChangedEvent implements Event {
    private final SedaVersion newVersion;

    public SedaVersionChangedEvent(SedaVersion newVersion) {
        this.newVersion = newVersion;
    }

    public SedaVersion getNewVersion() {
        return newVersion;
    }
}

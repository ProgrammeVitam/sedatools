package fr.gouv.vitam.tools.sedalib.core.seda;

public class SedaContext {
    private static SedaVersion _version;

    public static SedaVersion getVersion() {
        return _version;
    }

    public static void setVersion(SedaVersion version) {
        _version = version;
    }
}

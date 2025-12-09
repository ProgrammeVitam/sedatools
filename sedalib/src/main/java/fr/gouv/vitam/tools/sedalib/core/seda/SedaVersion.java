package fr.gouv.vitam.tools.sedalib.core.seda;

public enum SedaVersion {
    V2_0(2, 0),
    V2_1(2, 1),
    V2_2(2, 2),
    V2_3(2, 3);

    private final int major;
    private final int minor;

    SedaVersion(int major, int minor) {
        this.major = major;
        this.minor = minor;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    @Override
    public String toString() {
        return major + "." + minor;
    }

    public String displayString() {
        return "SEDA " + major + "." + minor;
    }

    public static SedaVersion from(int major, int minor) {
        for (SedaVersion version : values()) {
            if (version.major == major && version.minor == minor) {
                return version;
            }
        }
        throw new IllegalArgumentException("Unsupported SEDA version: " + major + "." + minor);
    }

    public static SedaVersion from(String version) {
        String[] fragments = version.split("[.]");

        if (fragments.length != 2) {
            throw new IllegalArgumentException("Unsupported SEDA version: " + version);
        }

        int major = Integer.parseInt(fragments[0]);
        int minor = Integer.parseInt(fragments[1]);

        return from(major, minor);
    }
}

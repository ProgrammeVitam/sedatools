package fr.gouv.vitam.tools.resip.frame.preferences;

public enum SedaVersion {
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
}

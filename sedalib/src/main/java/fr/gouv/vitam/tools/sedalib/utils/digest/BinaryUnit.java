package fr.gouv.vitam.tools.sedalib.utils.digest;

public enum BinaryUnit {

    KIBI(1024L),
    MEBI(1024L * 1024),
    GIBI(1024L * 1024 * 1024),
    TEBI(1024L * 1024 * 1024 * 1024);

    private final long bytes;

    BinaryUnit(long bytes) {
        this.bytes = bytes;
    }

    public long toBytes() {
        return bytes;
    }

    public long toBytes(long value) {
        return Math.multiplyExact(value, bytes);
    }
}

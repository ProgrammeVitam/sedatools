package fr.gouv.vitam.tools.sedalib.metadata.namedtype;

public class ComplexListMetadataKind {
    /**
     * The c.
     */
    public Class<?> metadataClass;

    /**
     * The many.
     */
    public boolean many;

    /**
     * Instantiates a new metadata item.
     *
     * @param metadataClass the metadata class
     * @param many          the many
     */
    public ComplexListMetadataKind(Class<?> metadataClass, boolean many) {
        this.metadataClass = metadataClass;
        this.many = many;
    }
}

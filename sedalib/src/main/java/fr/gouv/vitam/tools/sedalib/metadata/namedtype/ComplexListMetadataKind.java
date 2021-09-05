package fr.gouv.vitam.tools.sedalib.metadata.namedtype;

/**
 * The type Complex list metadata kind.
 */
public class ComplexListMetadataKind {
    /**
     * The metadata class.
     */
    private Class<?> metadataClass;

    /**
     * The possibility to have many of them.
     */
    private boolean many;

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

    public Class<?> getMetadataClass() {
        return metadataClass;
    }

    public void setMetadataClass(Class<?> metadataClass) {
        this.metadataClass = metadataClass;
    }

    public boolean isMany() {
        return many;
    }

    public void setMany(boolean many) {
        this.many = many;
    }
}

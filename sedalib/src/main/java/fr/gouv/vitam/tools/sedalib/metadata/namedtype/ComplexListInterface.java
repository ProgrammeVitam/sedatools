/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to provide
 * tools for construction and manipulation of SIP (Submission
 * Information Package) conform to the SEDA (Standard d’Échange
 * de données pour l’Archivage) standard.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitam.tools.sedalib.metadata.namedtype;

import fr.gouv.vitam.tools.sedalib.core.seda.SedaContext;
import fr.gouv.vitam.tools.sedalib.core.seda.SedaVersion;
import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.content.Gps;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLStreamWriter;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * The Interface ComplexListInterface.
 * <p>
 * Provides default methods for managing SEDA metadata lists consisting of ordered metadata.
 * This interface includes methods for constructing, manipulating, and validating metadata lists
 * based on schema definitions represented in metadata maps. This interface is primarily used for
 * handling complex ArchiveUnit metadata structures in ComplexListType, but also supports polymorphic
 * SEDA versions of DataObjects.
 * <p>
 * Key functionalities include:
 * <ul>
 *   <li>Dynamic metadata element construction and addition</li>
 *   <li>Metadata validation and modification</li>
 *   <li>Schema-based or expandable list handling</li>
 *   <li>Import/export of metadata to/from SEDA XML and CSV formats</li>
 * </ul>
 * <p>
 * Example of a metadata map definition:
 * <pre>
 * {@code
 * @ComplexListMetadataMap(isExpandable = true, sedaVersion = { SedaVersion.V2_3 })
 * public static final Map<String, ComplexListMetadataKind> metadataMap_v3;
 *
 * static {
 *     metadataMap_v3 = new LinkedHashMap<>();
 *     metadataMap_v3.put("DescriptionLevel", new ComplexListMetadataKind(DescriptionLevel.class, false));
 *     // ...
 * }
 * }
 * </pre>
 */
public interface ComplexListInterface {


    /**
     * Gets list of fields with ComplexListMetadataMap annotation.
     * <p>
     * Finds and collects all fields in the given class that are annotated with
     * {@link ComplexListMetadataMap}.
     *
     * @param targetClass the class to analyze
     * @return list of fields having ComplexListMetadataMap annotation
     */
    static private List<Field> getFieldsWithAnnotationComplexListMetadataMap(Class<?> targetClass) {
        List<Field> result = new ArrayList<>();
        for (Field field : targetClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(ComplexListMetadataMap.class)) {
                result.add(field);
            }
        }
        return result;
    }

    /**
     * Get SEDA version from annotation.
     * When no has value provided, all SEDA version are positioned.
     *
     * @param annotation a metadata definition map annotation.
     * @return SEDA version list for this annotation.
     */
    private static List<SedaVersion> getSedaVersions(ComplexListMetadataMap annotation) {
        SedaVersion[] explicit = annotation.sedaVersion();
        return explicit.length == 0
            ? Arrays.asList(SedaVersion.values())
            : Arrays.asList(explicit);
    }

    /**
     * Initializes the metadata map array by populating it with metadata maps
     * defined with the {@link ComplexListMetadataMap} annotation in the specified class.
     * <p>
     * Each metadata map corresponds to a specific SEDA version. The method identifies these maps
     * and their associated versions, then assigns them to the respective indices in the provided array.
     * The method also populates a boolean array indicating whether each version is expandable or not.
     * <p>
     * For example:
     * <pre>
     * {@code
     * // Class definition with annotated metadata map
     * public class MyMetadata {
     *   @ComplexListMetadataMap(isExpandable = true, sedaVersion = { SedaVersion.V2_3 })
     *   public static final Map<String, ComplexListMetadataKind> metadataMap_v3;
     *   ...
     * }
     *
     * @param targetClass              the class containing the metadata maps annotated with {@link ComplexListMetadataMap}
     * @param metadataMaps       the array of metadata maps, where each index corresponds to a SEDA version
     * @param nonExpandableFlags array to be populated with expandability flags for each version
     * @throws RuntimeException if metadata initialization fails due to reflection errors
     */
    static void initMetadataMaps(
            Class<?> targetClass,
            Map<SedaVersion, Map<String, ComplexListMetadataKind>> versionedMetadataDefinitions,
            Map<SedaVersion, Boolean> versionedNotExpandableFlags
    ) {
        try {
            List<Field> annotatedFields = getFieldsWithAnnotationComplexListMetadataMap(targetClass);
            for (Field field : annotatedFields) {
                ComplexListMetadataMap ann = field.getAnnotation(ComplexListMetadataMap.class);

                @SuppressWarnings("unchecked")
                LinkedHashMap<String, ComplexListMetadataKind> map =
                        (LinkedHashMap<String, ComplexListMetadataKind>) field.get(null);

                boolean isExpandable = ann.isExpandable();
                for (SedaVersion version : getSedaVersions(ann)) {
                    versionedMetadataDefinitions.put(version, map);
                    versionedNotExpandableFlags.put(version, !isExpandable);
                }
            }
        } catch (ReflectiveOperationException | ClassCastException e) {
            throw new RuntimeException(
                    "Erreur d'initialisation des metadata pour la classe " + targetClass.getName(), e);
        }
    }

    static Map<SedaVersion, Map<String, ComplexListMetadataKind>> metadataDefinitions(Class<?> clazz) {
        Map<SedaVersion, Map<String, ComplexListMetadataKind>> versionedMetadataDefinitions = new LinkedHashMap<>();

        try {
            List<Field> annotatedFields = getFieldsWithAnnotationComplexListMetadataMap(clazz);
            for (Field field : annotatedFields) {
                ComplexListMetadataMap ann = field.getAnnotation(ComplexListMetadataMap.class);

                @SuppressWarnings("unchecked")
                LinkedHashMap<String, ComplexListMetadataKind> map =
                    (LinkedHashMap<String, ComplexListMetadataKind>) field.get(null);

                for (SedaVersion version : getSedaVersions(ann)) {
                    versionedMetadataDefinitions.put(version, map);
                }
            }
        } catch (ReflectiveOperationException | ClassCastException e) {
            throw new RuntimeException(
                "Erreur d'initialisation des metadata pour la classe " + clazz.getName(), e);
        }

        return versionedMetadataDefinitions;
    }

    static Map<SedaVersion, Boolean> notExpandableFlags(Class<?> clazz) {
        Map<SedaVersion, Boolean> versionedNotExpandableFlags = new LinkedHashMap<>();

        try {
            List<Field> annotatedFields = getFieldsWithAnnotationComplexListMetadataMap(clazz);
            for (Field field : annotatedFields) {
                ComplexListMetadataMap ann = field.getAnnotation(ComplexListMetadataMap.class);

                for (SedaVersion version : getSedaVersions(ann)) {
                    versionedNotExpandableFlags.put(version, !ann.isExpandable());
                }
            }
        } catch (ClassCastException e) {
            throw new RuntimeException(
                "Erreur d'initialisation des metadata pour la classe " + clazz.getName(), e);
        }

        return versionedNotExpandableFlags;
    }

    /**
     * Gets the metadata map, which link xml element name with metadata class and
     * cardinality for a given ComplexListType subclass.
     *
     * @param complexListTypeMetadataClass the complex list type metadata class
     * @return the metadata map
     * @throws SEDALibException if the @ComplexListMetadataMap annotated static variable doesn't exist or is badly formed
     */
    static Map<String, ComplexListMetadataKind> getMetadataMap(Class<?> complexListTypeMetadataClass) throws SEDALibException {
        final Map<SedaVersion, Map<String, ComplexListMetadataKind>> metadataDefinitions = ComplexListInterface
            .metadataDefinitions(complexListTypeMetadataClass);

        if (metadataDefinitions.containsKey(SedaContext.getVersion())) {
            return metadataDefinitions.get(SedaContext.getVersion());
        }

        return metadataDefinitions.values().stream()
            .findFirst()
            .orElseThrow(() -> {
                final String errorMessage = "Le type "
                    + complexListTypeMetadataClass.getName()
                    + " ne dispose pas de définitions seda versionnée";

                return new SEDALibException(errorMessage);
            });
    }

    /**
     * Checks if the metadata list is closed for a given ComplexListType subclass.
     *
     * @param complexListTypeMetadataClass the complex list type metadata class
     * @return true, if is not expendable
     */
    static Boolean isNotExpandable(Class<?> complexListTypeMetadataClass) {
        final Map<SedaVersion, Boolean> notExpandableFlags = ComplexListInterface
            .notExpandableFlags(complexListTypeMetadataClass);

        if (notExpandableFlags.containsKey(SedaContext.getVersion())) {
            return notExpandableFlags.get(SedaContext.getVersion());
        }

        return false;
    }

    /**
     * Gets the metadata map associated with this metadata type.
     * The map links XML element names with their corresponding metadata class and cardinality information.
     *
     * @return the metadata map containing element name to metadata kind mappings
     */
    LinkedHashMap<String, ComplexListMetadataKind> getMetadataMap() throws SEDALibException;

    /**
     * Checks if this metadata type is not expandable, meaning it has a fixed set of allowed metadata elements.
     *
     * @return true if the metadata list is not expandable, false if new elements can be added
     */
    boolean isNotExpandable();

    /**
     * Gets the list of metadata elements contained in this metadata type.
     *
     * @return the list of SEDAMetadata elements
     */
    List<SEDAMetadata> getMetadataList();

    /**
     * Sets the list of metadata elements for this metadata type.
     *
     * @param metadataList the list of SEDAMetadata elements to set
     */
    void setMetadataList(List<SEDAMetadata> metadataList);

    /**
     * Gets the XML element name associated with this metadata type.
     * <p>
     * This abstract method should be implemented to return the name that will be used
     * as the XML element name when serializing the metadata to XML format.
     * <p>
     * For example, a 'Title' metadata would return "Title" as its XML element name.
     *
     * @return the XML element name for this metadata type
     */
    String getXmlElementName();

    /**
     * Wraps primitive types in their corresponding object wrapper classes.
     * <p>
     * This method converts primitive type classes to their equivalent wrapper classes:
     * If the input is not a primitive type, returns the original type unchanged.
     *
     * @param type the class type to wrap
     * @return the wrapped class type for primitives, or the original type if not primitive
     */
    static private Class<?> wrapPrimitives(Class<?> type) {
        if (!type.isPrimitive())
            return type;
        if (type == int.class)
            return (Integer.class);
        if (type == long.class)
            return (Long.class);
        if (type == double.class)
            return (Double.class);
        if (type == float.class)
            return (Float.class);
        if (type == boolean.class)
            return (Boolean.class);
        if (type == char.class)
            return (Character.class);
        if (type == byte.class)
            return (Byte.class);
        if (type == short.class)
            return (Short.class);
        return type;
    }

    /**
     * Checks if the provided arguments are compatible with the expected parameter types.
     * <p>
     * Verifies that:
     * <ul>
     *   <li>The number of arguments matches the number of parameters</li>
     *   <li>Each argument can be assigned to its corresponding parameter type</li>
     *   <li>Handles null values and primitive type parameters appropriately</li>
     * </ul>
     *
     * @param args       the actual arguments to check
     * @param paramTypes the expected parameter types from a constructor
     * @return true if the arguments are compatible, false otherwise
     */
    static private boolean areArgsCompatible(Object[] args, Class<?>[] paramTypes) {
        if (args.length != paramTypes.length)
            return false;
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            Class<?> expectedType = paramTypes[i];
            if (arg == null) {
                if (expectedType.isPrimitive())
                    return false;
                continue;
            }
            if (!wrapPrimitives(expectedType).isAssignableFrom(arg.getClass()))
                return false;
        }
        return true;
    }

    /**
     * Returns the first constructor from the given class that is compatible with the provided arguments.
     * <p>
     * A constructor is considered compatible if its parameter types match the runtime types of the arguments,
     * accounting for primitive types and their wrapper classes.
     *
     * @param clazz the class to search for compatible constructors
     * @param args  array of arguments to match against constructor parameters
     * @return the first compatible constructor found, or null if no compatible constructor exists
     */
    static private Constructor<?> getCompatibleConstructor(Class<?> clazz, Object[] args) {
        Constructor<?>[] constructors = clazz.getConstructors();
        for (Constructor<?> constructor : constructors) {
            if (areArgsCompatible(args, constructor.getParameterTypes()))
                return constructor;
        }
        return null;
    }

    /**
     * Prepares constructor arguments by optionally adding the element name as the first argument.
     * <p>
     * For named types (classes in the .namedtype package), this method prepends the element name
     * to the original arguments array. For non-named types, returns the original arguments unchanged.
     *
     * @param isNamedType  whether this is a named type that requires the element name
     * @param elementName  name of the element to prepend if needed
     * @param originalArgs array of original constructor arguments
     * @return array of arguments with element name prepended if needed
     */
    static private Object[] prepareConstructorArgs(
            boolean isNamedType,
            String elementName,
            Object[] originalArgs
    ) {
        if (!isNamedType) {
            return originalArgs;
        }
        Object[] prefixed = new Object[originalArgs.length + 1];
        prefixed[0] = elementName;
        System.arraycopy(originalArgs, 0, prefixed, 1, originalArgs.length);
        return prefixed;
    }

    /**
     * Constructs an instance of SEDAMetadata using a suitable constructor based on the provided arguments.
     * <p>
     * If the class belongs to the ".namedtype." package, the element name is prepended to the arguments.
     * Otherwise, the arguments are used directly to match an available constructor.
     *
     * @param metadataClass the class of the metadata to be constructed
     * @param elementName   the name of the metadata element
     * @param args          the arguments for the constructor (can be empty)
     * @return an initialized instance of SEDAMetadata
     * @throws SEDALibException          if no suitable constructor is found or instantiation fails
     * @throws IllegalAccessException    if access to the constructor is denied
     * @throws InvocationTargetException if constructor invocation results in an exception
     * @throws InstantiationException    if the metadataClass cannot be instantiated
     */
    static private SEDAMetadata getClassMemberConstructedFromArgs(Class<?> metadataClass, String elementName, Object[] args) throws SEDALibException, IllegalAccessException, InvocationTargetException, InstantiationException {
        boolean isNamedType = metadataClass.getName().contains(".namedtype.");
        SEDAMetadata sm;

        Object[] constructorArgs = prepareConstructorArgs(isNamedType, elementName, args);
        Constructor<?> compatibleCtor
                = getCompatibleConstructor(metadataClass, constructorArgs);

        if (compatibleCtor == null) {
            // Try to use a constructor with a variable argument list (Object... args) pattern
            try {
                if (isNamedType)
                    sm = (SEDAMetadata) metadataClass.getConstructor(String.class, Object[].class).newInstance(elementName, args);
                else
                    sm = (SEDAMetadata) metadataClass.getConstructor(Object[].class).newInstance(new Object[]{args});
            } catch (NoSuchMethodException e) {
                throw new SEDALibException("Pas de constructeur de l'élément [" + elementName + "]", e);
            }
        } else {
            // TODO vérifier que ce cas est bien illégitime!
            //        Type[] paramTypes = compatibleCtor.getGenericParameterTypes();
            //        if (paramTypes.length == 1 && paramTypes[0].equals(Object[].class)) {
            //            return (SEDAMetadata) compatibleCtor.newInstance(new Object[] { args });
            //        }
            return (SEDAMetadata) compatibleCtor.newInstance(constructorArgs);
        }
        return sm;
    }


    /**
     * Constructs a new instance of SEDA metadata.
     * <p>
     * Retrieves the metadata class from the map using the element name and dynamically instantiates the object.
     * If the element name is not found, an {@code AnyXMLType} fallback class is used.
     *
     * @param elementName the name of the metadata element to create
     * @param args        the arguments passed to the constructor of the metadata element
     * @return the instantiated SEDAMetadata
     * @throws SEDALibException if the metadata element could not be created due to invalid arguments or missing constructors
     */
    default SEDAMetadata newSEDAMetadata(String elementName, Object[] args) throws SEDALibException {
        LinkedHashMap<String, ComplexListMetadataKind> metadataMap = this.getMetadataMap();
        if (metadataMap == null)
            throw new SEDALibException("Impossible de construire l'élément [" + elementName + "] en "+ SedaContext.getVersion(), null);
        ComplexListMetadataKind metadataKind = metadataMap.get(elementName);
        try {
            Class<?> metadataClass = (metadataKind != null ? metadataKind.getMetadataClass() : AnyXMLType.class);
            SEDAMetadata metadata = getClassMemberConstructedFromArgs(metadataClass, elementName, args);
            if (metadata == null)
                throw new SEDALibException("Impossible de construire l'élément [" + elementName + "]");
            return metadata;
        } catch (SecurityException | InstantiationException | IllegalAccessException
                 | IllegalArgumentException e) {
            throw new SEDALibException("Impossible de construire l'élément [" + elementName + "]", e);
        } catch (InvocationTargetException te) {
            throw new SEDALibException("Impossible de construire l'élément [" + elementName + "]", te.getCause());
        }
    }

    /**
     * Retrieves the position (index) of a metadata element within the metadata map.
     * <p>
     * Searches for the specified element name in the provided metadata map and
     * returns its zero-based position. If the element is not found, returns -1.
     *
     * @param elementName the name of the metadata element to find
     * @return the zero-based index of the metadata element if found, or -1 if not present
     */
    default int indexOfMetadata(String elementName) throws SEDALibException {
        LinkedHashMap<String, ComplexListMetadataKind> metadataMap = this.getMetadataMap();
        int index = 0;
        for (String key : metadataMap.keySet()) {
            if (key.equals(elementName))
                return index;
            index++;
        }
        return -1;
    }

    /**
     * Adds a new metadata element to the metadata list or replaces it if the element already exists and is not multi-valued.
     * <p>
     * Determines whether the element is part of the metadata map and constructs it dynamically. Non-expandable
     * schemas will reject unknown elements by throwing an exception.
     * <p>
     * For example, to add certain metadata types:
     * - See Gps constructors {@link Gps#Gps(String, int, String, String, String)}
     * - See AgentType constructors {@link AgentType#AgentType(String, String)}, {@link AgentType#AgentType(String, String, String)}
     *
     * @param elementName the name of the metadata element
     * @param args        the arguments passed to the metadata constructor
     * @throws SEDALibException if the element cannot be added due to invalid arguments or schema constraints
     */
    default void addNewMetadata(String elementName, Object... args) throws SEDALibException {
        if (args.length > 0) {
            try  {
                SEDAMetadata sm = newSEDAMetadata(elementName, args);
                addMetadata(sm);
            }
            catch(SEDALibException e) {
                throw new SEDALibException("Impossible d'ajouter l'élément [" + elementName + "] au type composé ["
                        +this.getXmlElementName()+"] en "+ SedaContext.getVersion(), e);
            }
        }
    }

    /**
     * Helper class that stores information about where to insert a new metadata item
     * into the metadata list.
     */
    class InsertionInfo {
        /**
         * The index position where the new metadata should be inserted
         */
        final int insertionIndex;
        /**
         * Whether to replace an existing metadata at that position
         */
        final boolean replaceExisting;

        /**
         * Constructor for InsertionInfo
         *
         * @param insertionIndex  The index where new metadata should be inserted
         * @param replaceExisting Whether to replace existing metadata at that position
         */
        InsertionInfo(int insertionIndex, boolean replaceExisting) {
            this.insertionIndex = insertionIndex;
            this.replaceExisting = replaceExisting;
        }
    }

    /**
     * Compute insertion position for a known metadata type.
     * <p>
     * For metadata types defined in the metadata map, this determines where the new
     * metadata should be inserted to maintain proper ordering, and whether it should
     * replace an existing entry.
     *
     * @param newIndex Index of the new metadata type in the metadata map
     * @param isMany   Whether this metadata type allows multiple values
     * @return InsertionInfo with computed insertion position and replacement flag
     */
    private InsertionInfo computeInsertionInfoForKnownType(
            int newIndex, boolean isMany) throws SEDALibException {
        List<SEDAMetadata> metadataList = getMetadataList();
        int index = 0;
        boolean replaceExisting = false;

        for (SEDAMetadata item : metadataList) {
            int currentIndex = indexOfMetadata(item.getXmlElementName());
            if (!isMany && currentIndex == newIndex) {
                replaceExisting = true;
                break;
            }
            if (currentIndex == -1 || currentIndex > newIndex) {
                break;
            }
            index++;
        }
        return new InsertionInfo(index, replaceExisting);
    }

    /**
     * Compute insertion position for an unknown metadata type.
     * <p>
     * For metadata types not defined in the metadata map, this determines where the
     * new metadata should be inserted. If the schema is not expandable, this will
     * throw an exception.
     *
     * @param xmlName       Name of the metadata element being inserted
     * @param notExpandable Whether the schema allows new metadata types
     * @return InsertionInfo with computed insertion position
     * @throws SEDALibException if schema is not expandable
     */
    private InsertionInfo computeInsertionInfoForUnknownType(
            String xmlName,
            boolean notExpandable) throws SEDALibException {
        List<SEDAMetadata> metadataList = getMetadataList();

        if (notExpandable) {
            throw new SEDALibException("Impossible d'étendre le schéma avec des métadonnées non prévues [" + xmlName + "]");
        }
        int index = 0;
        boolean seen = false;
        for (SEDAMetadata item : metadataList) {
            String name = item.getXmlElementName();
            if (name.equals(xmlName)) {
                seen = true;
            } else if (seen) {
                break;
            }
            index++;
        }
        return new InsertionInfo(index, false);
    }

    /**
     * Adds a new metadata element to the metadata list or replaces it if it already exists
     * and the metadata cannot have multiple values. Does nothing if the provided metadata is null.
     *
     * @param metadata the metadata to add or replace
     * @throws SEDALibException if adding unknown metadata to a non-expandable type or if other constraints are violated
     */
    default void addMetadata(SEDAMetadata metadata) throws SEDALibException {
        if (metadata == null)
            return;

        List<SEDAMetadata> metadataList = getMetadataList();
        boolean notExpandable = isNotExpandable();
        String xmlName = metadata.getXmlElementName();
        int newIndex = indexOfMetadata(xmlName);


        InsertionInfo info = (newIndex == -1)
                ? computeInsertionInfoForUnknownType(xmlName, notExpandable)
                : computeInsertionInfoForKnownType(newIndex, isAMultiValuedMetadata(xmlName));

        if (info.replaceExisting) {
            metadataList.set(info.insertionIndex, metadata);
        } else {
            metadataList.add(info.insertionIndex, metadata);
        }
    }

    /**
     * Checks if a specific metadata is missing in the provided metadata list.
     *
     * @param elementName the name of the metadata element to find
     * @return true if the metadata is not present, false otherwise
     */
    default boolean isMetadataLacking(String elementName) {
        List<SEDAMetadata> metadataList = this.getMetadataList();
        for (SEDAMetadata sm : metadataList) {
            if (sm.getXmlElementName().equals(elementName))
                return false;
        }
        return true;
    }

    /**
     * Converts a metadata list into its SEDA XML representation using the provided writer.
     *
     * @param xmlWriter the writer used to generate XML
     * @throws SEDALibException if any metadata cannot be written to XML
     */
    default void toSedaXmlMetadataList(SEDAXMLStreamWriter xmlWriter) throws SEDALibException {
        List<SEDAMetadata> metadataList = getMetadataList();
        for (SEDAMetadata sm : metadataList)
            sm.toSedaXml(xmlWriter);
    }

    /**
     * Converts a metadata list into a CSV-compatible map representation.
     *
     * @return a LinkedHashMap with keys as metadata names and values as their corresponding values
     * @throws SEDALibException if metadata conversion encounters an error
     */
    default LinkedHashMap<String, String> toCsvList() throws SEDALibException {
        List<SEDAMetadata> metadataList = getMetadataList();
        LinkedHashMap<String, ComplexListMetadataKind> metadataMap = getMetadataMap();
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        String previousXMLElementName = null;
        int count = 0;
        for (SEDAMetadata sm : metadataList) {
            if (!sm.getXmlElementName().equals(previousXMLElementName)) {
                previousXMLElementName = sm.getXmlElementName();
                count = 0;
            } else count++;
            final String addedName;
            if (isAMultiValuedMetadata(sm.getXmlElementName()))
                addedName = sm.getXmlElementName() + "." + count;
            else
                addedName = sm.getXmlElementName();
            LinkedHashMap<String, String> smCsvList = sm.toCsvList();
            for (Map.Entry<String, String> e : smCsvList.entrySet()) {
                result.put(addedName + (e.getKey().isEmpty() ? "" : "." + e.getKey()), e.getValue());
            }
        }
        return result;
    }

    /**
     * Import the metadata content in XML expected form from the SEDA Manifest.
     *
     * @param xmlReader the SEDAXMLEventReader reading the SEDA manifest
     * @return true, if it finds something convenient, false if not
     * @throws SEDALibException if the XML can't be read or the SEDA scheme is not respected, for example
     */
    default boolean fillFromSedaXmlMetadataList(SEDAXMLEventReader xmlReader) throws SEDALibException {
        List<SEDAMetadata> metadataList = getMetadataList();
        LinkedHashMap<String, ComplexListMetadataKind> metadataMap = getMetadataMap();
        boolean isNotExpandable = isNotExpandable();
        Class<?> metadataClass;
        try {
            String tmp = xmlReader.peekName();
            while (tmp != null) {
                ComplexListMetadataKind mi = metadataMap.get(tmp);
                if (mi == null) {
                    if (isNotExpandable)
                        throw new SEDALibException(
                                "Impossible d'étendre le schéma avec des métadonnées non prévues ["
                                        + tmp + "]");
                    else
                        metadataClass = AnyXMLType.class;
                } else
                    metadataClass = mi.getMetadataClass();
                SEDAMetadata sm = SEDAMetadata.fromSedaXml(xmlReader, metadataClass);
                this.addMetadata(sm);
                tmp = xmlReader.peekName();
            }
        } catch (XMLStreamException | IllegalArgumentException | SEDALibException e) {
            throw new SEDALibException("Erreur de lecture XML dans un élément composé", e);
        }
        return true;
    }

    /**
     * Resolves the metadata class for a given element name using the metadata map.
     * <p>
     * Looks up the metadata class in the provided map. If not found and schema is expandable,
     * returns AnyXMLType as a fallback. If schema is not expandable and element is not found,
     * throws an exception.
     *
     * @param name          name of the metadata element to resolve class for
     * @param metadataMap   map of defined metadata elements and their types
     * @param notExpandable whether schema allows undefined elements
     * @return resolved metadata class
     * @throws SEDALibException if element not found in non-expandable schema
     */
    static private Class<?> resolveMetadataClass(String name,
                                                 Map<String, ComplexListMetadataKind> metadataMap,
                                                 boolean notExpandable)
            throws SEDALibException {
        ComplexListMetadataKind kind = metadataMap.get(name);
        if (kind != null) {
            return kind.getMetadataClass();
        }
        if (notExpandable) {
            throw new SEDALibException("Impossible d'étendre le schéma avec des métadonnées non prévues ["
                    + name + "]");
        }
        return AnyXMLType.class;
    }

    /**
     * Add metadata from fragments in XML expected form for the SEDA Manifest.
     *
     * @param xmlData the xml data
     * @throws SEDALibException if the XML can't be read or the SEDA scheme is not respected
     */
    default void addSedaXmlFragments(String xmlData) throws SEDALibException {
        LinkedHashMap<String, ComplexListMetadataKind> metadataMap = this.getMetadataMap();
        boolean isNotExpandable = this.isNotExpandable();

        try (ByteArrayInputStream bais = new ByteArrayInputStream(xmlData.getBytes(StandardCharsets.UTF_8));
             SEDAXMLEventReader xmlReader = new SEDAXMLEventReader(bais, true)) {

            xmlReader.nextUsefullEvent(); // jump document start
            String elementName = xmlReader.peekName();
            while (elementName != null) {
                Class<?> metadataClass = resolveMetadataClass(elementName, metadataMap, isNotExpandable);
                SEDAMetadata metadata = SEDAMetadata.fromSedaXml(xmlReader, metadataClass);
                addMetadata(metadata);
                elementName = xmlReader.peekName();
            }
        } catch (XMLStreamException | IllegalArgumentException | SEDALibException | IOException e) {
            throw new SEDALibException("Erreur de lecture XML de fragments d'un élément composé", e);
        }
    }

    /**
     * Retrieves the first instance of metadata with the specified name from the metadata list.
     *
     * @param elementName the name of the metadata element to retrieve
     * @return the first matching metadata or null if not found
     */
    default SEDAMetadata getFirstNamedMetadata(String elementName) {
        List<SEDAMetadata> metadataList = this.getMetadataList();
        for (SEDAMetadata sm : metadataList) {
            if (elementName.equals(sm.getXmlElementName()))
                return sm;
        }
        return null;
    }

    /**
     * Removes the first instance of metadata with the specified name from the metadata list.
     *
     * @param elementName the name of the metadata element to retrieve and remove
     * @return the first matching metadata or null if not found
     */
    default boolean removeFirstNamedMetadata(String elementName) {
        List<SEDAMetadata> metadataList = this.getMetadataList();
        for (int i = 0; i < metadataList.size(); i++) {
            if (elementName.equals(metadataList.get(i).getXmlElementName())) {
                metadataList.remove(i);
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves the String value of a simple metadata (like String, Text, or DateTime type) identified by a name.
     *
     * @param metadataName the name of the metadata to retrieve
     * @return the value of the metadata as a String, or null if not found
     */
    default String getSimpleMetadata(String metadataName) {
        List<SEDAMetadata> metadataList = this.getMetadataList();
        String langText = null;
        for (SEDAMetadata sm : metadataList) {
            if (sm.getXmlElementName().equals(metadataName)) {
                if (sm instanceof StringType)
                    return ((StringType) sm).getValue();
                else if ((sm instanceof TextType) && (((TextType) sm).getLang() == null))
                    return ((TextType) sm).getValue();
                else if ((sm instanceof TextType) && (((TextType) sm).getLang().equals("fr")))
                    langText = ((TextType) sm).getValue();
                else if ((sm instanceof EnumType))
                    return ((EnumType) sm).getValue();
                else if (sm instanceof DateTimeType) {
                    return (((DateTimeType) sm).getDateTimeString());
                }
            }
        }
        return langText;
    }

    /**
     * Determines whether a specific metadata element is allowed to have multiple values.
     *
     * @param metadataName the name of the metadata element to check
     * @return true if the metadata allows multiple values, false otherwise
     * @throws SEDALibException if the metadata map is malformed or does not exist
     */
    default boolean isAMultiValuedMetadata(String metadataName) throws SEDALibException {
        LinkedHashMap<String, ComplexListMetadataKind> metadataMap = getMetadataMap();
        ComplexListMetadataKind clmk = metadataMap.get(metadataName);
        if (clmk == null)
            return true;
        return clmk.isMany();
    }
}
/**
 * Copyright French Prime minister Office/DINSIC/Vitam Program (2015-2019)
 * <p>
 * contact.vitam@programmevitam.fr
 * <p>
 * This software is developed as a validation helper tool, for constructing Submission Information Packages (archives
 * sets) in the Vitam program whose purpose is to implement a digital archiving back-office system managing high
 * volumetry securely and efficiently.
 * <p>
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA archiveDeliveryRequestReply the following URL "http://www.cecill.info".
 * <p>
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 * <p>
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 * <p>
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */
package fr.gouv.vitam.tools.sedalib.metadata.namedtype;

import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.content.Gps;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLStreamWriter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * The Class ComplexListType.
 * <p>
 * For abstract SEDA metadata composed with list of other metadata or types
 */
public abstract class ComplexListType extends NamedTypeMetadata {

    /**
     * The Sub type metadata ordered list map.
     */
    static protected HashMap<Class, List<String>> subTypeMetadataOrderedListMap = new HashMap<Class, List<String>>();
    /**
     * The Sub type metadata map map.
     */
    static protected HashMap<Class, LinkedHashMap<String, ComplexListMetadataKind>> subTypeMetadataMapMap =
            new HashMap<Class, LinkedHashMap<String, ComplexListMetadataKind>>();
    /**
     * The Sub type expandable map.
     */
    static protected HashMap<Class, Boolean> subTypeNotExpandableMap =
            new HashMap<Class, Boolean>();

    /**
     * The metadata list.
     */
    public List<SEDAMetadata> metadataList;

    /**
     * Instantiates a new management.
     *
     * @param elementName the element name
     */
    public ComplexListType(String elementName) {
        super(elementName);
        this.metadataList = new ArrayList<SEDAMetadata>();
    }

    /**
     * Gets convenient constructor for args.
     *
     * @param metadataClass the metadata class
     * @param args          the args
     * @return the convenient constructor for args
     * @throws SEDALibException the seda lib exception
     */
    private static SEDAMetadata getClassMemberConstructedFromArgs(Class<?> metadataClass, String elementName, Object[] args) throws SEDALibException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Object[] newArgs;
        boolean needName = metadataClass.getName().contains(".namedtype.");
        SEDAMetadata sm;
        args = ArrayUtils.nullToEmpty(args);
        if (needName) {
            newArgs = new Object[args.length + 1];
            newArgs[0] = elementName;
            System.arraycopy(args, 0, newArgs, 1, args.length);
        } else
            newArgs = args;
        Class<?>[] parameterTypes = ClassUtils.toClass(newArgs);
        Constructor theConstructor = ConstructorUtils.getMatchingAccessibleConstructor(metadataClass, parameterTypes);
        if (theConstructor == null) {
            try {
                if (metadataClass.getName().contains(".namedtype."))
                    sm = (SEDAMetadata) metadataClass.getConstructor(String.class, Object[].class).newInstance(elementName, args);
                else {
                    newArgs = new Object[1];
                    newArgs[0] = args;
                    sm = (SEDAMetadata) metadataClass.getConstructor(Object[].class).newInstance(newArgs);
                }
            } catch (NoSuchMethodException e) {
                throw new SEDALibException("Pas de constructeur de l'élément [" + elementName + "]");
            }
        } else {
            Type[] types = theConstructor.getGenericParameterTypes();
            if ((types.length != 1) || (!types[0].equals(Object[].class)))
                sm = (SEDAMetadata) theConstructor.newInstance(newArgs);
            else {
                newArgs = new Object[1];
                newArgs[0] = args;
                sm = (SEDAMetadata) theConstructor.newInstance(newArgs);
            }
        }
        return sm;
    }

    /**
     * Construct a new SEDA metadata.
     *
     * @param elementName the element name
     * @param args        the args
     * @return the SEDA metadata
     * @throws SEDALibException if failed in construction
     */
    private SEDAMetadata newSEDAMetadata(String elementName, Object[] args) throws SEDALibException {
        ComplexListMetadataKind mi = getMetadataMap().get(elementName);
        Constructor<?> constructor;
        int i;
        try {
            Class metadataClass;
            if (mi == null)
                metadataClass = AnyXMLType.class;
            else
                metadataClass = mi.metadataClass;
            SEDAMetadata sm = getClassMemberConstructedFromArgs(metadataClass, elementName, args);
            if (sm == null)
                throw new SEDALibException("Impossible de construire l'élément [" + elementName + "]");
            return sm;
        } catch (SecurityException | InstantiationException | IllegalAccessException
                | IllegalArgumentException e) {
            throw new SEDALibException("Impossible de construire l'élément [" + elementName + "]\n->" + e.getMessage());
        } catch (InvocationTargetException te) {
            throw new SEDALibException("Impossible de construire l'élément [" + elementName + "]\n->" + te.getTargetException().getMessage());
        }
    }

    /**
     * Adds a new metadata, or replace it if it exists and the metadata can't have
     * many values. This is a flexible constructor used to simplify metadata management.
     * <p>
     * To know what kind of args you can use in this generic method, go and see the different constructors that exists
     * for each kind of metadata, or the .
     * <p>
     * For example to know the args possibility for :
     * <ul>
     * <li>Gps, see {@link Gps#Gps(String, int, String, String, String)}</li>
     * <li> or AgentType, see {@link AgentType#AgentType(String, String)}, {@link AgentType#AgentType(String, String, String)}
     * and {@link AgentType#AgentType(String, String, String, String)}</li>
     * <li>...</li>
     * </ul>
     *
     * @param elementName the element name
     * @param args        the args of the metadata constructor
     * @throws SEDALibException if construction is not possible, most of the time wrong args
     */
    public void addNewMetadata(String elementName, Object... args) throws SEDALibException {
        int addOrderIndex, curOrderIndex, i;
        boolean manyFlag, setFlag;
        if (args.length > 0) {
            addOrderIndex = getMetadataOrderedList().indexOf(elementName);
            i = 0;
            setFlag = false;
            if (addOrderIndex == -1) {
                if (isNotExpendable())
                    throw new SEDALibException(
                            "Impossible d'étendre le schéma avec des métadonnées non prévues ["
                                    + elementName + "]");
                manyFlag = true;
                boolean noBeforeEqual = true;
                for (SEDAMetadata sm : metadataList) {
                    if ((sm.getXmlElementName().equals(elementName)) && noBeforeEqual)
                        noBeforeEqual = false;
                    if (!(sm.getXmlElementName().equals(elementName)) && !noBeforeEqual)
                        break;
                    i++;
                }
            } else {
                manyFlag = getMetadataMap().get(elementName).many;
                for (SEDAMetadata sm : metadataList) {
                    curOrderIndex = getMetadataOrderedList().indexOf(sm.getXmlElementName());
                    if ((!manyFlag) && (curOrderIndex == addOrderIndex)) {
                        setFlag = true;
                        break;
                    }
                    if ((curOrderIndex == -1) || (curOrderIndex > addOrderIndex))
                        break;
                    i++;
                }
            }
            if (manyFlag)
                metadataList.add(i, newSEDAMetadata(elementName, args));
            else {
                if (setFlag)
                    metadataList.set(i, newSEDAMetadata(elementName, args));
                else
                    metadataList.add(i, newSEDAMetadata(elementName, args));
            }
        }
    }

    /**
     * Adds a metadata, or replace it if it exists and the metadata can't have many
     * values.
     *
     * @param sedaMetadata the named type metadata
     * @throws SEDALibException if try to add an unknown metadata in a not                          expandable type
     */
    public void addMetadata(SEDAMetadata sedaMetadata) throws SEDALibException {
        int addOrderIndex, curOrderIndex, i;
        boolean manyFlag, setFlag;
        addOrderIndex = getMetadataOrderedList().indexOf(sedaMetadata.getXmlElementName());
        i = 0;
        setFlag = false;
        if (addOrderIndex == -1) {
            if (isNotExpendable())
                throw new SEDALibException(
                        "Impossible d'étendre le schéma avec des métadonnées non prévues ["
                                + elementName + "]");
            manyFlag = true;
            boolean noBeforeEqual = true;
            for (SEDAMetadata sm : metadataList) {
                if ((sm.getXmlElementName().equals(sedaMetadata.getXmlElementName())) && noBeforeEqual)
                    noBeforeEqual = false;
                if (!(sm.getXmlElementName().equals(sedaMetadata.getXmlElementName())) && !noBeforeEqual)
                    break;
                i++;
            }
        } else {
            manyFlag = getMetadataMap().get(sedaMetadata.getXmlElementName()).many;
            for (SEDAMetadata sm : metadataList) {
                curOrderIndex = getMetadataOrderedList().indexOf(sm.getXmlElementName());
                if ((!manyFlag) && (curOrderIndex == addOrderIndex)) {
                    setFlag = true;
                    break;
                }
                if ((curOrderIndex == -1) || (curOrderIndex > addOrderIndex))
                    break;
                i++;
            }
        }
        if (manyFlag)
            metadataList.add(i, sedaMetadata);
        else {
            if (setFlag)
                metadataList.set(i, sedaMetadata);
            else
                metadataList.add(i, sedaMetadata);
        }
    }

    /**
     * Checks if metadata is lacking.
     *
     * @param elementName the element name
     * @return true, if metadata is lacking
     */
    public boolean isMetadataLacking(String elementName) {
        for (SEDAMetadata sm : metadataList) {
            if (sm.getXmlElementName().equals(elementName))
                return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata#toSedaXml(fr.gouv.vitam.
     * tools.sedalib.xml.SEDAXMLStreamWriter)
     */
    public void toSedaXml(SEDAXMLStreamWriter xmlWriter) throws SEDALibException {
        try {
            xmlWriter.writeStartElement(elementName);
            for (SEDAMetadata sm : metadataList) {
                sm.toSedaXml(xmlWriter);
            }
            xmlWriter.writeEndElement();
        } catch (XMLStreamException e) {
            throw new SEDALibException("Erreur d'écriture XML dans un élément d'un ComplexListType [" + getXmlElementName() + "]\n->" + e.getMessage());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata#toCsvList()
     */
    public LinkedHashMap<String, String> toCsvList() throws SEDALibException {
        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
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
            smCsvList.entrySet().stream().forEach(e -> {
                result.put(addedName + (e.getKey().isEmpty()?"":"."+e.getKey()), e.getValue());
            });
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
    public boolean fillFromSedaXml(SEDAXMLEventReader xmlReader) throws SEDALibException {
        Class<?> metadataClass;
        try {
            if (xmlReader.nextBlockIfNamed(elementName)) {
                xmlReader.peekUsefullEvent();
                String tmp = xmlReader.peekName();
                while (tmp != null) {
                    ComplexListMetadataKind mi = getMetadataMap().get(tmp);
                    if (mi == null) {
                        if (isNotExpendable())
                            throw new SEDALibException(
                                    "Impossible d'étendre le schéma avec des métadonnées non prévues ["
                                            + tmp + "]");
                        else
                            metadataClass = AnyXMLType.class;
                    } else
                        metadataClass = mi.metadataClass;
                    SEDAMetadata sm = SEDAMetadata.fromSedaXml(xmlReader, metadataClass);
                    addMetadata(sm);
                    tmp = xmlReader.peekName();
                }
                xmlReader.endBlockNamed(elementName);
            } else
                return false;
        } catch (XMLStreamException | IllegalArgumentException | SEDALibException e) {
            throw new SEDALibException("Erreur de lecture XML dans un élément [" + elementName + "]\n->" + e.getMessage());
        }
        return true;
    }

    /**
     * Add metadata from fragments in XML expected form for the SEDA Manifest.
     *
     * @param xmlData the xml data
     * @throws SEDALibException if the XML can't be read or the SEDA scheme is not                          respected
     */
    public void addSedaXmlFragments(String xmlData) throws SEDALibException {
        Class<?> metadataClass;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(xmlData.getBytes("UTF-8"));
             SEDAXMLEventReader xmlReader = new SEDAXMLEventReader(bais, true)) {
            // jump document start
            xmlReader.nextUsefullEvent();
            String tmp = xmlReader.peekName();
            while (tmp != null) {
                ComplexListMetadataKind mi = getMetadataMap().get(tmp);
                if (mi == null) {
                    if (isNotExpendable())
                        throw new SEDALibException(
                                "Impossible d'étendre le schéma avec des métadonnées non prévues ["
                                        + tmp + "]");
                    else
                        metadataClass = AnyXMLType.class;
                } else
                    metadataClass = mi.metadataClass;
                SEDAMetadata sm = SEDAMetadata.fromSedaXml(xmlReader, metadataClass);
                addMetadata(sm);
                tmp = xmlReader.peekName();
            }
        } catch (XMLStreamException | IllegalArgumentException | SEDALibException | IOException e) {
            throw new SEDALibException("Erreur de lecture XML de fragments d'un élément Content\n->" + e.getMessage());
        }
    }

    private static void getNewComplexListSubType(Class subClass) throws SEDALibException {
        List<Field> fields = FieldUtils.getFieldsListWithAnnotation(subClass, ComplexListMetadataMap.class);
        if (fields.isEmpty())
            throw new SEDALibException("Le type " + subClass + " n'a pas de variable annotée @ComplexListMetadataMap accessible");
        else if (fields.size() > 1)
            throw new SEDALibException("Le type " + subClass + " a plusieurs variables annotées @ComplexListMetadataMap accessibles (classe ou parents)");

        Object object;
        LinkedHashMap<String, ComplexListMetadataKind> metadataMap;
        try {
            object = fields.get(0).get(null);
        } catch (IllegalAccessException e) {
            throw new SEDALibException("La variable " + fields.get(0) + " annotée @ComplexListMetadataMap du type " + subClass + " ne peut être accédée");
        }
        try {
            metadataMap = (LinkedHashMap<String, ComplexListMetadataKind>) object;
        } catch (ClassCastException e) {
            throw new SEDALibException("La variable " + fields.get(0) + " annotée @ComplexListMetadataMap du type " + subClass + " n'est pas de type LinkedHashMap<String,ComplexListMetadataKind>");
        }
        subTypeMetadataMapMap.put(subClass, metadataMap);
        subTypeMetadataOrderedListMap.put(subClass, new ArrayList(metadataMap.keySet()));
        subTypeNotExpandableMap.put(subClass, !fields.get(0).getAnnotation(ComplexListMetadataMap.class).isExpandable());
    }

    /**
     * Gets the metadata ordered list.
     *
     * @return the metadata ordered list
     * @throws SEDALibException if the @ComplexListMetadataMap annotated static variable doesn't exist or is badly formed
     */
    public List<String> getMetadataOrderedList() throws SEDALibException {
        List<String> metadataOrderedList = subTypeMetadataOrderedListMap.get(this.getClass());
        if (metadataOrderedList == null) {
            getNewComplexListSubType(this.getClass());
            metadataOrderedList = subTypeMetadataOrderedListMap.get(this.getClass());
        }
        return metadataOrderedList;
    }

    /**
     * Gets the metadata map, which link xml element name with metadata class and
     * cardinality.
     *
     * @return the metadata map
     * @throws SEDALibException if the @ComplexListMetadataMap annotated static variable doesn't exist or is badly formed
     */
    public HashMap<String, ComplexListMetadataKind> getMetadataMap() throws SEDALibException {
        HashMap<String, ComplexListMetadataKind> metadataMap = subTypeMetadataMapMap.get(this.getClass());
        if (metadataMap == null) {
            getNewComplexListSubType(this.getClass());
            metadataMap = subTypeMetadataMapMap.get(this.getClass());
        }
        return metadataMap;
    }

    /**
     * Gets the metadata map, which link xml element name with metadata class and
     * cardinality for a given ComplexListType sub class.
     *
     * @param complexListTypeMetadataClass the complex list type metadata class
     * @return the metadata map
     */
    static public HashMap<String, ComplexListMetadataKind> getMetadataMap(Class complexListTypeMetadataClass) {
        HashMap<String, ComplexListMetadataKind> metadataMap = subTypeMetadataMapMap.get(complexListTypeMetadataClass);
        return metadataMap;
    }

    /**
     * Checks if it the metadata list is closed.
     *
     * @return true, if is not expendable
     * @throws SEDALibException if the @ComplexListMetadataMap annotated static variable doesn't exist or is badly formed
     */
    public boolean isNotExpendable() throws SEDALibException {
        Boolean isNotExpandable = subTypeNotExpandableMap.get(this.getClass());
        if (isNotExpandable == null) {
            getNewComplexListSubType(this.getClass());
            isNotExpandable = subTypeNotExpandableMap.get(this.getClass());
        }
        return isNotExpandable;
    }

    /**
     * Checks if it the metadata list is closed for a given ComplexListType sub class.
     *
     * @return true, if is not expendable
     */
    static public Boolean isNotExpendable(Class complexListTypeMetadataClass) {
        return subTypeNotExpandableMap.get(complexListTypeMetadataClass);
    }

    /**
     * Gets the value of a simple metadata (String, Text or DateTime type) determined only by a metadata name in String
     * format or null if not found
     *
     * @param metadataName the metadata name
     * @return the String formatted metadata value
     */
    public String getSimpleMetadata(String metadataName) {
        String langText = null;
        for (SEDAMetadata sm : metadataList) {
            if (sm.getXmlElementName().equals(metadataName)) {
                if (sm instanceof StringType)
                    return ((StringType) sm).getValue();
                else if ((sm instanceof TextType) && (((TextType) sm).getLang() == null))
                    return ((TextType) sm).getValue();
                else if ((sm instanceof TextType) && (((TextType) sm).getLang().equals("fr")))
                    langText = ((TextType) sm).getValue();
                else if (sm instanceof DateTimeType) {
                    return SEDAXMLStreamWriter.getStringFromDateTime(((DateTimeType) sm).getValue());
                }
            }
        }
        return langText;
    }

    /**
     * Is a multi valued metadata boolean.
     *
     * @param metadataName the metadata name
     * @return the boolean
     */
    public boolean isAMultiValuedMetadata(String metadataName) {
        ComplexListMetadataKind clmk = subTypeMetadataMapMap.get(this.getClass()).get(metadataName);
        if (clmk == null)
            return true;
        return clmk.many;
    }
}

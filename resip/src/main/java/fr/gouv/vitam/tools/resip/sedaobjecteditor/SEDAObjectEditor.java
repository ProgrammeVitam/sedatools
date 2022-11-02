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
 * circulated by CEA, CNRS and INRIA archiveTransfer the following URL "http://www.cecill.info".
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
package fr.gouv.vitam.tools.resip.sedaobjecteditor;

import fr.gouv.vitam.tools.resip.sedaobjecteditor.components.structuredcomponents.SEDAObjectEditorPanel;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.composite.ComplexListTypeEditor;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.composite.RuleTypeEditor;
import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.ComplexListType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.RuleType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static fr.gouv.vitam.tools.resip.sedaobjecteditor.SEDAObjectEditorConstants.translateTag;

/**
 * The SEDA object editor class.
 * <p>
 * This is the controller class for structured edition of SEDAMetadata and high-level objects
 * like BinaryDataObject, PhysicalDataObject, ArchiveUnit.
 * <p>
 * The data is in the SEDAMetadata... objects, and the visual is in SEDAObjectEditorPanels.
 */
abstract public class SEDAObjectEditor {

    /**
     * The SEDA object, either SEDAMetadata or high-level objects.
     */
    protected Object editedObject;

    /**
     * The SEDA object editor father if any.
     */
    protected SEDAObjectEditor father;

    /**
     * The editedObject panel.
     */
    protected SEDAObjectEditorPanel sedaObjectEditorPanel;

    /**
     * The constant BASE_FONT.
     */
    public static Font BASE_FONT = UIManager.getFont("Label.font");

    /**
     * The constant LABEL_FONT.
     */
    public static Font LABEL_FONT = BASE_FONT.deriveFont(BASE_FONT.getSize() + (float) 2.0);

    /**
     * The constant MINI_LABEL_FONT.
     */
    public static Font MINI_LABEL_FONT = LABEL_FONT.deriveFont(LABEL_FONT.getSize() - (float) 4.0);

    /**
     * The constant ITALIC_LABEL_FONT.
     */
    public static Font ITALIC_LABEL_FONT = LABEL_FONT.deriveFont(LABEL_FONT.getStyle() | Font.ITALIC);

    /**
     * The constant BOLD_LABEL_FONT.
     */
    public static Font BOLD_LABEL_FONT = LABEL_FONT.deriveFont(LABEL_FONT.getStyle() | Font.BOLD);

    /**
     * The constant EDIT_FONT.
     */
    public static Font EDIT_FONT = BASE_FONT.deriveFont(BASE_FONT.getSize() + (float) 2.0);

    /**
     * The constant MINI_EDIT_FONT.
     */
    public static Font MINI_EDIT_FONT = EDIT_FONT.deriveFont(EDIT_FONT.getSize() - (float) 4.0);

    /**
     * The constant GENERAL_BACKGROUND.
     */
    public static Color GENERAL_BACKGROUND = UIManager.getColor("Label.background");

    /**
     * The constant GENERAL_BACKGROUND.
     */
    public static Color GENERAL_FOREGROUND = UIManager.getColor("Label.foreground");

    /**
     * The constant COMPOSITE_LABEL_COLOR.
     */
    public static Color COMPOSITE_LABEL_COLOR = new Color(
            Math.min(GENERAL_FOREGROUND.getRed() + 64, 255),
            Math.min(GENERAL_FOREGROUND.getGreen() + 64, 255),
            Math.min(GENERAL_FOREGROUND.getBlue() + 200, 255));

    /**
     * The constant COMPOSITE_LABEL_SEPARATOR_COLOR.
     */
    public static Color COMPOSITE_LABEL_SEPARATOR_COLOR = new Color(
            (int) (GENERAL_BACKGROUND.getRed() * 0.9),
            (int) (GENERAL_BACKGROUND.getGreen() * 0.9),
            (int) (GENERAL_BACKGROUND.getBlue() * 0.9));

    /**
     * The constant COMPOSITE_LABEL_MARKUP_COLOR.
     */
    public static Color COMPOSITE_LABEL_MARKUP_COLOR = new Color(
            (int) (GENERAL_BACKGROUND.getRed() * 0.4),
            (int) (GENERAL_BACKGROUND.getGreen() * 0.4),
            (int) (GENERAL_BACKGROUND.getBlue() * 0.4));

    /**
     * The constant COMPOSITE_LABEL_ATTRIBUTE_COLOR.
     */
    public static Color COMPOSITE_LABEL_ATTRIBUTE_COLOR = new Color(
            (int) (GENERAL_BACKGROUND.getRed() * 0.7),
            0,
            (int) (GENERAL_BACKGROUND.getBlue() * 0.7));

    //
    // All static methods dealing with SEDAMetadata and SEDAMetadata editors
    //

    private static Class getMetadataClass(String simpleMetadataType) throws SEDALibException {
        Class result;
        String metadataType = "fr.gouv.vitam.tools.sedalib.metadata.content." + simpleMetadataType;
        try {
            result = Class.forName(metadataType);
        } catch (ClassNotFoundException e1) {
            metadataType = "fr.gouv.vitam.tools.sedalib.metadata.namedtype." + simpleMetadataType;
            try {
                result = Class.forName(metadataType);
            } catch (ClassNotFoundException e2) {
                metadataType = "fr.gouv.vitam.tools.sedalib.metadata.management." + simpleMetadataType;
                try {
                    result = Class.forName(metadataType);
                } catch (ClassNotFoundException e3) {
                    metadataType = "fr.gouv.vitam.tools.sedalib.metadata.data." + simpleMetadataType;
                    try {
                        result = Class.forName(metadataType);
                    } catch (ClassNotFoundException e4) {
                        metadataType = "fr.gouv.vitam.tools.sedalib.metadata." + simpleMetadataType;
                        try {
                            result = Class.forName(metadataType);
                        } catch (ClassNotFoundException e5) {
                            metadataType = "fr.gouv.vitam.tools.sedalib.metadata.compacted." + simpleMetadataType;
                            try {
                                result = Class.forName(metadataType);
                            } catch (ClassNotFoundException e6) {
                                throw new SEDALibException(
                                    "Le type de métadonnée [" + simpleMetadataType + "] n'est pas connu");
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Create sample SEDA metadata.
     *
     * @param metadataType the SEDAMetadata type used for this editedObject
     * @param elementName        the XML tag in SEDA
     * @param minimal            the minimal flag, if true subfields are selected and values are empty, if false all subfields are added and values are default values
     * @return the seda editedObject
     * @throws SEDALibException the seda lib exception
     */
    static public SEDAMetadata createSEDAMetadataSample(String metadataType, String elementName, boolean minimal) throws SEDALibException {
        SEDAMetadata result;
        try {
            String objectEditorType = "fr.gouv.vitam.tools.resip.sedaobjecteditor." + metadataType + "Editor";
            Class<?> objectEditorClass = Class.forName(objectEditorType);
            try {
                result = (SEDAMetadata) objectEditorClass.getMethod("getSEDAMetadataSample", String.class, boolean.class).invoke(null, elementName,minimal);
            } catch (IllegalAccessException | NoSuchMethodException e) {
                throw new SEDALibException("La création d'un exemple de la métadonnée de type [" + metadataType + "] n'est pas possible", e);
            } catch (InvocationTargetException te) {
                throw new SEDALibException("La création d'un exemple de la métadonnée [" + metadataType + "] a généré une erreur", te.getTargetException());
            }
        } catch (ClassNotFoundException e1) {
            Class objectClass = getMetadataClass(metadataType);
            if (!ComplexListType.class.isAssignableFrom(objectClass))
                throw new SEDALibException("L'éditeur de métadonnée [" + metadataType + "] n'existe pas");
            result = ComplexListTypeEditor.getSEDAMetadataSample(objectClass, elementName, minimal);
        }
        return result;
    }

    /**
     * Create SEDA object editor for a SEDA metadata.
     *
     * @param metadata the SEDAMetadata to edit
     * @param father   the father, the SEDAObjectEditor of the object (SEDAMetadata or high-level objects) containing editedObject
     * @return the SEDA object editor
     * @throws SEDALibException the seda lib exception
     */
    static public SEDAObjectEditor createSEDAObjectEditor(SEDAMetadata metadata, SEDAObjectEditor father) throws SEDALibException {
        SEDAObjectEditor result;
        if (metadata instanceof RuleType) {
            result = new RuleTypeEditor(metadata, father);
        } else if (metadata instanceof ComplexListType) {
            result = new ComplexListTypeEditor(metadata, father);
        } else {
            String objectEditorType =
                "fr.gouv.vitam.tools.resip.sedaobjecteditor." + metadata.getClass().getSimpleName() + "Editor";
            Class<?> objectEditorClass;
            try {
                objectEditorClass = Class.forName(objectEditorType);
            } catch (ClassNotFoundException e) {
                throw new SEDALibException("La métadonnée de type [" + metadata.getClass().getSimpleName() + "] n'a pas d'éditeur", e);
            }
            try {
                result = (SEDAObjectEditor) objectEditorClass.getConstructor(SEDAMetadata.class, SEDAObjectEditor.class).newInstance(metadata, father);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                throw new SEDALibException("La création d'un éditeur de la métadonnée de type [" + objectEditorType + "] n'est pas possible", e);
            } catch (InvocationTargetException te) {
                throw new SEDALibException("La création d'un éditeur de la métadonnée [" + metadata.toString() + "] a généré une erreur", te.getTargetException());
            }
        }
        return result;
    }

    /**
     * Create SEDA object editor.
     *
     * @param metadataType the SEDAMetadata type used for this editedObject
     * @param elementName  the XML tag in SEDA
     * @param minimal      the minimal flag, if true subfields are selected and values are empty, if false all subfields are added and values are default values
     * @param father       the father, the SEDAObjectEditor of the object (SEDAMetadata or high-level objects) containing editedObject
     * @return the SEDA object editor
     * @throws SEDALibException the seda lib exception
     */
    static public SEDAObjectEditor createSEDAObjectEditor(String metadataType, String elementName, boolean minimal, SEDAObjectEditor father) throws
            SEDALibException {
        return createSEDAObjectEditor(createSEDAMetadataSample(metadataType, elementName, minimal), father);
    }

    /**
     * Gets empty same type SEDA metadata.
     *
     * @param sedaMetadata the SEDAMetadata
     * @return a SEDAMetadata of same type but empty
     * @throws SEDALibException the seda lib exception
     */
    static public SEDAMetadata getEmptySameSEDAMetadata(SEDAMetadata sedaMetadata) throws SEDALibException {
        Constructor<?> cons;
        try {
            if (sedaMetadata.getClass().getName().contains("namedtype")) {
                cons = sedaMetadata.getClass().getConstructor(String.class);
                sedaMetadata = (SEDAMetadata) cons.newInstance(sedaMetadata.getXmlElementName());
            }
            else {
                cons = sedaMetadata.getClass().getConstructor();
                sedaMetadata = (SEDAMetadata) cons.newInstance();
            }
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException e) {
            throw new SEDALibException("Pas de constructeur vide pour la métadonnée de type [" + sedaMetadata.getClass() + "]", e);
        } catch (InvocationTargetException e) {
            throw new SEDALibException("Erreur durant la création de la métadonnée vide de type [" + sedaMetadata.getClass() + "]", e.getTargetException());
        }
        return sedaMetadata;
    }

    /**
     * Gets SEDA metadata sample.
     *
     * @param elementName the XML tag in SEDA
     * @param minimal     the minimal flag, if true subfields are selected and values are empty, if false all subfields are added and values are default values
     * @return the SEDA metadata sample
     * @throws SEDALibException the seda lib exception
     */
    static public SEDAMetadata getSEDAMetadataSample(String elementName, boolean minimal) throws SEDALibException {
        throw new SEDALibException("Métadonnée non implémentée pour [" + elementName + "]");
    }

    /**
     * Gets SEDA metadata information.
     *
     * @param sedaMetadata the SEDA metadata
     * @return the SEDA metadata information
     */
    static public String getSEDAMetadataInformation(SEDAMetadata sedaMetadata) {
        return SEDAObjectEditorConstants.sedaMetadataInformationMap.get(sedaMetadata.getXmlElementName());
    }

    //
    // SEDAObjectEditor methods
    //

    /**
     * Instantiates a new SEDA object editor.
     *
     * @param editedObject the editedObject
     * @param father   the father
     */
    public SEDAObjectEditor(Object editedObject, SEDAObjectEditor father) {
        this.editedObject = editedObject;
        this.father = father;
        this.sedaObjectEditorPanel = null;
    }

    /**
     * Get edited object XML tag in SEDA.
     *
     * @return the name string
     */
    public String getTag(){
        // standard case made default
        if (editedObject instanceof SEDAMetadata)
            return ((SEDAMetadata) editedObject).getXmlElementName();
        return "";
    }

    /**
     * Get edited object name.
     *
     * @return the name string
     */
    public String getName(){
        return translateTag(getTag());
    }

    /**
     * Extract edited object.
     *
     * @return the edited object
     * @throws SEDALibException the seda lib exception
     */
    abstract public Object extractEditedObject() throws SEDALibException;

    /**
     * Gets edited object summary.
     *
     * @return the summary string
     * @throws SEDALibException the seda lib exception
     */
    abstract public String getSummary() throws SEDALibException;

    /**
     * Create SEDA object editor panel.
     *
     * @throws SEDALibException the seda lib exception
     */
    abstract public void createSEDAObjectEditorPanel() throws SEDALibException;

    /**
     * Test if the edited object can contain multiple objects with this name.
     *
     * @param objectName the editedObject name
     * @return true if it can contain multiple, false if not
     * @throws SEDALibException the seda lib exception
     */
    public boolean canContainsMultiple(String objectName) throws SEDALibException {
        return false;
    }

    /**
     * Gets SEDA object editor panel.
     *
     * @return the SEDA object editor panel
     * @throws SEDALibException the seda lib exception
     */
    public SEDAObjectEditorPanel getSEDAObjectEditorPanel() throws SEDALibException {
        if (sedaObjectEditorPanel == null)
            createSEDAObjectEditorPanel();
        return sedaObjectEditorPanel;
    }

    /**
     * Gets SEDA object editor panel top parent.
     *
     * @return the SEDA object editor panel top parent
     */
    public Container getSEDAObjectEditorPanelTopParent() {
        if (father == null)
            return sedaObjectEditorPanel.getParent();
        return father.getSEDAObjectEditorPanelTopParent();
    }

    //
    // Getters and setters
    //

    /**
     * Gets father.
     *
     * @return the father
     */
    public SEDAObjectEditor getFather() {
        return father;
    }

    /**
     * Gets the edited object.
     *
     * @return the edited object
     */
    public Object getEditedObject() {
        return editedObject;
    }

    /**
     * Set the edited object.
      */
    public void setEditedObject(Object metadata) {
        this.editedObject =metadata;
    }
}

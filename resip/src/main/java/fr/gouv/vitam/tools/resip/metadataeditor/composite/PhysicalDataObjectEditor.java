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
package fr.gouv.vitam.tools.resip.metadataeditor.composite;

import fr.gouv.vitam.tools.resip.metadataeditor.MetadataEditor;
import fr.gouv.vitam.tools.resip.metadataeditor.components.structuredcomponents.CompositeEditorPanel;
import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.core.PhysicalDataObject;
import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.data.FileInfo;
import fr.gouv.vitam.tools.sedalib.metadata.data.FormatIdentification;
import fr.gouv.vitam.tools.sedalib.metadata.data.Metadata;
import fr.gouv.vitam.tools.sedalib.metadata.data.PhysicalDimensions;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.AnyXMLType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.DigestType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.IntegerType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.StringType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.apache.commons.lang3.tuple.Pair;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static fr.gouv.vitam.tools.resip.metadataeditor.MetadataEditorConstants.translateMetadataName;

/**
 * The PhysicalDataObject metadata editor class.
 */
public class PhysicalDataObjectEditor extends CompositeEditor {

    /**
     * Instantiates a new PhysicalDataObject editor.
     *
     * @param metadata the PhysicalDataObject metadata
     * @param father   the father
     */
    public PhysicalDataObjectEditor(PhysicalDataObject metadata, MetadataEditor father) {
        super(metadata, father);
    }

    private PhysicalDataObject getPhysicalDataObjectMetadata() {
        return (PhysicalDataObject) metadata;
    }

    @Override
    public String getName(){
        return translateMetadataName("PhysicalDataObject")+" - "+(metadata==null? translateMetadataName("Unknown"):getPhysicalDataObjectMetadata().getInDataObjectPackageId());
    }

    @Override
    public PhysicalDataObject extractEditedObject() throws SEDALibException {
        PhysicalDataObject tmpPdo=new PhysicalDataObject();
        for (MetadataEditor metadataEditor : metadataEditorList) {
            SEDAMetadata subMetadata=(SEDAMetadata)metadataEditor.extractEditedObject();
            switch (subMetadata.getXmlElementName()){
                case "DataObjectVersion":
                    tmpPdo.dataObjectVersion=(StringType)subMetadata;
                    break;
                case "PhysicalId":
                    tmpPdo.physicalId=(StringType) subMetadata;
                    break;
                case "PhysicalDimensions":
                    tmpPdo.physicalDimensions=(PhysicalDimensions) subMetadata;
                    break;
                case "AnyXMLType":
                    tmpPdo.otherDimensionsAbstractXml.add((AnyXMLType)subMetadata);
                    break;
            }
        }
        getPhysicalDataObjectMetadata().dataObjectVersion=tmpPdo.dataObjectVersion;
        getPhysicalDataObjectMetadata().physicalId=tmpPdo.physicalId;
        getPhysicalDataObjectMetadata().physicalDimensions=tmpPdo.physicalDimensions;
        getPhysicalDataObjectMetadata().otherDimensionsAbstractXml=tmpPdo.otherDimensionsAbstractXml;

        return getPhysicalDataObjectMetadata();
    }

    @Override
    public String getSummary() throws SEDALibException {
        List<String> summaryList = new ArrayList<String>(metadataEditorList.size());
        String tmp;

        if (getPhysicalDataObjectMetadata().dataObjectVersion!=null)
            summaryList.add(getPhysicalDataObjectMetadata().dataObjectVersion.getValue());
        else
            summaryList.add(translateMetadataName("Unknown"));

        if (getPhysicalDataObjectMetadata().physicalId!=null) {
            tmp=getPhysicalDataObjectMetadata().physicalId.getValue();
            if (tmp==null)
                tmp= translateMetadataName("Unknown");
        }
        else
            tmp= translateMetadataName("Unknown");
        summaryList.add(tmp);

        return String.join(", ", summaryList);
    }

    @Override
    public void createMetadataEditorPanel() throws SEDALibException {
        this.metadataEditorList = new ArrayList<MetadataEditor>();

        this.metadataEditorPanel = new CompositeEditorPanel(this);
        if (getPhysicalDataObjectMetadata().dataObjectVersion!=null) {
            MetadataEditor metadataEditor = MetadataEditor.createMetadataEditor(getPhysicalDataObjectMetadata().dataObjectVersion, this);
            metadataEditorList.add(metadataEditor);
            ((CompositeEditorPanel) metadataEditorPanel).addMetadataEditorPanel(0,metadataEditor.getMetadataEditorPanel());
        }
        if (getPhysicalDataObjectMetadata().physicalId!=null) {
            MetadataEditor metadataEditor = MetadataEditor.createMetadataEditor(getPhysicalDataObjectMetadata().physicalId, this);
            metadataEditorList.add(metadataEditor);
            ((CompositeEditorPanel) metadataEditorPanel).addMetadataEditorPanel(1,metadataEditor.getMetadataEditorPanel());
        }
        if (getPhysicalDataObjectMetadata().physicalDimensions!=null) {
            MetadataEditor metadataEditor = MetadataEditor.createMetadataEditor(getPhysicalDataObjectMetadata().physicalDimensions, this);
            metadataEditorList.add(metadataEditor);
            ((CompositeEditorPanel) metadataEditorPanel).addMetadataEditorPanel(2,metadataEditor.getMetadataEditorPanel());
        }
        int i=3;
        for (AnyXMLType sub:getPhysicalDataObjectMetadata().otherDimensionsAbstractXml){
            MetadataEditor metadataEditor = MetadataEditor.createMetadataEditor(sub, this);
            metadataEditorList.add(metadataEditor);
            ((CompositeEditorPanel) metadataEditorPanel).addMetadataEditorPanel(i++,metadataEditor.getMetadataEditorPanel());
        }
    }

    static private void openButton(Path path){
        System.out.println(path);
    }

    @Override
    public List<Pair<String,String>> getExtensionList() throws SEDALibException {
        List<Pair<String, String>> extensionList = new ArrayList<Pair<String, String>>(Arrays.asList(
                Pair.of("DataObjectVersion", translateMetadataName("DataObjectVersion")),
                Pair.of("PhysicalId", translateMetadataName("PhysicalId")),
                Pair.of("PhysicalDimensions", translateMetadataName("PhysicalDimensions"))));

        for (MetadataEditor me : metadataEditorList) {
            String name=me.getName();
            extensionList.remove(Pair.of(name, translateMetadataName(name)));
        }

        extensionList.add(Pair.of("AnyXMLType", translateMetadataName("AnyXMLType")));
        return extensionList;
    }

    @Override
    public void addChild(String metadataName) throws SEDALibException {
        SEDAMetadata sedaMetadata=null;
        int insertionIndex=0;
        switch(metadataName){
            case "DataObjectVersion":
                sedaMetadata = createSEDAMetadataSample("StringType", "DataObjectVersion", true);
                insertionIndex=0;
                break;
            case "PhysicalId":
                sedaMetadata = createSEDAMetadataSample("StringType", "PhysicalId", true);
                insertionIndex=1;
                break;
            case "PhysicalDimensions":
                sedaMetadata = createSEDAMetadataSample("PhysicalDimensions", "PhysicalDimensions", true);
                insertionIndex=2;
                break;
            case "AnyXMLType":
                sedaMetadata = createSEDAMetadataSample("AnyXMLType", "AnyXMLType", true);
                insertionIndex= Integer.MAX_VALUE;
                break;
        }

        if (sedaMetadata==null)
            throw new SEDALibException("La métadonnée ["+metadataName+"] n'existe pas dans un PhysicalDataObject");
        MetadataEditor addedMetadataEditor = createMetadataEditor(sedaMetadata, this);
        if (insertionIndex==Integer.MAX_VALUE)
            metadataEditorList.add(addedMetadataEditor);
        else
            metadataEditorList.add(insertionIndex, addedMetadataEditor);
        ((CompositeEditorPanel) metadataEditorPanel).addMetadataEditorPanel(insertionIndex,addedMetadataEditor.getMetadataEditorPanel());
    }

    @Override
    public boolean canContainsMultiple(String metadataName) throws SEDALibException {
        return metadataName.equals("AnyXMLType");
    }
}

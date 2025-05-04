package fr.gouv.vitam.tools.resip.sedaobjecteditor.components.viewers;

import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.core.DataObjectGroup;
import fr.gouv.vitam.tools.sedalib.metadata.data.FileInfo;
import fr.gouv.vitam.tools.sedalib.metadata.data.FormatIdentification;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.IntegerType;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * The class DuplicatesTableModel.
 * <p>
 * Class for duplicates table.
 */
public class DuplicatesTableModel extends AbstractTableModel {

    private final String[] entetes = {"Index", "Nb AU", "Nb DOG", "Noms", "Tailles", "Formats", "MimeTypes"};
    private LinkedHashMap<String, List<DataObjectGroup>> dogByDogDigestMap;
    private HashMap<String, List<ArchiveUnit>> auByDogDigestMap;
    private String[] lotList;

    /**
     * Gets DOG by DOG digest map.
     *
     * @return the dog by digest map
     */
    public HashMap<String, List<DataObjectGroup>> getDogByDogDigestMap() {
        return dogByDogDigestMap;
    }

    /**
     * Sets data, DOG and AU by DOG digest maps.
     *
     * @param dogByDigestMap the dog by digest map
     * @param auByDigestMap  the au by digest map
     */
    public void setData(LinkedHashMap<String, List<DataObjectGroup>> dogByDigestMap,
                        HashMap<String, List<ArchiveUnit>> auByDigestMap) {
        this.dogByDogDigestMap = dogByDigestMap;
        this.auByDogDigestMap = auByDigestMap;
        if (dogByDigestMap != null)
            this.lotList = dogByDigestMap.keySet().toArray(new String[0]);

    }

    /**
     * Change row dog list.
     *
     * @param dogList the dog list
     * @param row     the row
     */
    public void changeRowDogList(List<DataObjectGroup> dogList, int row){
        dogByDogDigestMap.put(lotList[row],dogList);
    }

    @Override
    public int getColumnCount() {
        return entetes.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return entetes[columnIndex];
    }

    @Override
    public Class getColumnClass(int col) {
        if (col < 3)
            return Integer.class;
        else
            return String.class;
    }

    @Override
    public int getRowCount() {
        if (dogByDogDigestMap == null) return 0;
        return dogByDogDigestMap.size();
    }

    @Override
    public Object getValueAt(int arg0, int arg1) {
        if (dogByDogDigestMap == null) return null;
        if (arg0 >= dogByDogDigestMap.size())
            throw new IllegalArgumentException();
        List<DataObjectGroup> dogList = dogByDogDigestMap.get(lotList[arg0]);
        switch (arg1) {
            case 0:
                return arg0;
            case 1:
                return auByDogDigestMap.get(lotList[arg0]).size();
            case 2:
                return dogList.size();
            case 3:
                List<String> names = new ArrayList<String>();
                for (BinaryDataObject bdo : dogList.get(0).getBinaryDataObjectList()) {
                    FileInfo fi=bdo.getMetadataFileInfo();
                    names.add((fi==null?null:fi.getSimpleMetadata("Filename")));
                }
                return String.join(", ", names);
            case 4:
                List<String> sizes = new ArrayList<String>();
                for (BinaryDataObject bdo : dogList.get(0).getBinaryDataObjectList()) {
                    IntegerType s=bdo.getMetadataSize();
                    sizes.add(String.format("%,d", (s==null?0:s.getValue())));
                }
                return String.join(", ", sizes);
            case 5:
                List<String> formats = new ArrayList<String>();
                for (BinaryDataObject bdo : dogList.get(0).getBinaryDataObjectList()) {
                    FormatIdentification fi=bdo.getMetadataFormatIdentification();
                    formats.add((fi==null?null:fi.getSimpleMetadata("FormatId")));
                }
                return String.join(", ", formats);
            case 6:
                List<String> types = new ArrayList<String>();
                for (BinaryDataObject bdo : dogList.get(0).getBinaryDataObjectList()) {
                    FormatIdentification fi=bdo.getMetadataFormatIdentification();
                    types.add((fi==null?null:fi.getSimpleMetadata("MimeType")));
                }
                return String.join(", ", types);
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * Gets row dog list.
     *
     * @param row the row
     * @return the row dog list
     */
    public List<DataObjectGroup> getRowDogList(int row) {
        if (dogByDogDigestMap != null)
            return dogByDogDigestMap.get(lotList[row]);
        else
            return null;
    }

    /**
     * Gets row au list.
     *
     * @param row the row
     * @return the row au list
     */
    public List<ArchiveUnit> getRowAuList(int row) {
        if (auByDogDigestMap != null)
            return auByDogDigestMap.get(lotList[row]);
        else
            return null;
    }
}
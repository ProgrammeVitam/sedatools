package fr.gouv.vitam.tools.resip.viewer;

import fr.gouv.vitam.tools.resip.data.StatisticData;
import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.core.DataObjectGroup;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class DuplicatesTableModel extends AbstractTableModel {

    private final String[] entetes = { "Index", "Nombre", "Noms", "Tailles","Formats","MimeTypes"};
    private LinkedHashMap<String, List<DataObjectGroup>> dogByDigestMap;
    private String[] lotList;

    public HashMap<String, List<DataObjectGroup>> getDogByDigestMap() {
        return dogByDigestMap;
    }

    public void setDogByDigestMap(LinkedHashMap<String, List<DataObjectGroup>> dogByDigestMap) {
        this.dogByDigestMap = dogByDigestMap;
        this.lotList=dogByDigestMap.keySet().toArray(new String[0]);

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
        if (col<2)
            return Integer.class;
        else
            return String.class;
    }

    @Override
    public int getRowCount() {
        if (dogByDigestMap == null) return 0;
        return dogByDigestMap.size();
    }

    @Override
    public Object getValueAt(int arg0, int arg1) {
        if (dogByDigestMap == null) return null;
        if (arg0 >= dogByDigestMap.size())
            throw new IllegalArgumentException();
        List<DataObjectGroup> dogList=dogByDigestMap.get(lotList[arg0]);
        switch (arg1) {
            case 0:
                return arg0;
            case 1:
                return dogList.size();
            case 2:
                List<String> names=new ArrayList<String>();
                for (BinaryDataObject bdo:dogList.get(0).getBinaryDataObjectList()) {
                    names.add(bdo.fileInfo.filename);
                }
                return String.join(", ",names);
            case 3:
                List<String> sizes=new ArrayList<String>();
                for (BinaryDataObject bdo:dogList.get(0).getBinaryDataObjectList()) {
                    sizes.add(String.format("%,d",bdo.size));
                }
                return String.join(", ",sizes);
            case 4:
                List<String> formats=new ArrayList<String>();
                for (BinaryDataObject bdo:dogList.get(0).getBinaryDataObjectList()) {
                    formats.add(bdo.formatIdentification.formatId);
                }
                return String.join(", ",formats);
            case 5:
                List<String> types=new ArrayList<String>();
                for (BinaryDataObject bdo:dogList.get(0).getBinaryDataObjectList()) {
                    types.add(bdo.formatIdentification.mimeType);
                }
                return String.join(", ",types);
            default:
                throw new IllegalArgumentException();
        }
    }

    public List<DataObjectGroup> getRowDogList(int row){
        return dogByDigestMap.get(lotList[row]);
    }
}
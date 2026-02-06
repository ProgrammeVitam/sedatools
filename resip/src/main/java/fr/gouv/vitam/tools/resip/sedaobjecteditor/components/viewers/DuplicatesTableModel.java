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
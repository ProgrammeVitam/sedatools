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
package fr.gouv.vitam.tools.sedalib.core;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class DataObjectRefList.
 * <p>
 * Class for managing list of contained DataObjects either by id or by object
 * reference. Those two modes are useful at different moments of process (import
 * -&lt; ID, DataObjectPackage fusion -&lt; objects...).
 * <p>
 * It keeps inner two lists, one of ids and one of DataObjects objects. At a
 * given moment only one list is defined the other one is null. So if you need
 * for example to change DataObjects ID, you have before to force all
 * DataObjectRefList in object reference mode by reading the object references list.
 */
public class DataObjectRefList extends DataObjectPackageElement {

    /**
     * The DataObject list.
     */
    @JsonIgnore
    private List<DataObject> dataObjectList;

    /**
     * The inDataPackageObjectId list.
     */
    private ArrayList<String> inDataObjectPackageIdList;

    /**
     * Instantiates a new DataObject reference list, used by deserialization.
     */
    public DataObjectRefList() {
        this(null);
    }

    /**
     * Instantiates a new DataObject reference list.
     *
     * @param dataObjectPackage the DataObjectPackage
     */
    public DataObjectRefList(DataObjectPackage dataObjectPackage) {
        super(dataObjectPackage);
        inDataObjectPackageIdList = null;
    }

    /**
     * Gets the inDataPackageObjectId list.
     *
     * @return the inDataPackageObjectId list
     */
    public ArrayList<String> getInDataObjectPackageIdList() {
        if (inDataObjectPackageIdList != null)
            return inDataObjectPackageIdList;
        if (dataObjectList == null)
            inDataObjectPackageIdList = new ArrayList<>(0);
        else {
            inDataObjectPackageIdList = new ArrayList<>(dataObjectList.size());
            for (DataObject dataObject : dataObjectList)
                inDataObjectPackageIdList.add(dataObject.getInDataObjectPackageId());
        }
        dataObjectList = null;
        return inDataObjectPackageIdList;
    }

    /**
     * Sets the inDataPackageObjectId list.
     *
     * @param inDataObjectPackageIdList the new inDataPackageObjectId list
     */
    public void setInDataObjectPackageIdList(ArrayList<String> inDataObjectPackageIdList) {
        this.inDataObjectPackageIdList = inDataObjectPackageIdList;
        this.dataObjectList = null;
    }

    /**
     * Gets the DataObject list.
     *
     * @return the DataObject list
     */
    public List<DataObject> getDataObjectList() {
        if (dataObjectList != null)
            return dataObjectList;
        if (inDataObjectPackageIdList == null)
            dataObjectList = new ArrayList<>(0);
        else {
            dataObjectList = new ArrayList<>(inDataObjectPackageIdList.size());
            for (String inSipId : inDataObjectPackageIdList)
                dataObjectList.add(getDataObjectPackage().getDataObjectById(inSipId));
        }
        inDataObjectPackageIdList = null;
        return dataObjectList;
    }

    /**
     * Sets the DataObject list.
     *
     * @param dataObjectList the new DataObject list
     */
    public void setDataObjectList(List<DataObject> dataObjectList) {
        this.dataObjectList = dataObjectList;
        this.inDataObjectPackageIdList = null;
    }

    /**
     * Adds the DataObject by object reference.
     *
     * @param zdo the DataObject
     */
    public void add(DataObject zdo) {
        getDataObjectList().add(zdo);
    }

    /**
     * Adds the DataObject by inDataPackageObjectId.
     *
     * @param inDataObjectPackageId the inDataPackageObjectId
     */
    public void addById(String inDataObjectPackageId) {
        getInDataObjectPackageIdList().add(inDataObjectPackageId);
    }

    /**
     * Removes the DataObject by object reference.
     *
     * @param zdo the DataObject
     */
    public void remove(DataObject zdo) {
        getDataObjectList().remove(zdo);
    }

    /**
     * Removes the DataObject by inDataPackageObjectId.
     *
     * @param inDataObjectPackageId the inDataPackageObjectId
     */
    public void removeById(String inDataObjectPackageId) {
        getInDataObjectPackageIdList().remove(inDataObjectPackageId);
    }

    /**
     * Gets the contained DataObject count.
     *
     * @return the count
     */
    @JsonIgnore
    public int getCount() {
        if (inDataObjectPackageIdList != null)
            return inDataObjectPackageIdList.size();
        else if (dataObjectList != null)
            return dataObjectList.size();
        return 0;
    }

    /**
     * Gets the uniq DataObjectGroup containing all DataObjects if it exists, null if not
     *
     * @return the uniq DataObjectGroup or null
     */
    @JsonIgnore
    public DataObjectGroup getNormalizedDataObjectGroup() {
        getDataObjectList();
        if (dataObjectList.size() != 1)
            return null;
        if (dataObjectList.get(0) instanceof DataObjectGroup)
            return (DataObjectGroup) dataObjectList.get(0);
        return null;
    }
}

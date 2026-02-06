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
package fr.gouv.vitam.tools.sedalib.core;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class ArchiveUnitList.
 * <p>
 * Class for managing list of child ArchiveUnits either by id or by object
 * reference. Those two modes are useful at different moments of process (import
 * -&lt; ID, ArchiveTransfer fusion -&lt; objects...).
 * <p>
 * It keeps inner two lists, one of ids and one of ArchiveUnit objects. At a
 * given moment only one list is defined the other one is null. So if you need
 * for example to change ArchiveUnits ID, you have before to force all
 * ArchiveUnitRefList in object reference mode by reading the object references list.
 */
public class ArchiveUnitRefList extends DataObjectPackageElement {

    /**
     * The ArchiveUnit list.
     */
    @JsonIgnore
    private List<ArchiveUnit> archiveUnitList;

    /**
     * The inDataPackageObjectId list.
     */
    private List<String> inDataObjectPackageIdList;

    /**
     * Instantiates a new ArchiveUnit references list.
     */
    public ArchiveUnitRefList() {}

    /**
     * Instantiates a new ArchiveUnit references list.
     *
     * @param dataObjectPackage the DataObjectPackage containing the ArchiveUnits in list
     */
    public ArchiveUnitRefList(DataObjectPackage dataObjectPackage) {
        super(dataObjectPackage);
        archiveUnitList = null;
        inDataObjectPackageIdList = null;
    }

    /**
     * Gets the inDataPackageObjectId list.
     *
     * @return the inDataPackageObjectId list
     */
    public List<String> getInDataObjectPackageIdList() {
        if (inDataObjectPackageIdList != null) return inDataObjectPackageIdList;
        if (archiveUnitList == null) inDataObjectPackageIdList = new ArrayList<>(0);
        else {
            inDataObjectPackageIdList = new ArrayList<>(archiveUnitList.size());
            for (ArchiveUnit au : archiveUnitList) inDataObjectPackageIdList.add(au.inDataPackageObjectId);
        }
        archiveUnitList = null;
        return inDataObjectPackageIdList;
    }

    /**
     * Sets the inDataPackageObjectId list.
     *
     * @param inDataObjectPackageIdList the new inDataPackageObjectId list
     */
    public void setInDataObjectPackageIdList(List<String> inDataObjectPackageIdList) {
        this.inDataObjectPackageIdList = inDataObjectPackageIdList;
        archiveUnitList = null;
    }

    /**
     * Gets the ArchiveUnit objects list.
     *
     * @return the ArchiveUnit list
     */
    @JsonIgnore
    public List<ArchiveUnit> getArchiveUnitList() {
        if (archiveUnitList != null) return archiveUnitList;
        if (inDataObjectPackageIdList == null) archiveUnitList = new ArrayList<>(0);
        else {
            archiveUnitList = new ArrayList<>(inDataObjectPackageIdList.size());
            for (String inSipId : inDataObjectPackageIdList) archiveUnitList.add(
                getDataObjectPackage().getArchiveUnitById(inSipId)
            );
        }
        inDataObjectPackageIdList = null;
        return archiveUnitList;
    }

    /**
     * Sets the ArchiveUnit objects list.
     *
     * @param archiveUnitList the new ArchiveUnit list
     */
    @JsonIgnore
    public void setArchiveUnitList(List<ArchiveUnit> archiveUnitList) {
        this.archiveUnitList = archiveUnitList;
        inDataObjectPackageIdList = null;
    }

    /**
     * Adds the ArchiveUnit by object reference.
     *
     * @param archiveUnit the ArchiveUnit
     */
    public void add(ArchiveUnit archiveUnit) {
        getArchiveUnitList().add(archiveUnit);
    }

    /**
     * Adds the ArchiveUnit by inDataPackageObjectId.
     *
     * @param inDataObjectPackageId the inDataPackageObjectId
     */
    public void addById(String inDataObjectPackageId) {
        getInDataObjectPackageIdList().add(inDataObjectPackageId);
    }

    /**
     * Removes the ArchiveUnit by object reference.
     *
     * @param archiveUnit the ArchiveUnit
     */
    public void remove(ArchiveUnit archiveUnit) {
        getArchiveUnitList().remove(archiveUnit);
    }

    /**
     * Replaces the ArchiveUnit by another.
     *
     * @param originArchiveUnit the origin ArchiveUnit
     * @param newArchiveUnit     the new ArchiveUnit
     */
    public void replace(ArchiveUnit originArchiveUnit, ArchiveUnit newArchiveUnit) {
        List<ArchiveUnit> listAu = getArchiveUnitList();
        for (int i = 0; i < listAu.size(); i++) {
            if (listAu.get(i) == originArchiveUnit) {
                listAu.set(i, newArchiveUnit);
                return;
            }
        }
    }

    /**
     * Removes the ArchiveUnit by inDataPackageObjectId.
     *
     * @param inDataObjectPackageId inDataPackageObjectId
     */
    public void removeById(String inDataObjectPackageId) {
        getInDataObjectPackageIdList().remove(inDataObjectPackageId);
    }

    /**
     * Gets the children count.
     *
     * @return the count
     */
    @JsonIgnore
    public int getCount() {
        if (inDataObjectPackageIdList != null) return inDataObjectPackageIdList.size();
        else if (archiveUnitList != null) return archiveUnitList.size();
        return 0;
    }
}

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

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The Class DataObjectPackageIdElement.
 * <p>
 * Class for SEDA elements objects in DataObjectPackage with an
 * inDataPackageObjectId (ArchiveUnit, DataObjectGroup...). This Id is used in XML
 * as SEDA elements attribute.
 * <p>These elements also can have on disk representation kept in onDiskPath if any.
 */
public class DataObjectPackageIdElement extends DataObjectPackageElement {

    /**
     * The id in DataObjectPackage.
     */
    protected String inDataPackageObjectId;

    /**
     * The on disk path.
     */
    protected Path onDiskPath;

    /**
     * Instantiates a new DataObjectPackage id element.
     */
    public DataObjectPackageIdElement() {
        this(null);
    }

    /**
     * Instantiates a new DataObjectPackage id element.
     *
     * @param dataObjectPackage the DataObjectPackage
     */
    public DataObjectPackageIdElement(DataObjectPackage dataObjectPackage) {
        super(dataObjectPackage);
        inDataPackageObjectId = null;
    }

    /**
     * Gets the inDataPackageObjectId.
     *
     * @return the inDataPackageObjectId
     */
    public String getInDataObjectPackageId() {
        return inDataPackageObjectId;
    }

    /**
     * Sets the inDataPackageObjectId.
     *
     * @param inDataObjectPackageId the new inDataPackageObjectId
     */
    public void setInDataObjectPackageId(String inDataObjectPackageId) {
        this.inDataPackageObjectId = inDataObjectPackageId;
    }

    /**
     * Gets the disk path where the DataObjectPackageIdElement is described or null.
     *
     * @return the disk path
     */
    @JsonIgnore
    public Path getOnDiskPath() {
        return onDiskPath;
    }

    /**
     * Sets the disk Path where the DataObjectPackageIdElement is described.
     *
     * @param onDiskPath the new on disk path
     */
    @JsonIgnore
    public void setOnDiskPath(Path onDiskPath) {
        if (onDiskPath == null)
            this.onDiskPath = null;
        else
            this.onDiskPath = onDiskPath.toAbsolutePath().normalize();
    }

    /**
     * Gets the onDiskPath to string.
     *
     * @return the onDiskPath in String form
     */
    @JsonGetter("onDiskPath")
    public String getOnDiskPathToString() {
        if (onDiskPath == null)
            return null;
        else
            return onDiskPath.toString();
    }

    /**
     * Sets the onDiskPath from string.
     *
     * @param onDiskPathString the new onDiskPath in String form
     */
    @JsonSetter("onDiskPath")
    public void setOnDiskPathFromString(String onDiskPathString) {
        if (onDiskPathString == null)
            this.onDiskPath = null;
        else
            this.onDiskPath = Paths.get(onDiskPathString).toAbsolutePath().normalize();
    }


}

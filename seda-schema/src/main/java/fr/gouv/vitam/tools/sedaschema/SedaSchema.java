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
package fr.gouv.vitam.tools.sedaschema;

import fr.gouv.vitam.tools.sedaschema.model.ComplexTypeDef;
import fr.gouv.vitam.tools.sedaschema.model.ElementDef;
import fr.gouv.vitam.tools.sedaschema.model.TypeDef;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * A loaded XSD schema: the global elements (document roots) and the named types, from which the
 * whole content model is reachable.
 * <p>
 * Use {@link #of(SedaVersion)} for the bundled official SEDA schemas, {@link #of(SedaVersion,
 * SedaFlavor)} to get the Vitam flavour instead, {@link #load(URL)} for any other XSD (a future
 * SEDA version, a restricted variant...). Instances are immutable and thread safe once built.
 */
public final class SedaSchema {

    private static final Map<SedaFlavor, Map<SedaVersion, SedaSchema>> BUILTIN = new EnumMap<>(SedaFlavor.class);

    private final String targetNamespace;
    private final SedaVersion version;
    private final SedaFlavor flavor;
    private final Map<String, ElementDef> globalElements;
    private final Map<String, TypeDef> namedTypes;

    SedaSchema(
        String targetNamespace,
        SedaVersion version,
        SedaFlavor flavor,
        Map<String, ElementDef> globalElements,
        Map<String, TypeDef> namedTypes
    ) {
        this.targetNamespace = targetNamespace;
        this.version = version;
        this.flavor = flavor;
        this.globalElements = Collections.unmodifiableMap(globalElements);
        this.namedTypes = Collections.unmodifiableMap(namedTypes);
    }

    /**
     * @param version a bundled SEDA version
     * @return the official schema of this version, loaded once and cached
     */
    public static SedaSchema of(SedaVersion version) throws SedaSchemaException {
        return of(version, SedaFlavor.OFFICIAL);
    }

    /**
     * @param version a bundled SEDA version
     * @param flavor  the official standard or the variant Vitam implements
     * @return the schema of this version and flavour, loaded once and cached
     */
    public static synchronized SedaSchema of(SedaVersion version, SedaFlavor flavor) throws SedaSchemaException {
        Map<SedaVersion, SedaSchema> byVersion = BUILTIN.computeIfAbsent(flavor, f -> new EnumMap<>(SedaVersion.class));
        SedaSchema schema = byVersion.get(version);
        if (schema == null) {
            String resource = version.getMainXsdResource(flavor);
            URL url = SedaSchema.class.getClassLoader().getResource(resource);
            if (url == null) throw new SedaSchemaException("Ressource XSD introuvable " + resource);
            schema = new SedaSchemaLoader().load(url, version, flavor);
            byVersion.put(version, schema);
        }
        return schema;
    }

    /**
     * Loads any XSD. The xml and xlink W3C schemas are resolved locally.
     *
     * @param mainXsd the main XSD URL (file, jar, classpath resource...)
     */
    public static SedaSchema load(URL mainXsd) throws SedaSchemaException {
        return new SedaSchemaLoader().load(mainXsd, null, null);
    }

    public String getTargetNamespace() {
        return targetNamespace;
    }

    /**
     * @return the bundled version this schema was loaded from, or null when loaded from a URL
     */
    public SedaVersion getVersion() {
        return version;
    }

    /**
     * @return the bundled flavour this schema was loaded from, or null when loaded from a URL
     */
    public SedaFlavor getFlavor() {
        return flavor;
    }

    /**
     * @return the global elements of the target namespace, the possible document roots
     * (ArchiveTransfer, ArchiveDeliveryRequestReply...)
     */
    public List<ElementDef> getRootElements() {
        return new ArrayList<>(globalElements.values());
    }

    /**
     * @param name a global element local name
     * @return its declaration, or null
     */
    public ElementDef getRootElement(String name) {
        return globalElements.get(name);
    }

    /**
     * @return the named types of the target namespace, by name
     */
    public Map<String, TypeDef> getNamedTypes() {
        return namedTypes;
    }

    public TypeDef getType(String name) {
        return namedTypes.get(name);
    }

    public ComplexTypeDef getComplexType(String name) {
        TypeDef t = namedTypes.get(name);
        return t instanceof ComplexTypeDef ? (ComplexTypeDef) t : null;
    }

    /**
     * Resolves an element declaration by its path from a root element, following the first
     * declaration of each name in the content models.
     *
     * @param path a slash separated path, like "ArchiveTransfer/DataObjectPackage/DescriptiveMetadata/ArchiveUnit/Content/Title"
     * @return the element declaration
     * @throws SedaSchemaException if a step of the path is not declared
     */
    public ElementDef getElementAt(String path) throws SedaSchemaException {
        String[] steps = path.split("/");
        ElementDef current = globalElements.get(steps[0]);
        if (current == null) throw new SedaSchemaException("Élément racine inconnu [" + steps[0] + "]");
        for (int i = 1; i < steps.length; i++) {
            ComplexTypeDef ct = current.getComplexType();
            ElementDef next = ct == null ? null : ct.getChildElement(steps[i]);
            if (next == null) throw new SedaSchemaException(
                "Élément [" +
                steps[i] +
                "] non déclaré dans [" +
                String.join("/", java.util.Arrays.copyOf(steps, i)) +
                "]"
            );
            current = next;
        }
        return current;
    }
}

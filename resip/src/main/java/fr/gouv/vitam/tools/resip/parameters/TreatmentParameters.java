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
package fr.gouv.vitam.tools.resip.parameters;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class CreationContext.
 */
public class TreatmentParameters {

    // general elements
    /**
     * The format by category map.
     */
    LinkedHashMap<String, List<String>> formatByCategoryMap;

    /**
     * The maximum duplicates aggregation number.
     */
    int dupMax;

    /**
     * The SEDA2 subversion.
     */
    int seda2Version;

    /**
     * Instantiates a new creation context.
     */
    public TreatmentParameters() {
        formatByCategoryMap = new LinkedHashMap<String, List<String>>();
    }


    private String canonizeCategoryName(String category){
        return StringUtils.stripAccents(category).replaceAll("[^A-Za-z0-9]","");
    }

    /**
     * Instantiates a new creation context.
     *
     * @param prefs the prefs
     */
    public TreatmentParameters(Prefs prefs) {
        String categoriesString = prefs.getPrefProperties().getProperty("treatmentParameters.categoriesList", null);
        formatByCategoryMap = new LinkedHashMap<String, List<String>>();
        if (categoriesString != null) {
            List<String> categoryList = Arrays.asList(categoriesString.split("\\|"));
            categoryList.replaceAll(String::trim);
            formatByCategoryMap = new LinkedHashMap<String, List<String>>();
            for (String category : categoryList) {
                String formatsString = prefs.getPrefProperties().getProperty("treatmentParameters.categories." + canonizeCategoryName(category), "");
                List<String> formatList = Arrays.asList(formatsString.split("\\|"));
                formatList.replaceAll(String::trim);
                formatByCategoryMap.put(category, formatList);
            }
        }
        try {
            dupMax=Integer.parseInt(prefs.getPrefProperties().getProperty("treatmentParameters.dupMax","1000"));
        }
        catch (NumberFormatException e){
            dupMax=1000;
        }
        try {
            seda2Version=Integer.parseInt(prefs.getPrefProperties().getProperty("treatmentParameters.seda2Version","1"));
        }
        catch (NumberFormatException e){
            seda2Version=1;
        }
    }

    /**
     * Put in preferences the values specific of this class.
     *
     * @param prefs the prefs
     */
    public void toPrefs(Prefs prefs) {
        prefs.getPrefProperties().setProperty("treatmentParameters.categoriesList", String.join("|",formatByCategoryMap.keySet()));
        for (Map.Entry<String,List<String>> e:formatByCategoryMap.entrySet()) {
            prefs.getPrefProperties().setProperty("treatmentParameters.categories."+canonizeCategoryName(e.getKey()),String.join("|",e.getValue()));
        }
        prefs.getPrefProperties().setProperty("treatmentParameters.dupMax", Integer.toString(dupMax));
        prefs.getPrefProperties().setProperty("treatmentParameters.seda2Version", Integer.toString(seda2Version));
    }

    /**
     * Sets the default prefs.
     */
    public void setDefaultPrefs() {
        formatByCategoryMap=new LinkedHashMap<String,List<String>>();

        formatByCategoryMap.put("Base de données (access,filemaker...)",Arrays.asList("fmt/161", "fmt/194", "fmt/275",
                "fmt/995", "fmt/1196", "x-fmt/1", "x-fmt/8", "x-fmt/9", "x-fmt/10", "x-fmt/66", "x-fmt/238", "x-fmt/239",
                "x-fmt/240", "x-fmt/241", "x-fmt/318", "x-fmt/319"));
        formatByCategoryMap.put("Chiffré",Arrays.asList("fmt/494", "fmt/754", "fmt/755"));
        formatByCategoryMap.put("Compressé (zip,tar...)",Arrays.asList("fmt/484", "x-fmt/263", "x-fmt/265", "x-fmt/266",
                "x-fmt/268"));
        formatByCategoryMap.put("Dessin (svg,odg,autocad...)",Arrays.asList("fmt/21", "fmt/22", "fmt/23", "fmt/24",
                "fmt/25", "fmt/26", "fmt/27", "fmt/28", "fmt/29", "fmt/30", "fmt/31", "fmt/32", "fmt/33", "fmt/34",
                "fmt/35", "fmt/36"));
        formatByCategoryMap.put("Exécutable",Arrays.asList("fmt/688", "fmt/689", "fmt-899", "fmt/900", "x-fmt/409",
                "x-fmt/410", "x-fmt/411"));
        formatByCategoryMap.put("HTML",Arrays.asList("fmt/96", "fmt/97", "fmt/98", "fmt/99", "fmt/100", "fmt/101",
                "fmt/102", "fmt/103", "fmt/471"));
        formatByCategoryMap.put("Image (jpg,jpg2000,tiff...)", Arrays.asList("fmt/3", "fmt/4", "fmt/11", "fmt/12",
                "fmt/13", "fmt/41", "fmt/42", "fmt/43", "fmt/44", "fmt/150", "fmt/156", "fmt/353", "fmt/463", "fmt/529",
                "fmt/645", "x-fmt/387", "x-fmt/390", "x-fmt/391", "x-fmt/392", "x-fmt/398"));
        formatByCategoryMap.put("Messagerie (mbox,pst,eml...)",Arrays.asList("fmt/278", "fmt/720", "fmt/950", "x-fmt/248",
                "x-fmt/249", "x-fmt/430"));
        formatByCategoryMap.put("Pdf",Arrays.asList("fmt/14", "fmt/15", "fmt/16", "fmt/17", "fmt/18", "fmt/19", "fmt/20",
                "fmt/95", "fmt/144", "fmt/145", "fmt/146", "fmt/147", "fmt/148", "fmt/157", "fmt/158", "fmt/276",
                "fmt/354", "fmt/476", "fmt/477", "fmt/478", "fmt/479", "fmt/480", "fmt/481", "fmt/488", "fmt/489",
                "fmt/490", "fmt/491", "fmt/492", "fmt/493", "fmt/1129"));
        formatByCategoryMap.put("Présentation (ppt,pptx,odp...)",Arrays.asList("fmt/125", "fmt/126", "fmt/138", "fmt/179",
                "fmt/181", "fmt/215", "fmt/292", "fmt/293", "x-fmt/88"));
        formatByCategoryMap.put("Son (wave,mp3...)",Arrays.asList("fmt/1", "fmt/2", "fmt/6", "fmt/132", "fmt/134",
                "fmt/141", "fmt/142", "fmt/527", "fmt/703", "fmt/704", "fmt/705", "fmt/706", "fmt/707", "fmt/708",
                "fmt/709", "fmt/710", "fmt/711"));
        formatByCategoryMap.put("Structuré (XML,json)",Arrays.asList("fmt/101", "fmt/817", "fmt/880"));
        formatByCategoryMap.put("Tableur (csv,xls,xlsx,ods...)",Arrays.asList("fmt/55", "fmt/56", "fmt/57", "fmt/59",
                "fmt/61", "fmt/62", "fmt/137", "fmt/175", "fmt/176", "fmt/177", "fmt/214", "fmt/294", "fmt/295",
                "fmt/445", "x-fmt/18"));
        formatByCategoryMap.put("Texte (doc,docx,odt...)", Arrays.asList("fmt/37", "fmt/38", "fmt/39", "fmt/40", "fmt/45",
                "fmt/50", "fmt/51", "fmt/52", "fmt/53", "fmt/136", "fmt/258", "fmt/290", "fmt/291", "fmt/412", "fmt/609",
                "fmt/754", "x-fmt/42", "x-fmt/43", "x-fmt/44", "x-fmt/64", "x-fmt/65", "x-fmt/273", "x-fmt/274",
                "x-fmt/275", "x-fmt/276", "x-fmt/393", "x-fmt/394"));
        formatByCategoryMap.put("Texte brut",Arrays.asList("x-fmt/111"));
        formatByCategoryMap.put("Video (avi,mov,mpeg,mp4...)",Arrays.asList("fmt/5", "fmt/199", "fmt/569", "fmt/640",
                "fmt/649", "fmt/797", "x-fmt/384", "x-fmt/385", "x-fmt/386"));
        formatByCategoryMap.put("Non connu",Arrays.asList("UNKNOWN"));
        formatByCategoryMap.put("Autres...",Arrays.asList("Other"));
        dupMax=1000;
        seda2Version=1;
    }

    // Getters and setters

    /**
     * Gets the format by category map.
     *
     * @return the format by category map
     */
    public LinkedHashMap<String, List<String>> getFormatByCategoryMap() {
        return formatByCategoryMap;
    }

    /**
     * Sets the format by category map.
     *
     * @param formatByCategoryMap the format by category map
     */
    public void setFormatByCategoryMap(LinkedHashMap<String, List<String>> formatByCategoryMap) {
        this.formatByCategoryMap = formatByCategoryMap;
    }

    /**
     * Gets duplicates maximum aggregation number.
     *
     * @return the dup max
     */
    public int getDupMax() {
        return dupMax;
    }

    /**
     * Sets duplicates maximum aggregation number.
     *
     * @param dupMax the dup max
     */
    public void setDupMax(int dupMax) {
        this.dupMax = dupMax;
    }

    /**
     * Gets seda version.
     *
     * @return the seda2 version
     */
    public int getSeda2Version() {
        return seda2Version;
    }

    /**
     * Sets seda2 version.
     *
     * @param sedaVersion the seda2 version
     */
    public void setSeda2Version(int sedaVersion) {
        this.seda2Version = sedaVersion;
    }


}
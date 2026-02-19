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
package fr.gouv.vitam.tools.resip.sedaobjecteditor;

import com.github.lgooddatepicker.components.DateTimePicker;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.components.structuredcomponents.SEDAObjectEditorSimplePanel;
import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.DateTimeType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.DateTimeType.DateTimeFormatType;
import fr.gouv.vitam.tools.sedalib.utils.LocalDateTimeUtil;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;

/**
 * The DateTimeType object editor class.
 */
public class DateTimeTypeEditor extends SEDAObjectEditor {

    /**
     * The editedObject edition graphic component
     */
    private final DateTimePicker valueDateTimePicker = new DateTimePicker();
    private final JLabel warningLabel = new JLabel();
    private DateTimeType lastDateTimeType;

    /**
     * Instantiates a new DateTimeType editor.
     *
     * @param metadata the DateTimeType editedObject
     * @param parent   the parent editor
     * @throws SEDALibException if not a DateTimeType editedObject
     */
    public DateTimeTypeEditor(SEDAMetadata metadata, SEDAObjectEditor parent) throws SEDALibException {
        super(metadata, parent);
        if (!(metadata instanceof DateTimeType)) throw new SEDALibException(
            "La métadonnée à éditer n'est pas du bon type"
        );

        this.update(getInitialValue());

        valueDateTimePicker.setDateTimePermissive(getInitialValue().toLocalDateTime());
        valueDateTimePicker.addDateTimeChangeListener(event -> {
            LocalDateTime old = event.getOldDateTimePermissive();
            LocalDateTime next = event.getNewDateTimePermissive();

            if (next != null && !next.equals(old)) {
                update(new DateTimeType(getInitialValue().getXmlElementName(), next));
            }
        });

        JPanel labelPanel = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        gbl.columnWeights = new double[] { 1.0 };
        labelPanel.setLayout(gbl);

        JLabel label = new JLabel(getName() + " :");
        label.setToolTipText(getTag());
        label.setFont(SEDAObjectEditor.LABEL_FONT);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.insets = new Insets(0, 5, 0, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;
        labelPanel.add(label, gbc);

        JPanel editPanel = new JPanel();
        gbl = new GridBagLayout();
        gbl.columnWeights = new double[] { 1.0 };
        editPanel.setLayout(gbl);

        gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.gridx = 0;
        gbc.gridy = 0;
        editPanel.add(valueDateTimePicker, gbc);

        gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.gridx = 0;
        gbc.gridy = 1;
        editPanel.add(warningLabel, gbc);

        this.sedaObjectEditorPanel = new SEDAObjectEditorSimplePanel(this, labelPanel, editPanel);
    }

    private DateTimeType getInitialValue() {
        return (DateTimeType) editedObject;
    }

    /**
     * Gets DateTimeType sample.
     *
     * @param elementName the element name, corresponding to the XML tag in SEDA
     * @param minimal     the minimal flag, if true subfields are selected and
     *                    values are empty, if false all subfields are added and
     *                    values are default values
     * @return the seda editedObject sample
     * @throws SEDALibException the seda lib exception
     */
    public static SEDAMetadata getSEDAMetadataSample(String elementName, boolean minimal) throws SEDALibException {
        if (minimal) return new DateTimeType(elementName);
        else return new DateTimeType(elementName, LocalDateTime.of(1970, 1, 1, 1, 0, 0));
    }

    @Override
    public SEDAMetadata extractEditedObject() throws SEDALibException {
        return lastDateTimeType;
    }

    @Override
    public String getSummary() throws SEDALibException {
        LocalDateTime tmp = valueDateTimePicker.getDateTimePermissive();
        if (tmp != null) return LocalDateTimeUtil.getFormattedDateTime(tmp);
        return "";
    }

    @Override
    public void createSEDAObjectEditorPanel() throws SEDALibException {}

    private String computeWarningMessage(DateTimeType dateTimeType) {
        String warning = "";
        if (dateTimeType.getFormatTypeEnum() == DateTimeFormatType.OFFSET_DATE_TIME) warning += " (Timezone)";
        TemporalAccessor ta = dateTimeType.getTemporalValue();
        if (ta instanceof LocalDateTime) {
            LocalDateTime ldt = (LocalDateTime) ta;
            if (ldt.getSecond() != 0 || ldt.getNano() != 0) warning += " (Secondes/Millisecondes)";
        } else if (ta instanceof OffsetDateTime) {
            OffsetDateTime odt = (OffsetDateTime) ta;
            if (odt.getSecond() != 0 || odt.getNano() != 0) warning += " (Secondes/Millisecondes)";
        }

        if (!warning.isEmpty()) {
            return "Perte si édition en mode structuré: " + warning;
        }

        return "";
    }

    private String computeWarningTooltip() {
        return (
            "Afin de ne pas perdre d'information, \n" + "il est nécessaire de modifier les dates en mode non structuré."
        );
    }

    private void updateWarning(DateTimeType dateTimeType) {
        warningLabel.setText(computeWarningMessage(dateTimeType));
        warningLabel.setForeground(Color.RED);
        warningLabel.setToolTipText(computeWarningTooltip());
    }

    private void update(DateTimeType next) {
        this.lastDateTimeType = next;
        this.updateWarning(this.lastDateTimeType);
    }
}

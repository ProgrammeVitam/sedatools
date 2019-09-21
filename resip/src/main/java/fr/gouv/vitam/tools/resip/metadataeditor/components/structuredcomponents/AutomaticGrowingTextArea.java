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
package fr.gouv.vitam.tools.resip.metadataeditor.components.structuredcomponents;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.time.Instant;

/**
 * The automatic growing JTextArea class with a lines count limit (lines count take into account wrapping).
 * <p>
 * This is a special class used in a MetadataEditorSimplePanel and is forcing it's GridBagLayout.
 */
public class AutomaticGrowingTextArea extends JTextArea {

    private int maxLines;
    private int currentLineNumber;
    private JScrollPane scrollPane;

    public AutomaticGrowingTextArea(int maxLines) {
        this.maxLines = maxLines;
        this.currentLineNumber = 1;
        AutomaticGrowingTextArea inner = this;

        //setPreferredSize(new Dimension());
        scrollPane = new JScrollPane(this);

        scrollPane.getViewport().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (inner.getText().length() > 0) {
                    JViewport viewport = (JViewport) e.getSource();
                    int lineCount = viewport.getView().getMinimumSize().height / inner.getRowHeight();
                    if (inner.currentLineNumber == Math.min(lineCount, maxLines))
                        return;
                    inner.currentLineNumber = Math.min(lineCount, maxLines);
                    MetadataEditorSimplePanel mesp = (MetadataEditorSimplePanel) scrollPane.getParent().getParent();
                    inner.setRows(inner.currentLineNumber);
                    GridBagLayout gbl = (GridBagLayout) mesp.getLayout();
                    gbl.rowHeights = new int[]{inner.currentLineNumber * inner.getRowHeight()+4};
                    gbl.rowWeights = new double[]{0.0};
                    mesp.revalidate();
                }
            }
        });
    }

    public JScrollPane getScrollPane() {
        return scrollPane;
    }
}

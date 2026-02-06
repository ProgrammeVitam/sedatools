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
package fr.gouv.vitam.tools.resip.frame;

import org.fife.rsta.ui.search.FindToolBar;
import org.fife.rsta.ui.search.SearchListener;

import javax.swing.*;
import java.awt.*;

public class ManifestFindToolBar extends FindToolBar {
    public ManifestFindToolBar(SearchListener listener) {
        super(listener);
    }

    @Override
    protected void createFindButtons() {
        findPrevButton = new JButton();
        makeEnterActivateButton(this.findPrevButton);
        findPrevButton.setIcon(new ImageIcon(getClass().getResource("/icon/go-previous.png")));
        findPrevButton.setToolTipText("Chercher un précédent...");
        findPrevButton.setText("");
        findPrevButton.setMaximumSize(new Dimension(16, 16));
        findPrevButton.setMinimumSize(new Dimension(16, 16));
        findPrevButton.setPreferredSize(new Dimension(16, 16));
        findPrevButton.setBorderPainted(false);
        findPrevButton.setContentAreaFilled(false);
        findPrevButton.setFocusPainted(false);
        findPrevButton.setFocusable(false);
        findPrevButton.setActionCommand("FindPrevious");
        findPrevButton.addActionListener(this.listener);
        findPrevButton.setEnabled(false);
        findButton = new JButton();
        this.makeEnterActivateButton(this.findButton);
        findButton.setIcon(new ImageIcon(getClass().getResource("/icon/go-next.png")));
        findButton.setToolTipText("Chercher un suivant...");
        findButton.setText("");
        findButton.setMaximumSize(new Dimension(16, 16));
        findButton.setMinimumSize(new Dimension(16, 16));
        findButton.setPreferredSize(new Dimension(16, 16));
        findButton.setBorderPainted(false);
        findButton.setContentAreaFilled(false);
        findButton.setFocusPainted(false);
        findButton.setFocusable(false);
        this.findButton.setActionCommand("FindNext");
        this.findButton.addActionListener(this.listener);
        this.findButton.setEnabled(false);
    }

    @Override
    protected Container createButtonPanel() {
        System.out.println("Here");
        Box panel = new Box(2);
        this.createFindButtons();
        panel.add(this.findPrevButton);
        panel.add(this.findButton);
        panel.add(Box.createHorizontalStrut(5));
        this.matchCaseCheckBox = this.createCB("MatchCase");
        panel.add(this.matchCaseCheckBox);
        this.regexCheckBox = this.createCB("RegEx");
        panel.add(this.regexCheckBox);
        this.wholeWordCheckBox = this.createCB("WholeWord");
        panel.add(this.wholeWordCheckBox);
        //this.markAllCheckBox = this.createCB("MarkAll");
        //panel.add(this.markAllCheckBox);
        return panel;
    }


}

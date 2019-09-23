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

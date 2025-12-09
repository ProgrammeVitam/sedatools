package fr.gouv.vitam.tools.resip.frame.preferences;

import fr.gouv.vitam.tools.sedalib.core.seda.SedaVersion;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SedaVersionSelector extends JPanel {

    private final Map<SedaVersion, JRadioButton> versionButtons = new LinkedHashMap<>();
    private final ButtonGroup buttonGroup = new ButtonGroup();
    private final List<SedaVersion> versions = Arrays.stream(SedaVersion.values())
        .filter(version -> !version.equals(SedaVersion.V2_0))
        .collect(Collectors.toList());

    public SedaVersionSelector(SedaVersion selectedVersion) {
        setLayout(new FlowLayout(FlowLayout.LEFT, 20, 0));
        initializeButtons(selectedVersion);
    }

    private void initializeButtons(SedaVersion selectedVersion) {
        for (SedaVersion version : this.versions) {
            JRadioButton radioButton = new JRadioButton(version.toString());
            versionButtons.put(version, radioButton);
            buttonGroup.add(radioButton);
            add(radioButton);

            if (version.equals(selectedVersion)) {
                radioButton.setSelected(true);
            }
        }
    }

    public SedaVersion getSelectedVersion() {
        return versionButtons.entrySet()
            .stream()
            .filter(entry -> entry.getValue().isSelected())
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(null); // ou une version par défaut
    }

    public void setSelectedVersion(SedaVersion version) {
        JRadioButton button = versionButtons.get(version);
        if (button != null) {
            button.setSelected(true);
        }
    }

    public Map<SedaVersion, JRadioButton> getVersionButtons() {
        return versionButtons;
    }
}


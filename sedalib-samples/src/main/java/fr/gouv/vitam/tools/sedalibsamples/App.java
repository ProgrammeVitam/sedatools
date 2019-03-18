package fr.gouv.vitam.tools.sedalibsamples;

/**
 * Sample SEDALib usage
 *
 */
public class App {
    public static void main(String[] args) throws Exception {
// Build a SIP from a disk hierarchy with no other treatment
        Sample1.run();

// Build a SIP from a disk hierarchy for a part and from affair files differentiated by their name
        Sample2.run();

// Build a SIP from a disk hierarchy for a part and from affair files organized in a csv
        Sample3.run();

// Build a SIP from a disk hierarchy with more affair files organized in a csv and to add to existing archived files group
        Sample3plus.run();

// Build a SIP from a disk hierarchy for a part and from affair files with hierarchy and metadata defined in a csv
// which is automatically interpreted
        Sample4.run();
    }
}

package fr.gouv.vitam.tools.sedalib;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import mslinks.ShellLink;
import mslinks.ShellLinkException;

class TestPrepare {

	public static boolean isPrepared = false;
	public static boolean isWindows = false;

	public static void createSymbolicLink(String link, String target) throws SEDALibException {
		Path linkpath = Paths.get(link);
		Path targetpath = Paths.get(target);
		try {
			Files.delete(linkpath);
			Files.delete(Paths.get(linkpath.toString()+".lnk"));
		} catch (Exception ignored) {
		}
		try {
			Files.delete(Paths.get(linkpath.toString()+".lnk"));
		} catch (Exception ignored) {
		}
		try {
			Files.createSymbolicLink(linkpath.toAbsolutePath(), linkpath.toAbsolutePath().getParent().relativize(targetpath.toAbsolutePath()));
		} catch (Exception e) {
			if (isWindows) {
				System.err.println(
						"Link creation is impossible, Windows shortcut creation is tried");
				ShellLink sl = new ShellLink();
				sl.setTarget(target);
				try {
					sl.saveTo(link + ".lnk");
				} catch (IOException e1) {
					throw new SEDALibException(
							"Link and Windows shortcut [" + link + "] creation impossible\n->" + e.getMessage());
				}
			}
			else
				throw new SEDALibException(
						"Link [" + link + "] creation impossible\n->" + e.getMessage());
		}
	}

	public static void createShortcutIfWindows(String link, String target)
			throws IOException, SEDALibException {
		if (!isWindows)
			createSymbolicLink(link, target);
		else {
			Path linkpath = Paths.get(link);
			try {
				Files.delete(linkpath);
			} catch (Exception ignored) {
			}
			ShellLink sl = new ShellLink();
			sl.setTarget(target);
			sl.saveTo(link);
		}
	}

	static void ContructTestFiles() throws IOException, ShellLinkException, SEDALibException {
		if (!isPrepared) {
			String prefix;
			isWindows=System.getProperty("os.name").toLowerCase().contains("win");


			prefix = "src/test/resources/PacketSamples/SampleWithLinksModelV2/Root/";
			// regenerate PacketSamples.SampleWithWindowsLinksAndShortcutsModelV2 links
			createSymbolicLink(prefix + "Link Node 1.2", prefix + "Node 1/Node 1.2");
			createSymbolicLink(prefix + "Link SmallContract.json",
					prefix + "Node 2/Node 2.3 - Many/SmallContract.json");
			createShortcutIfWindows(prefix + "Shortcut Node 2.4 - OG Link.lnk", prefix + "Node 2/Node 2.4 - OG Link");
			createShortcutIfWindows(prefix + "ShortCut OK-RULES-MDRULES.zip.lnk",
					prefix + "Node 2/Node 2.3 - Many/OK-RULES-MDRULES.zip");
			createSymbolicLink(prefix + "Node 2/Node 2.4 - OG Link/Link ##Test ObjectGroup##",
					prefix + "Node 1/##Test ObjectGroup##");
			createShortcutIfWindows(prefix + "Node 2/Node 2.5 - OG Shortcut/Shortcut ##Test ObjectGroup##.lnk",
					prefix + "Node 1/##Test ObjectGroup##");
			System.err.println("Test files with links in ["+prefix+"] prepared");
			isPrepared = true;
		}
	}

}

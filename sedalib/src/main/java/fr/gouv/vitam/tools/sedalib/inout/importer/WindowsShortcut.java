/**
 * Represents a Windows shortcut (typically visible to Java only as a '.lnk' file).
 * <p>
 * Retrieved 2011-09-23 from http://stackoverflow.com/questions/309495/windows-shortcut-lnk-parser-in-java/672775#672775
 * Originally called LnkParser
 * <p>
 * Written by: (the stack overflow users, obviously!)
 * isPotentialValidLink made more efficient by not opening file if name doesn't end with .lnk
 * and correction of offset computation (made 32bits) JS Lair
 * Apache Commons VFS dependency removed by crysxd (why were we using that!?) https://github.com/crysxd
 * Headerified, refactored and commented by Code Bling http://stackoverflow.com/users/675721/code-bling
 * Network file support added by Stefan Cordes http://stackoverflow.com/users/81330/stefan-cordes
 * Adapted by Sam Brightman http://stackoverflow.com/users/2492/sam-brightman
 * Support for additional strings (description, relative_path, working_directory, command_line_arguments) added by Max Vollmer https://stackoverflow.com/users/9199167/max-vollmer
 * Based on information in 'The Windows Shortcut File Format' by Jesse Hager &lt;jessehager@iname.com&gt;
 * And somewhat based on code from the book 'Swing Hacks: Tips and Tools for Killer GUIs'
 * by Joshua Marinacci and Chris Adamson
 * ISBN: 0-596-00907-0
 * http://www.oreilly.com/catalog/swinghks/
 */

package fr.gouv.vitam.tools.sedalib.inout.importer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;

/**
 * The Class WindowsShortcut.
 */
public class WindowsShortcut {

    /** The is directory. */
    private boolean isDirectory;

    /** The is local. */
    private boolean isLocal;

    /** The real file. */
    private String real_file;

    /** The description. */
    private String description;

    /** The relative path. */
    private String relative_path;

    /** The working directory. */
    private String working_directory;

    /** The command line arguments. */
    private String command_line_arguments;

    /**
     * Provides a quick test to see if this could be a valid link ! If you try to
     * instantiate a new WindowShortcut and the link is not valid, Exceptions may be
     * thrown and Exceptions are extremely slow to generate, therefore any code
     * needing to loop through several files should first check this.
     *
     * @param file the potential link
     * @return true if may be a link, false otherwise
     */
    public static boolean isPotentialValidLink(final File file) {
        final int minimum_length = 0x64;
        boolean isPotentiallyValid = false;
        if (file.getName().toLowerCase().endsWith(".lnk"))
            try (final InputStream fis = new FileInputStream(file)) {
                isPotentiallyValid = file.isFile() && fis.available() >= minimum_length && isMagicPresent(getBytes(fis, 32));
            } catch (Exception e) {
                // forget it
            }
        return isPotentiallyValid;
    }

    /**
     * Instantiates a new windows shortcut.
     *
     * @param file the file
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ParseException the parse exception
     */
    public WindowsShortcut(final File file) throws IOException, ParseException {
        try (InputStream in = new FileInputStream(file)) {
            parseLink(getBytes(in));
        }
    }

    /**
     * Gets the real filename.
     *
     * @return the name of the filesystem object pointed to by this shortcut
     */
    public String getRealFilename() {
        return real_file;
    }

    /**
     * Gets the description.
     *
     * @return a description for this shortcut, or null if no description is set
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the relative path.
     *
     * @return the relative path for the filesystem object pointed to by this
     *         shortcut, or null if no relative path is set
     */
    public String getRelativePath() {
        return relative_path;
    }

    /**
     * Gets the working directory.
     *
     * @return the working directory in which the filesystem object pointed to by
     *         this shortcut should be executed, or null if no working directory is
     *         set
     */
    public String getWorkingDirectory() {
        return working_directory;
    }

    /**
     * Gets the command line arguments.
     *
     * @return the command line arguments that should be used when executing the
     *         filesystem object pointed to by this shortcut, or null if no command
     *         line arguments are present
     */
    public String getCommandLineArguments() {
        return command_line_arguments;
    }

    /**
     * Tests if the shortcut points to a local resource.
     *
     * @return true if the 'local' bit is set in this shortcut, false otherwise
     */
    public boolean isLocal() {
        return isLocal;
    }

    /**
     * Tests if the shortcut points to a directory.
     *
     * @return true if the 'directory' bit is set in this shortcut, false otherwise
     */
    public boolean isDirectory() {
        return isDirectory;
    }

    /**
     * Gets all the bytes from an InputStream.
     *
     * @param in the InputStream from which to read bytes
     * @return array of all the bytes contained in 'in'
     * @throws IOException if an IOException is encountered while reading the data
     *                     from the InputStream
     */
    private static byte[] getBytes(final InputStream in) throws IOException {
        return getBytes(in, null);
    }

    /**
     * Gets up to max bytes from an InputStream.
     *
     * @param in  the InputStream from which to read bytes
     * @param max maximum number of bytes to read
     * @return array of all the bytes contained in 'in'
     * @throws IOException if an IOException is encountered while reading the data
     *                     from the InputStream
     */
    private static byte[] getBytes(final InputStream in, Integer max) throws IOException {
        // read the entire file into a byte buffer
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        final byte[] buff = new byte[256];
        while (max == null || max > 0) {
            final int n = in.read(buff);
            if (n == -1) {
                break;
            }
            bout.write(buff, 0, n);
            if (max != null)
                max -= n;
        }
        in.close();
        return bout.toByteArray();
    }

    /**
     * Checks if is magic present.
     *
     * @param link the link
     * @return true, if is magic present
     */
    private static boolean isMagicPresent(final byte[] link) {
        final int magic = 0x0000004C;
        final int magic_offset = 0x00;
        return link.length >= 32 && bytesToDword(link, magic_offset) == magic;
    }

    /**
     * Gobbles up link data by parsing it and storing info in member fields.
     *
     * @param link all the bytes from the .lnk file
     * @throws ParseException the parse exception
     */
    private void parseLink(final byte[] link) throws ParseException {
        try {
            if (!isMagicPresent(link))
                throw new ParseException("Invalid shortcut; magic is missing", 0);

            // get the flags byte
            final byte flags = link[0x14];

            // get the file attributes byte
            final int file_atts_offset = 0x18;
            final byte file_atts = link[file_atts_offset];
            final byte is_dir_mask = (byte) 0x10;
            isDirectory = (file_atts & is_dir_mask) > 0;

            // if the shell settings are present, skip them
            final int shell_offset = 0x4c;
            final byte has_shell_mask = (byte) 0x01;
            int shell_len = 0;
            if ((flags & has_shell_mask) > 0) {
                // the plus 2 accounts for the length marker itself
                shell_len = bytesToWord(link, shell_offset) + 2;
            }

            // get to the file settings
            final int file_start = 0x4c + shell_len;

            final int file_location_info_flag_offset_offset = 0x08;
            final int file_location_info_flag = link[file_start + file_location_info_flag_offset_offset];
            isLocal = (file_location_info_flag & 1) == 1;
            // get the local volume and local system values
            final int basename_offset_offset = 0x10;
            final int networkVolumeTable_offset_offset = 0x14;
            final int finalname_offset_offset = 0x18;
            final int finalname_offset = bytesToDword(link, file_start + finalname_offset_offset) + file_start;
            final String finalname = getNullDelimitedString(link, finalname_offset);
            if (isLocal) {
                final int basename_offset = bytesToDword(link, file_start + basename_offset_offset) + file_start;
                final String basename = getNullDelimitedString(link, basename_offset);
                real_file = basename + finalname;
            } else {
                final int networkVolumeTable_offset = bytesToDword(link, file_start + networkVolumeTable_offset_offset) + file_start;
                final int shareName_offset_offset = 0x08;
                final int shareName_offset = bytesToDword(link, networkVolumeTable_offset + shareName_offset_offset)
                        + networkVolumeTable_offset;
                final String shareName = getNullDelimitedString(link, shareName_offset);
                real_file = shareName + "\\" + finalname;
            }

            // parse additional strings coming after file location
            final int file_location_size = bytesToDword(link, file_start);
            int next_string_start = file_start + file_location_size;

            final byte has_description = (byte) 0b00000100;
            final byte has_relative_path = (byte) 0b00001000;
            final byte has_working_directory = (byte) 0b00010000;
            final byte has_command_line_arguments = (byte) 0b00100000;

            // if description is present, parse it
            if ((flags & has_description) > 0) {
                final int string_len = bytesToWord(link, next_string_start) * 2; // times 2 because UTF-16
                description = getUTF16String(link, next_string_start + 2, string_len);
                next_string_start = next_string_start + string_len + 2;
            }

            // if relative path is present, parse it
            if ((flags & has_relative_path) > 0) {
                final int string_len = bytesToWord(link, next_string_start) * 2; // times 2 because UTF-16
                relative_path = getUTF16String(link, next_string_start + 2, string_len);
                next_string_start = next_string_start + string_len + 2;
            }

            // if working directory is present, parse it
            if ((flags & has_working_directory) > 0) {
                final int string_len = bytesToWord(link, next_string_start) * 2; // times 2 because UTF-16
                working_directory = getUTF16String(link, next_string_start + 2, string_len);
                next_string_start = next_string_start + string_len + 2;
            }

            // if command line arguments are present, parse them
            if ((flags & has_command_line_arguments) > 0) {
                final int string_len = bytesToWord(link, next_string_start) * 2; // times 2 because UTF-16
                command_line_arguments = getUTF16String(link, next_string_start + 2, string_len);
            }

        } catch (final ArrayIndexOutOfBoundsException e) {
            throw new ParseException("Could not be parsed, probably not a valid WindowsShortcut", 0);
        }
    }

    /**
     * Gets the null delimited string.
     *
     * @param bytes the bytes
     * @param off the off
     * @return the null delimited string
     */
    private static String getNullDelimitedString(final byte[] bytes, final int off) {
        int len = 0;
        // count bytes until the null character (0)
        while (bytes[off + len] != 0) {
            len++;
        }
        return new String(bytes, off, len);
    }

    /**
     * Gets the UTF 16 string.
     *
     * @param bytes the bytes
     * @param off the off
     * @param len the len
     * @return the UTF 16 string
     */
    private static String getUTF16String(final byte[] bytes, final int off, final int len) {
        return new String(bytes, off, len, StandardCharsets.UTF_16LE);
    }

    /**
     * Bytes to word.
     *
     * @param bytes the bytes
     * @param off the off
     * @return the int
     */
    /*
     * convert two bytes into a short note, this is little endian because it's for
     * an Intel only OS.
     */
    private static int bytesToWord(final byte[] bytes, final int off) {
        return ((bytes[off + 1] & 0xff) << 8) | (bytes[off] & 0xff);
    }

    /**
     * Bytes to dword.
     *
     * @param bytes the bytes
     * @param off the off
     * @return the int
     */
    private static int bytesToDword(final byte[] bytes, final int off) {
        return (bytesToWord(bytes, off + 2) << 16) | bytesToWord(bytes, off);
    }

}
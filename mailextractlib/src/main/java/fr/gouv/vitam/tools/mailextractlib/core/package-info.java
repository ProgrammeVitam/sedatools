/**
 * Provides all core classes and method to do extraction and listing of mail
 * boxes.
 * <p>
 * It uses:
 * <ul>
 * <li>JavaMail to access IMAP, IMAPS, POP3 (GIMAP experimental) account and Thunderbird mbox
 * directory or Eml,</li>
 * <li>libpst to access Outlook pst files, and</li>
 * <li>apache POI HSMF to access msg files.</li>
 * </ul>
 */
package fr.gouv.vitam.tools.mailextractlib.core;
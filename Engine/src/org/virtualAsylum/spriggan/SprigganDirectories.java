package org.virtualAsylum.spriggan;

import java.io.File;

/**
 * Created by Morgan on 11/03/14.
 */
public class SprigganDirectories {
    public static final File
            home = new File(String.format("%s%sDocuments%sElder Scrolls Online%slive%s", System.getProperty("user.home"), File.separator, File.separator , File.separator, File.separator)),
            management = new File(home, "Addon Repository"),
            installation = new File(home, "Addons");
}

/**
 * 
 */
package net.lahwran.package_renamer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * @author lahwran
 *
 */
public class ZipTools {

    public static void extractZip(File zip, File targetdir) throws IOException {
        //Create input and output streams
        ZipFile zipfile = new ZipFile(zip);

        byte[] buffer = new byte[1024];
        Enumeration<? extends ZipEntry> entries = zipfile.entries();
        //Get next zip entry and start reading data
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            
            File targfile = new File(targetdir, entry.getName());

            if (!targfile.toPath().normalize().startsWith(targetdir.toPath().normalize())) {
                throw new IOException("Bad zip entry");
            }
            if (entry.isDirectory()) {
                targfile.mkdirs();
            } else {
                targfile.getParentFile().mkdirs();
                InputStream in = zipfile.getInputStream(entry);
                OutputStream out = new BufferedOutputStream(new FileOutputStream(targfile));
                int readcount;
                while ((readcount = in.read(buffer)) > 0) {
                    out.write(buffer, 0, readcount);
                }
                out.close();
                in.close();
            }
        }
    }
}

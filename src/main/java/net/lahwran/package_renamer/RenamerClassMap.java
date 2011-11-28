/**
 * 
 */
package net.lahwran.package_renamer;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * @author lahwran
 *
 */
public class RenamerClassMap extends javassist.ClassMap {
    private Rename[] dynamicrenames;
    private Rename[] staticrenames;
    public RenamerClassMap(Rename[] dynamicrenames, Rename[] staticrenames) {
        this.dynamicrenames = dynamicrenames;
        this.staticrenames = staticrenames;
    }
    public void addDirectory(File start) {
        addDirectory(start, "", null);
    }
    
    public void addDirectory(File start, HashMap<String, File> files) {
        addDirectory(start, "", files);
    }
    
    public void addDirectory(File start, String position, HashMap<String, File> files) {
        File dir = new File(start, position);
        String[] children = dir.list();
        for(String childname:children) {
            File child = new File(dir, childname);
            if(childname.endsWith(".class") && child.isFile() && !child.isDirectory()) {
                String classname = position+childname.substring(0,childname.length()-6);
                if (files != null) {
                    files.put(classname, child);
                }
                String newname = Rename.performRenames(staticrenames, toJavaName(classname));
                if (newname != null) {
                    put(classname, toJvmName(newname));
                }
                
            } else if (child.isDirectory()) {
                addDirectory(start, position+child.getName()+"/", files);
            }
        }
    }

    public void addZip(File zip) throws IOException {
        ZipFile zipfile = new ZipFile(zip);
        Enumeration<? extends ZipEntry> entries = zipfile.entries();

        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (!entry.isDirectory()) {
                String classname = entry.getName().substring(0,entry.getName().length()-6)
                        .replace("\\", "/");
                String newname = Rename.performRenames(staticrenames, toJavaName(classname));
                if (newname != null) {
                    put(classname, toJvmName(newname));
                }
            }
        }
    }

    public Object get(Object jvmClassName) {
        String name = (String) super.get(jvmClassName);
        if (dynamicrenames.length > 0) {
            if (name == null) {
                name = toJavaName((String)jvmClassName);
            } else {
                name = toJavaName(name);
            }
            name = Rename.performRenames(dynamicrenames, name);
            
            if (name != null)
                return toJvmName(name);
            return null;
        } else {
            return name;
        }
    }
}

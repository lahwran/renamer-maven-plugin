/**
 * 
 */
package net.lahwran.package_renamer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author lahwran
 *
 */
public class Rename {

    /**
     * @parameter
     * @required
     */
    public String from;
    /**
     * @parameter
     * @required
     */
    public String to;
    /**
     * @parameter default-value="true"
     */
    public String packageOnly;
    private Boolean __packageOnly;
    
    
    /**
     * @parameter default-value="true"
     */
    public String dynamic;
    private Boolean __dynamic;

    private Pattern __re;
    
    boolean getDynamic() {
        if (__dynamic == null) {
            if (dynamic == null)
                dynamic = "true";
            __dynamic = Boolean.valueOf(dynamic);
        }
        return __dynamic;
    }
    
    boolean getPackageOnly() {
        if (__packageOnly == null) {
            if (packageOnly == null)
                packageOnly = "true";
            __packageOnly = Boolean.valueOf(packageOnly);
        }
        return __packageOnly;
    }
    
    public static String expand(Matcher matcher, String format) {
        StringBuilder output = new StringBuilder();
        boolean escaped = false;
        StringBuilder num = null;
        for (char c:format.toCharArray()) {
            if (escaped) {
                if (c == '\\') {
                    output.append('\\');
                    escaped = false;
                } else if (c >= 49 && c <= 57) { //numeric
                    if (num == null) {
                        num = new StringBuilder(c);
                    } else {
                        num.append(c);
                    }
                } else {
                    if (num != null) {
                        output.append(matcher.group(Integer.parseInt(num.toString())));
                    }
                    output.append(c);
                    escaped = false;
                }
            } else if (c == '\\') {
                escaped = true;
                
            } else {
                output.append(c);
            }
        }
        return output.toString();
    }
    
    public Pattern getRE() {
        if (__re == null)
            __re = Pattern.compile(getFrom());
        return __re;
    }
    
    public String getTo() {
        if (to == null || to.trim().equals("-"))
            to = "";
        return to;
    }
    public String getFrom() {
        if (from == null || from.trim().equals("-"))
            from = "";
        return from;
    }
    
    public String toString() {
        return "Rename: "+from+" "+to+" "+((__re == null) ? "null" : __re);
    }
    
    public static final Pattern packagepattern = Pattern.compile("(.*?)\\.([^.]*)");
    public static final Pattern failedpackage = Pattern.compile("()(.*)");
    public static String performRenames(Rename[] renames, String name) {
        boolean changed = false;
        for (int i=0; i<renames.length; i++) {
            Rename rename = renames[i];
            if (rename.getPackageOnly()) {
                Matcher m = packagepattern.matcher(name);
                if (!m.matches()) {
                    m = failedpackage.matcher(name);
                    if (!m.matches()) {
                        continue;
                    }
                }
                String pkg = m.group(1);
                Matcher renamematch = rename.getRE().matcher(pkg);
                if (!renamematch.matches())
                    continue;
                String newpkg = expand(renamematch, rename.getTo());
                if (newpkg.isEmpty()) {
                    name = m.group(2);
                } else {
                    name = newpkg + "." + m.group(2);
                }
                changed = true;
            } else {
                Matcher renamematch = rename.getRE().matcher(name);
                if (!renamematch.matches())
                    continue;
                name = expand(renamematch, rename.getTo());
                changed = true;
            }
        }
        if (!changed)
            return null;
        return name;
    }
}

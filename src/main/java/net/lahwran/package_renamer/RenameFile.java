/**
 * 
 */
package net.lahwran.package_renamer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.lahwran.package_renamer.config.Configuration;
import net.lahwran.package_renamer.config.ConfigurationNode;

/**
 * @author yetanotherx
 *
 */
public class RenameFile {

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
     * @parameter
     * @required
     */
    public String file;
    
    public Rename[] getRenames() {
        
        File configFile = new File(this.file);
        if( !configFile.exists() ) {
            return new Rename[] {};
        }
        
        Configuration conf = new Configuration(configFile);
        conf.load();
        
        List<ConfigurationNode> renames = conf.getNodeList("renames", new ArrayList<ConfigurationNode>());
        List<Rename> list = new ArrayList<Rename>();
        
        for( ConfigurationNode rename : renames ) {
            Rename newRename = new Rename();
            newRename.from = rename.getString(this.from, "");
            newRename.to = rename.getString(this.to, "");
            newRename.dynamic = rename.getString("dynamic");
            newRename.packageOnly = rename.getString("packageOnly");
            
            if( newRename.from.isEmpty() || newRename.to.isEmpty() ) {
                continue;
            }
            
            list.add(newRename);
        }
        
        return list.toArray(new Rename[] {});
    }

    public String toString() {
        return "Configuration file " + file;
    }
}

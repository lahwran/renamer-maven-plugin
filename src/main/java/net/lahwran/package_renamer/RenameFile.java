/**
 * 
 */
package net.lahwran.package_renamer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.lahwran.package_renamer.config.Configuration;
import net.lahwran.package_renamer.config.ConfigurationNode;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

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
    
    public Rename[] getRenames(AbstractMojo mojo) throws MojoExecutionException {
        
        File configFile = new File(this.file);
        if (!configFile.exists()) {
            throw new MojoExecutionException("Obfuscation file could not be read: " + configFile.getAbsolutePath());
        }
        
        mojo.getLog().info("Reading obfuscation values from " + configFile.getAbsolutePath());
        Configuration conf = new Configuration(configFile);
        conf.load();
        
        List<ConfigurationNode> renames = conf.getNodeList("renames", new ArrayList<ConfigurationNode>());
        List<Rename> list = new ArrayList<Rename>();
        
        for (ConfigurationNode rename : renames) {
            Rename newRename = new Rename();
            newRename.from = rename.getString(this.from, null);
            newRename.to = rename.getString(this.to, null);
            newRename.dynamic = rename.getString("dynamic");
            newRename.packageOnly = rename.getString("packageOnly");
            
            if (newRename.from == null) {
                newRename.from = "";
            }
            if (newRename.to == null) {
                newRename.to = "";
            }
            list.add(newRename);
        }
        
        return list.toArray(new Rename[]{});
    }
    
    public String toString() {
        return "Configuration file " + file;
    }
}

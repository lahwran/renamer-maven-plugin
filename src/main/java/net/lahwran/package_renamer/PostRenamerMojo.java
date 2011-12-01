package net.lahwran.package_renamer;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

import javassist.bytecode.ClassFile;

/**
 * Goal which renames classes of a dependency and outputs them to the classes dir
 * (yes, this is cheating)
 * 
 * @goal rename
 * 
 * @phase process-classes
 * @requiresDependencyResolution runtime
 */
public class PostRenamerMojo extends AbstractMojo {
    /**
     * @parameter default-value="${project}"
     * @readonly
     * @required
     */
    private MavenProject project;

    /**
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File outputDirectory;
    
    /**
     * @parameter
     * @required
     */
    private Rename[] renames;
    
    /**
     * @parameter
     */
    private RenameFile[] renameFiles;
    
    /**
     * @parameter
     */
    private ArtifactMatcher refereceArtifacts;

    public void execute() throws MojoExecutionException {
        Set<Artifact> artifacts = null;
        if (refereceArtifacts != null)
            artifacts = refereceArtifacts.resolveArtifacts(project, getLog());
        else
            artifacts = new HashSet<Artifact>();

        ArrayList<Rename> dynamiclist = new ArrayList<Rename>();
        ArrayList<Rename> staticlist = new ArrayList<Rename>();
        for(Rename rename:renames) {
            if (rename.getDynamic()) {
                dynamiclist.add(rename);
            } else {
                staticlist.add(rename);
            }
        }
        
        for( RenameFile renameFile : renameFiles ) {
            for(Rename rename:renameFile.getRenames(this)) {
                if (rename.getDynamic()) {
                    dynamiclist.add(rename);
                } else {
                    staticlist.add(rename);
                }
            }
        }
        
        Rename[] dynamicrenames = dynamiclist.toArray(new Rename[dynamiclist.size()]);
        Rename[] staticrenames = staticlist.toArray(new Rename[staticlist.size()]);
        RenamerClassMap renamermap = new RenamerClassMap(dynamicrenames, staticrenames);
        
        HashMap<String, File> toProcess = new HashMap<String, File>();
        
        for (Artifact artifact:artifacts) {
            try {
                renamermap.addZip(artifact.getFile());
            } catch (IOException e1) {
                throw new MojoExecutionException("Error reading in dependency zip", e1);
            }
        }
        File classdir = new File(outputDirectory, "classes");
        
        renamermap.addDirectory(classdir, toProcess);
        
        for(Entry<String, File> e:toProcess.entrySet())
        {
            String rename = (String) renamermap.get(e.getKey());
            File in = e.getValue();
            File out = in;
            if(rename != null)
                out = new File(classdir, rename+".class");
            getLog().info("Processing "+e.getKey()+(rename != null ? " -> "+rename : ""));
            if(!in.exists())
            {
                getLog().error(in.getAbsolutePath()+" MISSING! ********************");
                continue;
            }
            try
            {
                ClassFile processing = new ClassFile(new DataInputStream(new FileInputStream(in)));
                processing.renameClass(renamermap);
                if(rename != null && out.exists())
                {
                    getLog().warn(in.getAbsolutePath()+" would overwrite " + out.getAbsolutePath() +" ********************");
                    continue;
                }
                in.delete();
                processing.write(new DataOutputStream(new FileOutputStream(out)));
            }
            catch (IOException e1)
            {
                e1.printStackTrace();
                continue;
            }
        }
    }
}

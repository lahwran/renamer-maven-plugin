/**
 * 
 */
package net.lahwran.package_renamer;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.SelectorUtils;

/**
 * inspired by the maven shade plugin; produces compatible output. Slightly
 * less efficient for the sake of brevity.
 * @author lahwran
 */
public class ArtifactMatcher {

    private Set<String> includes;

    private Set<String> excludes;

    public boolean patternMatches(String pattern, Artifact artifact) {
        if (pattern == null) return false;
        String[] split = pattern.split(":");

        String groupID = (split.length > 0) ? split[0] : "";
        String artifactID = (split.length > 1) ? split[1] : "*";
        String type = (split.length > 3) ? split[2] : "*";
        String classifier = (split.length > 3) ? split[3] : ((split.length > 2) ? split[2] : "*");

        return SelectorUtils.match(groupID, artifact.getGroupId()) &&
                SelectorUtils.match(artifactID, artifact.getArtifactId()) &&
                SelectorUtils.match(type, artifact.getType()) &&
                (artifact.getClassifier() == null ||
                 SelectorUtils.match(classifier, artifact.getClassifier()));
    }

    public boolean isSelected(Artifact artifact) {
        if (artifact == null)
            return false;
        boolean included = false;
        boolean excluded = false;
        if (includes == null || includes.isEmpty()) {
            included = true;
        } else {
            for (String include:includes) {
                if (patternMatches(include, artifact)) {
                    included = true;
                    break;
                }
            }
        }

        if (included && excludes != null) { //only matters if it was excluded if it was also included
            for (String exclude:excludes) {
                if (patternMatches(exclude, artifact)) {
                    excluded = true;
                    break;
                }
            }
        }
        return included && !excluded;
    }

    public Set<Artifact> resolveArtifacts(MavenProject project, Log log) {
        Set<Artifact> artifacts = (Set<Artifact>)project.getArtifacts();
        Set<Artifact> filteredArtifacts = new HashSet<Artifact>();
        for(Artifact artifact:artifacts) {
            if(artifact.getType().equals("pom")) {
                log.info("Skipping pom dependency " + artifact.getId());
                continue;
            }
            boolean selected = isSelected(artifact);
            if (selected) {
                File artifactFile = artifact.getFile();
                log.info("+ Including " + artifact.getId() + " from file " + artifactFile.getName());
            } else {
                log.info("- Excluding " + artifact.getId());
            }
        }
        return filteredArtifacts;
    }
}

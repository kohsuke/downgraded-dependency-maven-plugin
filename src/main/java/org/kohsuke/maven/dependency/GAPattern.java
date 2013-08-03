package org.kohsuke.maven.dependency;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;

import java.util.regex.Pattern;

/**
 * GLOB pattern matcher on groupId+artifactId.
 *
 * @author Kohsuke Kawaguchi
 */
public class GAPattern implements ArtifactFilter {
    private final Pattern groupId;
    private final Pattern artifactId;

    public GAPattern(String v) {
        String[] tokens = v.split(":");
        if (tokens.length!=2)
            throw new IllegalArgumentException("Expecting groupId:artifactId but got "+v);
        groupId = glob(tokens[0]);
        artifactId = glob(tokens[1]);
    }

    private Pattern glob(String s) {
        return Pattern.compile(s.replace(".","\\.").replace("*",".*").replace("?","."));
    }

    public boolean include(Artifact a) {
        return match(groupId,a.getGroupId()) && match(artifactId,a.getArtifactId());
    }

    private boolean match(Pattern matcher, String value) {
        return matcher.matcher(value).matches();
    }
}

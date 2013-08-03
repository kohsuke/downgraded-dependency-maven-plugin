package org.kohsuke.maven.dependency;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;

import java.util.List;

/**
 * {@link ArtifactFilter} that combines multiple {@link ArtifactFilter}s in the OR semantics.
 * @author Kohsuke Kawaguchi
 */
public class OrArtifactFilter implements ArtifactFilter {
    private List<ArtifactFilter> members;

    public OrArtifactFilter(List<ArtifactFilter> members) {
        this.members = members;
    }

    public boolean include(Artifact artifact) {
        for (ArtifactFilter member : members) {
            if (member.include(artifact))
                return true;
        }
        return false;
    }
}

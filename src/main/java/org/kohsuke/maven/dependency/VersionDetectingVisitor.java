package org.kohsuke.maven.dependency;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.OverConstrainedVersionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.apache.maven.shared.dependency.tree.traversal.DependencyNodeVisitor;

/**
 * @author Kohsuke Kawaguchi
 */
public class VersionDetectingVisitor implements DependencyNodeVisitor {

    private final ArtifactFilter exclusion;

    private final Log log;

    /**
     * Was there an error?
     */
    private boolean error;

    public VersionDetectingVisitor(Log log, ArtifactFilter exclusion) {
        this.log = log;
        this.exclusion = exclusion;
    }

    public boolean hasError() {
        return error;
    }

    public boolean visit(DependencyNode node) {
        if (node.getState()==DependencyNode.OMITTED_FOR_CONFLICT) {
            try {
                ArtifactVersion dropped = getVersionOf(node.getArtifact());
                ArtifactVersion active = getVersionOf(node.getRelatedArtifact());
                if (dropped.compareTo(active)>0 && !exclusion.include(node.getArtifact())) {
                    log.error("Unexpected forced downgrade: "+node.toString().trim());
                    error = true;
                }
            } catch (OverConstrainedVersionException e) {
                // not sure under what circumstances this would happen
                throw new RuntimeException("Failed to parse dependency:"+node,e);
            }
        }
        return true;
    }

    private ArtifactVersion getVersionOf(Artifact a) throws OverConstrainedVersionException {
        return a.getSelectedVersion();
    }

    public boolean endVisit(DependencyNode node) {
        return true;
    }
}

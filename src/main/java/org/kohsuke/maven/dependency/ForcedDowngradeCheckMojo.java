package org.kohsuke.maven.dependency;

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

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilder;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilderException;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks if the dependency resolution is picking up old versions unknowingly.
 *
 */
@Mojo(name = "check", requiresDependencyResolution = ResolutionScope.TEST, threadSafe = true, defaultPhase = LifecyclePhase.VERIFY)
public class ForcedDowngradeCheckMojo extends AbstractMojo
{
    /**
     * The Maven project.
     */
    @Component
    private MavenProject project;

    /**
     * The dependency tree builder to use.
     */
    @Component( hint = "default" )
    private DependencyGraphBuilder dependencyGraphBuilder;

    /**
     * The dependency tree builder to use for verbose output.
     */
    @Component
    private DependencyTreeBuilder dependencyTreeBuilder;

    @Parameter( defaultValue = "${localRepository}", readonly = true )
    private ArtifactRepository localRepository;

    /**
     * List of artifacts that you are OK with Maven picking up older version.
     *
     * Each exclusion is "groupId:artifactId" format. Wildcard '*' can be used to match anything,
     * such as "groupId:*".
     */
    @Parameter
    private List<String> excludes = new ArrayList<String>();


    public void execute() throws MojoExecutionException {
        try {
            // verbose mode force Maven 2 dependency tree component use
            DependencyNode dep = dependencyTreeBuilder.buildDependencyTree(project, localRepository, null);

            VersionDetectingVisitor d = new VersionDetectingVisitor(getLog(), buildExclusionFilter());
            dep.accept(d);
            if (d.hasError())
                throw new MojoExecutionException("Problems were found");
        } catch (DependencyTreeBuilderException e) {
            throw new MojoExecutionException("Failed to analyze dependencies",e);
        }
    }

    private ArtifactFilter buildExclusionFilter() {
        List<ArtifactFilter> filters = new ArrayList<ArtifactFilter>();
        for (String e : excludes) {
            filters.add(new GAPattern(e));
        }
        return new OrArtifactFilter(filters);
    }

}

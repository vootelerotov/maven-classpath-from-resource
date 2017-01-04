package org.bashhead.maven.rad;

import static org.codehaus.plexus.PlexusTestCase.getBasedir;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.LinkedHashSet;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.project.MavenProject;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;

public class AddResourceDependenciesMojoTest{

    @Rule
    public MojoRule mojoRule = new MojoRule();

    @Test
    public void addsResourcesToClasspath() throws Exception{
        File basedir = new File( getBasedir(), "target/test-classes/unit/add-dependencies" );
        MavenProject project = mojoRule.readMavenProject( basedir );
        // set by org.apache.maven.lifecycle.internal.LifecycleDependencyResolver
        project.setDependencyArtifacts( new LinkedHashSet<Artifact>() );

        mojoRule.executeMojo( project, "process" );

        assertThat( project.getDependencyArtifacts(), hasSize( 3 ) );
        assertThat( project.getDependencyArtifacts(), hasItem( hasArtifact( "local.dependency.libs:dep1:jar:0.0.1:system" ) ) );
        assertThat( project.getDependencyArtifacts(), hasItem( hasArtifact( "local.dependency.libs:dep2:zip:0.0.1:system" ) ) );
        assertThat( project.getDependencyArtifacts(), hasItem( hasArtifact( "local.dependency.libs:dep4:jar:0.0.1:system" ) ) );
    }

    public Matcher<Artifact> hasArtifact( String path ){
        return hasToString( path );
    }

}

package com.bashhead.maven.rad;

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
    private static final String VERSION = "0.0.1";
    private static final String SCOPE = "system";

    @Test
    public void addsResourcesToClasspath() throws Exception{
        File basedir = new File( getBasedir(), "target/test-classes/unit/add-dependencies" );
        MavenProject project = mojoRule.readMavenProject( basedir );
        // set by org.apache.maven.lifecycle.internal.LifecycleDependencyResolver
        project.setDependencyArtifacts( new LinkedHashSet<Artifact>() );

        mojoRule.executeMojo( project, "process" );

        assertThat( project.getDependencyArtifacts(), hasSize( 3 ) );
        String groupId = getGroupId( new File(basedir, "libs" ).getAbsolutePath() );
        assertThat( project.getDependencyArtifacts(), hasItem( hasArtifact( getArtifactName( groupId, "dep1", "jar" ) ) ) );
        assertThat( project.getDependencyArtifacts(), hasItem( hasArtifact( getArtifactName( groupId, "dep2", "zip" ) ) ) );
        assertThat( project.getDependencyArtifacts(), hasItem( hasArtifact( getArtifactName( groupId, "dep4", "jar" ) ) ) );
    }

    @Test
    public void addsResourcesToClasspathWithDuplicateNameAndParentFolderName() throws Exception{
        File basedir = new File( getBasedir(), "target/test-classes/unit/add-duplicate-dependencies" );
        MavenProject project = mojoRule.readMavenProject( basedir );
        // set by org.apache.maven.lifecycle.internal.LifecycleDependencyResolver
        project.setDependencyArtifacts( new LinkedHashSet<Artifact>() );

        mojoRule.executeMojo( project, "process" );

        assertThat( project.getDependencyArtifacts(), hasSize( 2 ) );

        File libsFolder = new File( basedir, "libs" );

        File otherDependencyParent = new File( new File( libsFolder, "other" ), "dup" );
        assertThat( project.getDependencyArtifacts(), hasItem( hasArtifact( getArtifactName( getGroupId( otherDependencyParent.getAbsolutePath() ), "dep", "jar" ) ) ) );

        File pluginDependencyParent = new File(new File(libsFolder, "plugins"), "dup");
        assertThat( project.getDependencyArtifacts(), hasItem( hasArtifact( getArtifactName( getGroupId( pluginDependencyParent.getAbsolutePath() ), "dep", "jar" ) ) ) );
    }


    private String getGroupId( String absolutePathOfParentDir ) {
        return "local.dependency" + absolutePathOfParentDir.replace( File.separatorChar, '.' );
    }

    private String getArtifactName( String groupId, String artifactId, String packaging ) {
        return groupId + ":" + artifactId + ":" + packaging + ":" + VERSION + ":" + SCOPE;
    }

    public Matcher<Artifact> hasArtifact( String path ){
        return hasToString( path );
    }

}

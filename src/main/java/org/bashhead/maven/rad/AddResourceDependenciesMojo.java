package org.bashhead.maven.rad;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.model.FileSet;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;

/**
 * Mojo for adding jars from FileSet as system scoped dependencies
 */
@Mojo(
        name = "process",
        defaultPhase = LifecyclePhase.PROCESS_RESOURCES,
        threadSafe = true,
        requiresDependencyResolution = ResolutionScope.COMPILE
        )
public class AddResourceDependenciesMojo extends AbstractMojo
{

    private static final String[] EMPTY_STRING_ARRAY = {};

    /**
     * The filesets used to find additonal classpath elements
     */
    @Parameter( required = true )
    private FileSet[] fileSets;

    @Parameter( defaultValue = "${project}", readonly = true, required = true )
    private MavenProject project;

    /**
     * {@inheritDoc}
     */
    public void execute()
    {
        List<Artifact> dependencies = new ArrayList<Artifact>();
        for ( FileSet r : fileSets )
        {
            getLog().info( "Adding resource as dependencies: " + r );
            for ( File file : getIncludedFiles( r ) )
            {
                getLog().debug( "Adding resource dependency: " + file );
                dependencies.add( getDependency( file ) );
            }
        }
        project.getDependencyArtifacts().addAll( dependencies );
    }

    private List<File> getIncludedFiles( FileSet resource )
    {
        DirectoryScanner scanner = getScanner( resource );
        scanner.scan();
        return getIncludedFiles( scanner );
    }

    private List<File> getIncludedFiles( DirectoryScanner scanner )
    {
        List<File> files = new ArrayList<File>();
        for ( String fileName : scanner.getIncludedFiles() )
        {
            files.add( new File( scanner.getBasedir(), fileName ) );
        }
        return files;
    }

    private DirectoryScanner getScanner( FileSet resource )
    {
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir( resource.getDirectory() );
        scanner.setExcludes( resource.getExcludes().toArray( EMPTY_STRING_ARRAY ) );
        scanner.setIncludes( resource.getIncludes().toArray( EMPTY_STRING_ARRAY ) );
        return scanner;
    }

    private Artifact getDependency( File file )
    {
        DefaultArtifactHandler h = new DefaultArtifactHandler();
        h.setAddedToClasspath( true );
        h.setIncludesDependencies( false );

        List<String> fileExtension = fileExtension( file );
        DefaultArtifact d = new DefaultArtifact(
                "local.dependency." + file.getParentFile().getName(),
                fileExtension.get( 0 ),
                "0.0.1",
                Artifact.SCOPE_SYSTEM, fileExtension.get( 1 ), null,
                h );
        d.setFile( file );
        d.setOptional( true );
        return d;
    }

    private List<String> fileExtension( File file )
    {
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf( '.' );
        if ( dotIndex == -1 )
        {
            return Arrays.asList( fileName, "jar" );
        }

        return Arrays.asList( fileName.substring( 0, dotIndex ), fileName.substring( dotIndex + 1 ) );
    }

}

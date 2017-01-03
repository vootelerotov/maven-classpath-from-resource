package org.bashhead.maven.rad;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.model.FileSet;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;

@Mojo(name = "add-resource-dependencies", threadSafe = true)
public class AddResourceDependenciesMojo extends AbstractMojo {

  private static final String[] EMPTY_STRING_ARRAY = {};

  @Parameter(required = true)
  private FileSet[] resources;

  @Parameter(defaultValue = "${project}", readonly = true, required = true)
  private MavenProject project;

  public void execute() throws MojoExecutionException {
    List<Artifact> dependencies = new ArrayList<Artifact>();
    for (FileSet r : resources) {
      getLog().info("Adding resource as dependencies: " + r);
      for (File file : getIncludedFiles(r)) {
        getLog().debug("Adding resource dependency: " + file);
        dependencies.add(getDependency(file));
      }
    }
    project.getDependencyArtifacts().addAll(dependencies);
  }

  private List<File> getIncludedFiles(FileSet resource) {
    DirectoryScanner scanner = getScanner(resource);
    scanner.scan();
    return getIncludedFiles(scanner);
  }

  private List<File> getIncludedFiles(DirectoryScanner scanner) {
    List<File> files = new ArrayList<File>();
    for (String fileName : scanner.getIncludedFiles()) {
      files.add(new File(scanner.getBasedir(), fileName));
    }
    return files;
  }

  private DirectoryScanner getScanner(FileSet resource) {
    DirectoryScanner scanner = new DirectoryScanner();
    scanner.setBasedir(resource.getDirectory());
    scanner.setExcludes(resource.getExcludes().toArray(EMPTY_STRING_ARRAY));
    scanner.setIncludes(resource.getIncludes().toArray(EMPTY_STRING_ARRAY));
    return scanner;
  }

  private Artifact getDependency(File file) {
    DefaultArtifactHandler h = new DefaultArtifactHandler();
    h.setAddedToClasspath(true);

    DefaultArtifact d = new DefaultArtifact(
        "local.dependency-" + file.getParentFile().getName(), file.getName(), "1.0",
        "system", "jar", null,
        h);
    d.setFile(file);
    return d;
  }
}

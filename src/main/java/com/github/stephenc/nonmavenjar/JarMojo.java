/*
 * Copyright 2013 Stephen Connolly.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.stephenc.nonmavenjar;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

/**
 * A helper mojo for when you have 3rd party non-maven jars to integrate into a maven project.
 *
 * @author stephenc
 * @since 1.0
 */
@Mojo(name = "jar",
        aggregator = false,
        defaultPhase = LifecyclePhase.COMPILE, // needs to be compile to lest the test phase work
        requiresProject = true,
        threadSafe = true)
public class JarMojo extends AbstractMojo {

    /**
     * The non-maven jar file.
     */
    @Parameter(property = "non-maven-jar.file", defaultValue = "${basedir}/src")
    private File jarFile;

    @Component
    private MavenProject project;

    @Component
    private MavenProjectHelper projectHelper;

    public void execute() throws MojoExecutionException, MojoFailureException {
        File artifact;
        if (jarFile.isDirectory()) {
            File[] files = jarFile.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".jar");
                }
            });
            if (files.length == 1) {
                getLog().info("Found exactly one .jar file in " + jarFile + " -> " + files[0].getName());
                artifact = files[0];
            } else if (files.length == 0) {
                throw new MojoExecutionException(
                        "Expected exactly one .jar file in the " + jarFile + " directory, found none");
            } else {
                throw new MojoExecutionException(
                        "Expected exactly one .jar file in the " + jarFile + " directory, found " + files.length + ": "
                                + Arrays.asList(files));
            }
        } else if (jarFile.isFile()) {
            artifact = jarFile;
        } else {
            throw new MojoExecutionException("Expected either a single .jar file in the " + jarFile
                    + " directory or else that the 'jarFile' parameter would point to a jar file");
        }
        projectHelper.attachArtifact(project, "jar", artifact);
    }
}

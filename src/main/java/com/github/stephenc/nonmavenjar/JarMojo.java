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
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
     * The non-maven jar file or the directory to find the file in.
     */
    @Parameter(property = "non-maven-jar.file", defaultValue = "${basedir}/src")
    private File jarFile;

    /**
     * The classifiers to look for secondary artifacts to attach to the build.
     * Defaults to <code>javadoc, sources</code>.
     * Where there are multiple candidates for {@link #jarFile} the one that matches the most secondary artifacts
     * will win.
     */
    @Parameter
    private String[] classifiers;

    @Component
    private MavenProject project;

    @Component
    private MavenProjectHelper projectHelper;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (classifiers == null) {
            classifiers = new String[]{"javadoc", "sources"};
        }
        Map<String, File> selection;
        if (jarFile.isDirectory()) {
            File[] files = jarFile.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".jar");
                }
            });
            List<Map<String, File>> selections = new ArrayList<Map<String, File>>();
            for (File file : files) {
                selections.add(matchArtifacts(file));
            }
            Collections.sort(selections, new Comparator<Map<String, File>>() {
                public int compare(Map<String, File> o1, Map<String, File> o2) {
                    long diff = ((long) o2.size()) - o1.size();
                    return diff < 0 ? -1 : diff == 0 ? 0 : +1;
                }
            });
            if (selections.size() > 1) {
                // if more than one selection, remove all the less optimal selections
                Iterator<Map<String, File>> i = selections.iterator();
                int bestSize = i.next().size();
                while (i.hasNext()) {
                    if (i.next().size() < bestSize) {
                        i.remove();
                    }
                }
            }
            if (selections.size() == 1) {
                selection = selections.get(0);
                getLog().info("Found unique best match in " + jarFile);
                logSelection(selection);
            } else if (selections.isEmpty()) {
                throw new MojoExecutionException(
                        "Expected exactly one main .jar file in the " + jarFile + " directory, found none");
            } else {
                throw new MojoExecutionException(
                        "Expected exactly one best match in the " + jarFile + " directory, found " + selections.size()
                                + ":\n"
                                + formatSelections(selections));
            }
        } else if (jarFile.isFile()) {
            selection = matchArtifacts(jarFile);
        } else {
            throw new MojoExecutionException("Expected either a single .jar file in the " + jarFile
                    + " directory or else that the 'jarFile' parameter would point to a jar file");
        }
        for (Map.Entry<String, File> entry : selection.entrySet()) {
            if (StringUtils.isEmpty(entry.getKey())) {
                projectHelper.attachArtifact(project, "jar", entry.getValue());
            } else {
                projectHelper.attachArtifact(project, "jar", entry.getKey(), entry.getValue());
            }
        }
    }

    private Map<String, File> matchArtifacts(File mainFile) {
        Map<String, File> selection = new TreeMap<String, File>();
        if (mainFile.isFile()) {
            selection.put("", mainFile);
            for (String classifier : classifiers) {
                String filename = mainFile.getName();
                File sideFile = new File(mainFile.getParentFile(),
                        FileUtils.basename(filename, "." + FileUtils.extension(filename))
                                + "-" + classifier + "."
                                + FileUtils.extension(filename));
                if (sideFile.isFile()) {
                    selection.put(classifier, sideFile);
                }
            }
        }
        return selection;
    }

    private void logSelection(Map<String, File> selection) {
        getLog().info("Primary artifact: " + selection.get(""));
        for (Map.Entry<String, File> entry : selection.entrySet()) {
            if (StringUtils.isBlank(entry.getKey())) {
                continue;
            }
            getLog().info("Secondary artifact, classifier " + entry.getKey() + ": " + entry.getValue());
        }
    }

    private String formatSelection(Map<String, File> selection) {
        StringBuilder buf = new StringBuilder();
        buf.append("Primary artifact: ").append(selection.get(""));
        for (Map.Entry<String, File> entry : selection.entrySet()) {
            if (StringUtils.isBlank(entry.getKey())) {
                continue;
            }
            buf.append("\nSecondary artifact, classifier ")
                    .append(entry.getKey())
                    .append(": ")
                    .append(entry.getValue());
        }
        return buf.toString();
    }

    private String formatSelections(List<Map<String, File>> selections) {
        StringBuilder buf = new StringBuilder();
        for (Map<String, File> selection : selections) {
            buf.append("- ").append(formatSelection(selection).replace("\n", "\n  ")).append("\n");
        }
        return buf.toString();
    }
}

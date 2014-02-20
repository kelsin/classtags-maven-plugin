package net.kelsin;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.lang.String;
import java.util.List;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

/**
 * Mojo which generates a .classtags file full of all classes in classpath
 */
@Mojo(name = "generate", requiresDependencyResolution = ResolutionScope.TEST)
public class ClassTags extends AbstractMojo {
	@Parameter(property = "project", readonly = true, required = true)
	private MavenProject project;

	public void execute() throws MojoExecutionException {
		getLog().info("Generating Classtags");

		List<String> elements;

		try {
			elements = project.getRuntimeClasspathElements();

			getLog().info("Found " + elements.size() + " Runtime Elements");
			for(String element: elements) {
				getLog().info(element);
			}

			elements = project.getCompileClasspathElements();

			getLog().info("Found " + elements.size() + " Compile Elements");
			for(String element: elements) {
				getLog().info(element);
			}

			elements = project.getTestClasspathElements();

			getLog().info("Found " + elements.size() + " Test Elements");
			for(String element: elements) {
				getLog().info(element);
			}

			elements = project.getSystemClasspathElements();

			getLog().info("Found " + elements.size() + " System Elements");
			for(String element: elements) {
				getLog().info(element);
			}
		} catch (DependencyResolutionRequiredException e) {
			getLog().error("Couldn't get dependencies");
		}

		getLog().info(System.getProperty("java.ext.dirs"));
		getLog().info(System.getProperty("sun.boot.class.path"));
	}
}

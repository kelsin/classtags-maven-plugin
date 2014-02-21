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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.String;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.kelsin.Klass;
import net.kelsin.Source;

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
	public static final PathMatcher JARMATCHER = FileSystems.getDefault().getPathMatcher("glob:*.jar");
	public static final PathMatcher CLASSMATCHER = FileSystems.getDefault().getPathMatcher("glob:*.class");
	public static final String TAG_FILE = ".classtags";

	@Parameter(property = "project", readonly = true, required = true)
	private MavenProject project;

	public void execute() throws MojoExecutionException {
		getLog().info("Generating Classtags");
		Set<Klass> classes = new HashSet();

		processElements(classes, collectMavenClasspathElements(), Source.MAVEN);
		processElements(classes, collectBootClasspathElements(), Source.BOOT);
		processElements(classes, collectExtensionClasspathElements(), Source.EXTENSION);

		// Sort and output the classes
		List<Klass> sortedKlasses = new ArrayList(classes);
		Collections.sort(sortedKlasses);
		File tags = new File(project.getBasedir(), TAG_FILE);
		try {
			PrintWriter writer = new PrintWriter(tags, "UTF-8");
			for(Klass klass: sortedKlasses) {
				writer.println(klass.toString());
			}
			writer.close();
		} catch (IOException exception) {
			getLog().error("Error writing tags file");
		}
	}

	public void processElements(Set<Klass> classes, Set<String> elements, Source source) {
		for(String element: elements) {
			File elementFile = new File(element);
			if(elementFile.exists()) {
				if(elementFile.isDirectory()) {
					classes.addAll(processDirectory(elementFile, source));
				} else {
					classes.addAll(processJar(elementFile, source));
				}
			}
		}
	}

	public Set<String> collectClasspathElements() {
		Set<String> elements = new HashSet();

		elements.addAll(collectMavenClasspathElements());
		elements.addAll(collectBootClasspathElements());
		elements.addAll(collectExtensionClasspathElements());

		getLog().info("Found " + elements.size() + " Total Classpath Elements");
		return elements;
	}

	public Set<String> collectMavenClasspathElements() {
		Set<String> elements = new HashSet();

		try {
			elements.addAll(project.getTestClasspathElements());

		} catch (DependencyResolutionRequiredException e) {
			getLog().error("Couldn't get dependencies from MavenProject!");
		}

		getLog().info("Found " + elements.size() + " Maven Classpath Elements");
		return elements;
	}

	public Set<String> collectBootClasspathElements() {
		Set<String> elements = new HashSet();
		elements.addAll(Arrays.asList(System.getProperty("sun.boot.class.path").split(";")));

		getLog().info("Found " + elements.size() + " Boot Classpath Elements");
		return elements;
	}

	public Set<String> collectExtensionClasspathElements() {
		Set<String> elements = new HashSet();

		String[] dirs = System.getProperty("java.ext.dirs").split(";");
		for(String dir: dirs) {
			File dirFile = new File(dir);
			if(dirFile.exists() && dirFile.isDirectory()) {
				File[] files = dirFile.listFiles();
				for(File file: files) {
					Path path = FileSystems.getDefault().getPath(file.getName());
					if(JARMATCHER.matches(path)) {
						elements.add(file.getAbsolutePath());
					}
				}
			}
		}

		getLog().info("Found " + elements.size() + " Extension Classpath Elements");
		return elements;
	}

	public Set<Klass> processDirectory(File dir, Source source) {
		getLog().info("Scanning: " + dir.getAbsolutePath());
		return processDirectory(dir, dir, source);
	}

	public Set<Klass> processDirectory(File dir, File root, Source source) {
		Set<Klass> classes = new HashSet();

		if(dir.exists() && dir.isDirectory()) {
			File[] files = dir.listFiles();
			for(File file: files) {
				if(file.exists()) {
					if(file.isDirectory()) {
						classes.addAll(processDirectory(file, root, source));
					} else {
						Path path = FileSystems.getDefault().getPath(file.getName());
						if(CLASSMATCHER.matches(path)) {
							String name = processClassFile(file, root);
							if(name != null) {
								addKlass(classes, name, source);
							}
						}
					}
				}
			}
		}
		return classes;
	}

	/**
	 * Take a path like "net/kelsin/ClassTags.class" and convert to
	 * "net.kelsin.ClassTags"
	 *
	 * @param name The relative path of the class to work with
	 */
	public String processClassNameFromPath(String name) {
		StringBuilder className = new StringBuilder();
		for(String part: name.split("[/\\" + File.separator + "]")) {
			if(className.length() != 0) {
				className.append('.');
			}
			className.append(part);
			if(part.endsWith(".class")) {
				className.setLength(className.length()-".class".length());
			}
		}
		return className.toString();
	}

	public String processClassFile(File file, File root) {
		if(file.exists() && file.isFile()) {
			String relative = file.getAbsolutePath().substring(root.getAbsolutePath().length());
			return processClassNameFromPath(relative);
		} else {
			return null;
		}
	}

	public Set<Klass> processJar(File jar, Source source) {
		Set<Klass> classes = new HashSet();

		getLog().info("Processing: " + jar.getName());
		try {
			ZipInputStream zip=new ZipInputStream(new FileInputStream(jar));
			for(ZipEntry entry=zip.getNextEntry();entry!=null;entry=zip.getNextEntry()) {
				if(entry.getName().endsWith(".class") && !entry.isDirectory()) {
					String name = processClassNameFromPath(entry.getName());
					addKlass(classes, name, source);
				}
			}
		} catch (IOException e) {
			getLog().error("Error when processing jar", e);
		}

		return classes;
	}

	/**
	 * Create a Klass object from a name and add it to a collection but only if
	 * it's not a inner class.
	 *
	 * @param collection The collection to add to
	 * @param name The full name of the class to add
	 * @param source Where this class is from
	 */
	public void addKlass(Collection<Klass> collection, String name, Source source) {
		// Don't add any inner classes for now
		if(!name.contains("$")) {
			try {
				Klass klass = Klass.fromName(name, source);
				collection.add(klass);
			} catch (IllegalArgumentException e) {
				// Don't bother with extra messages, most of these are weird
				// meta classes that we don't care about
				// getLog().info("Bad class name: " + name);
			}
		}
	}
}

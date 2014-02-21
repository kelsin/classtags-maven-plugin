package net.kelsin;

import java.lang.IllegalArgumentException;
import java.lang.String;
import java.lang.StringBuilder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.kelsin.Source;

public class Klass implements Comparable<Klass> {
	private static final Pattern CLASSNAME = Pattern.compile("^([A-Za-z0-9-_\\.]+)\\.([A-Z][A-Za-z0-9-_]+)$");

	private final String name;
	private final String pkg;
	private final Source source;

	public static Klass fromName(final String fullname, final Source source) {
		Matcher m = CLASSNAME.matcher(fullname);
		if(m.find()) {
			return new Klass(m.group(2), m.group(1), source);
		} else {
			throw new IllegalArgumentException("Must be a valid java full class name: " + fullname);
		}
	}

	public Klass(final String name, final String pkg, final Source source) {
		this.name = name;
		this.pkg = pkg;
		this.source = source;
	}

	public Klass(final String fullname, final Source source) {
		Matcher m = CLASSNAME.matcher(fullname);
		if(m.find()) {
			this.name = m.group(2);
			this.pkg = m.group(1);
		} else {
			throw new IllegalArgumentException("Must be a valid java full class name: " + fullname);
		}

		this.source = source;
	}

	public String getName() {
		return name;
	}

	public boolean equals(Object o) {
		if (o == null) return false;
		if (o == this) return true;
		if (!(o instanceof Klass)) return false;
		Klass other = (Klass)o;
		return this.name.equals(other.name);
	}

	public int hashCode() {
		return this.name.hashCode();
	}

	public int compareTo(Klass o) {
		return this.name.compareTo(o.name);
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.name);
		builder.append(':');
		builder.append(this.pkg);
		builder.append(':');
		builder.append(this.source.toString());
		return builder.toString();
	}
}

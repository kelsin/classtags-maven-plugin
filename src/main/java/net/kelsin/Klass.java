package net.kelsin;

import java.lang.String;

public class Klass implements Comparable<Klass> {
	private final String name;

	public Klass(final String name) {
		this.name = name;
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
}

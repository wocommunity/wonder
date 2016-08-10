package er.ajax.mootools.example.components;

/**
 * Just some example data.
 */

public class Word {
	public String name;
	public int value;

	public Word(String name) {
		this.name = name;
		value = name.length();
	}

	@Override
	public String toString() {
		return "<" + name + ": " + value + ">";
	}
}
package er.ajax.example;

/**
 * Just some example data.
 */

public class Word {
	public String name;
	public int value;

	public Word(String name) {
		this.name = name;
		this.value = name.length();
	}

	public String toString() {
		return "<" + name + ": " + value + ">";
	}
}
package er.ajax.example;

public class Comment {
	private String _text;

	public Comment() {
	}

	public void setText(String text) {
		_text = text;
	}

	public String text() {
		return _text;
	}
}

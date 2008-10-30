package er.rest;

public interface IERXResponseWriter {
	public void setHeader(String value, String key);

	public void appendContentCharacter(char _ch);

	public void appendContentString(String _str);
}

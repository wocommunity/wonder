package er.rest.format;

public interface IERXRestResponse {
	public void setHeader(String value, String key);

	public void appendContentCharacter(char _ch);

	public void appendContentString(String _str);
}

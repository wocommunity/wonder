package er.neo4jadaptor.utils.cursor;

import java.util.Iterator;

public interface Cursor<T> extends Iterator<T> {
	public void close();
	
	
}

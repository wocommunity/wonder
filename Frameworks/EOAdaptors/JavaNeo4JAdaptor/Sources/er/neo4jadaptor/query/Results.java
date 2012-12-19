package er.neo4jadaptor.query;

import org.neo4j.graphdb.PropertyContainer;

import er.neo4jadaptor.utils.cursor.Cursor;



/**
 * Provides iterator for the search results.
 * 
 * @author Jedrzej Sobanski
 *
 * @param <T>
 */
public interface Results <T extends PropertyContainer> {
	public Cursor<T> iterator();
}

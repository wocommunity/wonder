package er.neo4jadaptor.query;

import org.neo4j.graphdb.PropertyContainer;

import er.neo4jadaptor.utils.cursor.Cursor;



/**
 * Marker interface for the search results cursor.
 * 
 * @author Jedrzej Sobanski
 *
 * @param <T>
 */
public interface Results <T extends PropertyContainer> extends Cursor<T> {
}

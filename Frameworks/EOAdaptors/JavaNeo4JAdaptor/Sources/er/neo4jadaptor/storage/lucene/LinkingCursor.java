package er.neo4jadaptor.storage.lucene;

import org.neo4j.graphdb.PropertyContainer;

import com.webobjects.eoaccess.EOEntity;

import er.neo4jadaptor.ersatz.neo4j.Neo4JErsatz;
import er.neo4jadaptor.utils.cursor.Cursor;

/**
 * {@link er.neo4jadaptor.utils.cursor.Cursor} that for another cursor of {@link org.neo4j.graphdb.PropertyContainer}s
 * from some entity return ersatz objects for the stored records. 
 * 
 * @author Jedrzej Sobanski
 */
public class LinkingCursor implements Cursor<Neo4JErsatz> {
	private final EOEntity entity;
	
	private Cursor<? extends PropertyContainer> cursor;
	
	public LinkingCursor(Cursor<? extends PropertyContainer> it, EOEntity entity) {
		this.entity = entity;
		cursor = it;
	}

	private Neo4JErsatz asUltimate(PropertyContainer n) {
		return Neo4JErsatz.create(entity, n);
	}
	
	public Neo4JErsatz next() {
		PropertyContainer candidate = cursor.next();
			
		return asUltimate(candidate);
	}

	public boolean hasNext() {
		return cursor.hasNext();
	}
	
	public void remove() {
		throw new UnsupportedOperationException();
	}

	public void close() {
		cursor.close();
	}
}

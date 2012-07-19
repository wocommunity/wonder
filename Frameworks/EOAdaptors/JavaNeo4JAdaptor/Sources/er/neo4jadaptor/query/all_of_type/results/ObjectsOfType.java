package er.neo4jadaptor.query.all_of_type.results;

import java.util.Iterator;

import org.apache.lucene.search.Query;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.index.Index;

import com.webobjects.eoaccess.EOEntity;

import er.neo4jadaptor.query.Results;
import er.neo4jadaptor.query.lucene.LuceneQueryConverter;
import er.neo4jadaptor.utils.cursor.Cursor;
import er.neo4jadaptor.utils.cursor.IteratorCursor;

public class ObjectsOfType <Type extends PropertyContainer> implements Results<Type> {
	@SuppressWarnings("unused")
	private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ObjectsOfType.class);
	
	private final Index<Type> typeIndex;
	private final EOEntity entity;
	
	public ObjectsOfType(Index<Type> nodeIndex, EOEntity entity) {
		this.typeIndex = nodeIndex;
		this.entity = entity;
	}

	public Cursor<Type> iterator() {
		Query q = LuceneQueryConverter.matchAllOfEntity(entity);
		Iterator<Type> it = typeIndex.query(q).iterator();
		
		return new IteratorCursor<Type>(it);
	}
}

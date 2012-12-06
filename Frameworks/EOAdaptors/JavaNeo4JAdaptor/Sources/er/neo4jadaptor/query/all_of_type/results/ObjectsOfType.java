package er.neo4jadaptor.query.all_of_type.results;

import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.index.Index;

import com.webobjects.eoaccess.EOEntity;

import er.neo4jadaptor.query.Results;
import er.neo4jadaptor.query.lucene.LuceneQueryConverter;
import er.neo4jadaptor.utils.cursor.IteratorCursor;

public class ObjectsOfType <Type extends PropertyContainer> extends IteratorCursor<Type> implements Results<Type> {
	@SuppressWarnings("unused")
	private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ObjectsOfType.class);
	
	public ObjectsOfType(Index<Type> nodeIndex, EOEntity entity) {
		super(nodeIndex.query(LuceneQueryConverter.matchAllOfEntity(entity)).iterator());
	}
}

package er.neo4jadaptor.query.all_of_type;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.index.Index;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOQualifier;

import er.neo4jadaptor.query.Filter;
import er.neo4jadaptor.query.Results;
import er.neo4jadaptor.query.all_of_type.results.ObjectsOfType;
import er.neo4jadaptor.storage.IndexProvider;

/**
 * Filter that returns all the records that belong to some entity. It uses Lucene index for filtering.
 * 
 * @author Jedrzej Sobanski
 *
 * @param <Type>
 */
public class AllOfTypeFilter <Type extends PropertyContainer> extends Filter <Type> {

	@SuppressWarnings("unchecked")
	private Results<Type> allOfType(GraphDatabaseService db, EOEntity entity) {
		Index<Type> index = (Index<Type>) IndexProvider.instance.getIndexForEntity(db, entity);
		
		return new ObjectsOfType(index, entity);
	}
	
	@Override
	public Results<Type> doFilter(GraphDatabaseService db, EOEntity entity, EOQualifier qualifier) {
		if (qualifier == null) {
			return allOfType(db, entity);
		} else {
			return successor.doFilter(db, entity, qualifier);
		}
	}

}

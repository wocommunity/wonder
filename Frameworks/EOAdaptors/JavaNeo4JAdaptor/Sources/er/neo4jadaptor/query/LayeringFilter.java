package er.neo4jadaptor.query;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.PropertyContainer;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOQualifier;

import er.neo4jadaptor.query.all_of_type.AllOfTypeFilter;
import er.neo4jadaptor.query.lucene.LuceneFilter;
import er.neo4jadaptor.query.neo4j_by_pk.ByPrimaryKeyFilter;

/**
 * <p>
 * Filter that uses various types of {@link er.neo4jadaptor.query.Filter}s and their layering to achieve
 * results exactly matching search criteria.
 * </p>
 * 
 * <p>
 * </p>
 * 
 * @author Jedrzej Sobanski
 *
 * @param <T>
 */
public class LayeringFilter <T extends PropertyContainer> extends Filter<T> {
	private final Filter<T> firstLine;
	
	public LayeringFilter() {
		List<Filter<T>> filters = new ArrayList<Filter<T>>();
		
		filters.add(new AllOfTypeFilter<T>());
		filters.add(new ByPrimaryKeyFilter<T>());
		filters.add(new LuceneFilter<T>());
		
		for (int i=0; i<filters.size()-1; i++) {
			filters.get(i).setSuccessor(filters.get(i+1));
		}
		
		this.firstLine = filters.get(0);
	}
	
	@Override
	public Results<T> doFilter(GraphDatabaseService db, EOEntity entity, EOQualifier qualifier) {
		Results<T> ret = firstLine.doFilter(db, entity, qualifier);
		
		if (ret != null) {
			return ret;
		} else {
			// chain delegation (probably won't be used, but left here as formality for the chain of responsibility pattern
			return successor.doFilter(db, entity, qualifier);
		}
	}
}

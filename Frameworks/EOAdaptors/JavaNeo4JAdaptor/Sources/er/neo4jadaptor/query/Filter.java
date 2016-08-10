package er.neo4jadaptor.query;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.PropertyContainer;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOQualifier;

/**
 * Performs Neo4J node/relationship filtering to exclude ones that do not match search criteria.
 * The contract is that if a {@link PropertyContainer} is excluded from the {@link er.neo4jadaptor.query.Results} 
 * then for sure it didn't match search criterias, but if a node/relationship is returned in {@link er.neo4jadaptor.query.Results}
 * then it may or may not match the criteria.
 * 
 * @author Jedrzej Sobanski
 *
 * @param <T>
 */
public abstract class Filter<T extends PropertyContainer> {
	protected Filter<T> successor;
	
	/**
	 * Perform search.
	 * 
	 * @param db database to search in
	 * @param entity entity to search records from
	 * @param qualifier search criteria
	 * @return records that may match search criteria
	 */
	public abstract Results<T> doFilter(GraphDatabaseService db, EOEntity entity, EOQualifier qualifier);
	
	/**
	 * Set chain of responsibility pattern delegate. 
	 * 
	 * @param successor
	 */
	public void setSuccessor(Filter<T> successor) {
		this.successor = successor;
	}
}

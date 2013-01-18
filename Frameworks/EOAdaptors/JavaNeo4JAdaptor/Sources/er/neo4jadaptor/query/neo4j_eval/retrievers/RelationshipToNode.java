/**
 * 
 */
package er.neo4jadaptor.query.neo4j_eval.retrievers;

import java.util.Iterator;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import com.webobjects.eoaccess.EORelationship;

import er.neo4jadaptor.utils.iteration.Iterators;

/**
 * Retrieves EO relationship destination, where the source object is representing a record
 * from some join entity.
 * 
 * @author Jedrzej Sobanski
 *
 */
public class RelationshipToNode extends RelationshipRetriever<Relationship, Node> {
	private final String propertyName;
	
	public RelationshipToNode(EORelationship rel) {
		propertyName = rel.sourceAttributes().get(0).name();
	}
	
	public Iterator<Node> retrieve(Relationship rel) {
		long nodeId = ((Number) rel.getProperty(propertyName)).longValue();
		GraphDatabaseService db = rel.getGraphDatabase();
		
		return Iterators.singleton(db.getNodeById(nodeId));
	}
	
	@Override
	public String toString() {
		return "relationship-to-node through " + propertyName;
	}
}
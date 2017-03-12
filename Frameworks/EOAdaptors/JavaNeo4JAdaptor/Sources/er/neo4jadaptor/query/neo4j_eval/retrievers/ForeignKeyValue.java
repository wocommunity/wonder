package er.neo4jadaptor.query.neo4j_eval.retrievers;

import java.util.Iterator;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.foundation.NSArray;

import er.neo4jadaptor.ersatz.neo4j.Neo4JUtils;
import er.neo4jadaptor.query.neo4j_eval.Cost;
import er.neo4jadaptor.utils.EOUtilities;
import er.neo4jadaptor.utils.iteration.Iterators;

/**
 * Simulates getting foreign key value. Actually we don't store foreign key values in Neo4J so instead
 * we traverse relationship and return destination ID.
 * 
 * @author Jedrzej Sobanski
 *
 */
public class ForeignKeyValue implements Retriever<PropertyContainer, Number> {
	private final RelationshipType relationshipType;
	private final EOAttribute srcAttribute;
	
	public ForeignKeyValue(EORelationship rel) {
		relationshipType = Neo4JUtils.getRelationshipType(rel);
		
		NSArray<EOAttribute> srcAtts = rel.sourceAttributes();
		
		if (srcAtts.count() != 1) {
			throw new IllegalArgumentException();
		}
		srcAttribute = srcAtts.get(0);
	}

	private Iterator<Number> retrieveFromProperties(PropertyContainer container) {		
		Number n = (Number) container.getProperty(srcAttribute.name(), null);
		
		if (n != null) {
			return Iterators.singleton(n);
		} else {
			return Iterators.empty(); 
		}
	}
	
	private Iterator<Number> retrieveFromNodeRelationships(Node node) {
		Relationship r = node.getSingleRelationship(relationshipType, Direction.OUTGOING);
		
		if (r != null) {
			Long id =  r.getEndNode().getId();
			Number ret = EOUtilities.convertToAttributeType(srcAttribute, id);
			
			return Iterators.singleton(ret);
		} else {
			return Iterators.empty();
		}
	}
	
	public Cost getCost() {
		return Cost.RELATIONSHIPS;
	}
	
	public Iterator<Number> retrieve(PropertyContainer container) {
		if (container instanceof Relationship) {
			return retrieveFromProperties(container);
		} else {
			return retrieveFromNodeRelationships((Node) container);
		}
	}
	
	@Override
	public String toString() {
		return srcAttribute.name();
	}
}

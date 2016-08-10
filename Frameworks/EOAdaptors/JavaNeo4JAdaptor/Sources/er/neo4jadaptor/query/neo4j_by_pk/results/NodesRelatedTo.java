package er.neo4jadaptor.query.neo4j_by_pk.results;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import com.webobjects.eoaccess.EORelationship;

import er.neo4jadaptor.ersatz.neo4j.Neo4JUtils;
import er.neo4jadaptor.query.Results;
import er.neo4jadaptor.utils.cursor.Cursor;
import er.neo4jadaptor.utils.cursor.FlattenedCursor;
import er.neo4jadaptor.utils.cursor.IteratorCursor;

/**
 * Returns only nodes that are connected with some relationship to some well defined set of nodes.
 * 
 * @author Jedrzej Sobanski
 *
 */
public class NodesRelatedTo implements Results<Node> {
	final Cursor<Relationship> relationshipsIterator;
	
	public NodesRelatedTo(Results<Node> relatedTo, EORelationship rel) {
		final RelationshipType relationshipType = Neo4JUtils.getRelationshipType(rel);
		
		final Cursor<Node> nodeCursor = relatedTo;
		
		Cursor<Cursor<Relationship>> twoLevelIt = new Cursor<Cursor<Relationship>>() {
			public void remove() {
				nodeCursor.remove();
			}
			
			public Cursor<Relationship> next() {
				Node node = nodeCursor.next();
				Iterable<Relationship> relationships = node.getRelationships(Direction.INCOMING, relationshipType);
				
				return new IteratorCursor<Relationship>(relationships.iterator());
			}
			
			public boolean hasNext() {
				return nodeCursor.hasNext();
			}
			
			public void close() {
				nodeCursor.close();
			}
		};
		
		relationshipsIterator = new FlattenedCursor<Relationship>(twoLevelIt);
	}
	
	public boolean hasNext() {
		return relationshipsIterator.hasNext();
	}
	
	public void close() {
		relationshipsIterator.close();
	}

	public Node next() {
		return relationshipsIterator.next().getStartNode();
	}

	public void remove() {
		relationshipsIterator.remove();
	}
}

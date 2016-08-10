package er.neo4jadaptor.query.neo4j_by_pk.results;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;

import er.neo4jadaptor.query.Results;

/**
 * Returns nodes with specified IDs.
 * 
 * @author Jedrzej Sobanski
 *
 */
public class NodesWithIds implements Results<Node> {
	private final Iterator<Number> nodeIdsIt;
	private Node next;
	
	private final GraphDatabaseService db;
	
	public NodesWithIds(GraphDatabaseService db, Collection<? extends Number> nodeIds) {
		for (Number n : nodeIds) {
			if (n == null) {
				throw new IllegalArgumentException();
			}
		}
		
		nodeIdsIt = (Iterator<Number>) Collections.unmodifiableCollection(nodeIds).iterator();
		this.db = db;
	}
	
	private void calculateNext() {
		while (nodeIdsIt.hasNext()) {
			long id = nodeIdsIt.next().longValue();
			
			try {
				next = db.getNodeById(id);
				return;
			} catch (NotFoundException e) {
				// ignore
			}
		}
	}
	
	public void close() {
		// do nothing
	}
	
	public boolean hasNext() {
		if (next == null) {
			calculateNext();
		}
		return next != null;
	}

	public Node next() {
		Node ret = next;
		
		next = null;
		
		return ret;
	}

	public void remove() {
		nodeIdsIt.remove();
	}
}

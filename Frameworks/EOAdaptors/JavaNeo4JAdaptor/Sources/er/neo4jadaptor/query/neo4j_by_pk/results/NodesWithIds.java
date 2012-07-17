package er.neo4jadaptor.query.neo4j_by_pk.results;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;

import er.neo4jadaptor.query.Results;
import er.neo4jadaptor.utils.cursor.Cursor;

/**
 * Returns nodes with specified IDs.
 * 
 * @author Jedrzej Sobanski
 *
 */
public class NodesWithIds implements Results<Node> {
	@SuppressWarnings("unused")
	private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(NodesWithIds.class);

	private final Collection<Number> nodeIds;
	private final GraphDatabaseService db;
	
	public NodesWithIds(GraphDatabaseService db, Collection<? extends Number> nodeIds) {
		for (Number n : nodeIds) {
			if (n == null) {
				throw new IllegalArgumentException();
			}
		}
		
		this.nodeIds = Collections.unmodifiableCollection(nodeIds);
		this.db = db;
	}
	
	public Cursor<Node> iterator() {
		final Iterator<Number> it = nodeIds.iterator();
		
		return new Cursor<Node>() {
			Node next;
			
			private void calculateNext() {
				while (it.hasNext()) {
					long id = it.next().longValue();
					
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
				it.remove();
			}
		};
	}
}

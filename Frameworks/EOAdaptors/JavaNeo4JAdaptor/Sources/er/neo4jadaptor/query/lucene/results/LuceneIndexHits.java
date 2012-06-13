package er.neo4jadaptor.query.lucene.results;

import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.index.IndexHits;

import er.neo4jadaptor.query.Results;
import er.neo4jadaptor.utils.cursor.Cursor;
import er.neo4jadaptor.utils.cursor.IteratorCursor;


public class LuceneIndexHits <Type extends PropertyContainer> implements Results<Type> {
	private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LuceneIndexHits.class);
	
	private final IndexHits<Type> hits;
	
	public LuceneIndexHits(IndexHits<Type> hits) {
		this.hits = hits;
	}
	
	public Cursor<Type> iterator() {
		return new IteratorCursor<Type>(hits) {
			public void close() {
				if (log.isDebugEnabled()) {
					log.debug("Closing lucene hits");
				}
				hits.close();
			}
		};
	}
}

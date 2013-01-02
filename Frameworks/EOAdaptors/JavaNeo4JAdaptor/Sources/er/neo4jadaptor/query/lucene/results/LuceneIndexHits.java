package er.neo4jadaptor.query.lucene.results;

import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.index.IndexHits;

import er.neo4jadaptor.query.Results;
import er.neo4jadaptor.utils.cursor.IteratorCursor;


public class LuceneIndexHits <Type extends PropertyContainer> extends IteratorCursor<Type> implements Results<Type> {
	private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LuceneIndexHits.class);
	
	final IndexHits<Type> hits;
	
	public LuceneIndexHits(final IndexHits<Type> hits) {
		super(hits);
		
		this.hits = hits;
	}

	@Override
	public void close() {
		if (log.isDebugEnabled()) {
			log.debug("Closing lucene hits");
		}
		hits.close();
	}

}

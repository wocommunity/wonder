package er.neo4jadaptor.query.lucene.results;

import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.index.IndexHits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import er.neo4jadaptor.query.Results;
import er.neo4jadaptor.utils.cursor.IteratorCursor;


public class LuceneIndexHits <Type extends PropertyContainer> extends IteratorCursor<Type> implements Results<Type> {
	private static final Logger log = LoggerFactory.getLogger(LuceneIndexHits.class);
	
	final IndexHits<Type> hits;
	
	public LuceneIndexHits(final IndexHits<Type> hits) {
		super(hits);
		
		this.hits = hits;
	}

	@Override
	public void close() {
		log.debug("Closing lucene hits");
		hits.close();
	}

}

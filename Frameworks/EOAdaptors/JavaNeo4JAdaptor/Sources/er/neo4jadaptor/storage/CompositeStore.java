package er.neo4jadaptor.storage;

import org.neo4j.graphdb.PropertyContainer;

import com.webobjects.eocontrol.EOQualifier;

import er.neo4jadaptor.ersatz.Ersatz;
import er.neo4jadaptor.ersatz.neo4j.Neo4JErsatz;
import er.neo4jadaptor.storage.lucene.LuceneStore;
import er.neo4jadaptor.utils.cursor.Cursor;

/**
 * Store that utilizes one store for keeping the data and {@link er.neo4jadaptor.storage.lucene.LuceneStore}
 * for queries.
 * 
 * @author Jedrzej Sobanski
 *
 * @param <Type>
 */
public class CompositeStore <Type extends PropertyContainer> implements Store<Ersatz, Neo4JErsatz> {
	private final Store<Ersatz, Neo4JErsatz> neoStore;
	private final LuceneStore<Type> luceneStore;
	
	public CompositeStore(Store<Ersatz, Neo4JErsatz> neoStore, LuceneStore<Type> luceneStore) {
		this.neoStore = neoStore;
		this.luceneStore = luceneStore;
	}
	
	public Cursor<Neo4JErsatz> query(EOQualifier qualifier) {
		return luceneStore.query(qualifier);
	}
	
	public Neo4JErsatz insert(Ersatz row) {
		Neo4JErsatz newNeo = neoStore.insert(row);
		
		if (newNeo != null) {
			luceneStore.insert(newNeo);
		}
		
		return newNeo;
	}
	
	public void update(Ersatz newValues, Neo4JErsatz neoErsatz) {
		neoStore.update(newValues, neoErsatz);
		luceneStore.update(newValues, neoErsatz);
	}
	
	public void delete(Neo4JErsatz neoErsatz) {
		neoStore.delete(neoErsatz);
		luceneStore.delete(neoErsatz);
	}
	
	public Ersatz newPrimaryKey() {
		return neoStore.newPrimaryKey();
	}
}

package er.neo4jadaptor.storage.lucene;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.index.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOQualifier;

import er.neo4jadaptor.ersatz.Ersatz;
import er.neo4jadaptor.ersatz.lucene.LuceneErsatz;
import er.neo4jadaptor.ersatz.neo4j.Neo4JErsatz;
import er.neo4jadaptor.query.Filter;
import er.neo4jadaptor.query.LayeringFilter;
import er.neo4jadaptor.query.Results;
import er.neo4jadaptor.storage.IndexProvider;
import er.neo4jadaptor.storage.Store;
import er.neo4jadaptor.utils.cursor.Cursor;

/**
 * Store that maintains record representation in Lucene documents. Each EO attribute is stored as a separate
 * document field, plus there an extra property {@value #TYPE_PROPERTY_NAME} added for storing entity name.
 * 
 * @author Jedrzej Sobanski
 *
 * @param <Type>
 */
public class LuceneStore <Type extends PropertyContainer> implements Store<Neo4JErsatz, Neo4JErsatz> {
	private static final Logger log = LoggerFactory.getLogger(LuceneStore.class);

	public static final String TYPE_PROPERTY_NAME = "#_type";
	
	private final Index<Type> index;
	private final GraphDatabaseService db;
	private final EOEntity entity;
	private final Filter<Type> facetCreator;
	
	@SuppressWarnings("unchecked")
	public LuceneStore(GraphDatabaseService db, EOEntity entity) {
		this.index = (Index<Type>) IndexProvider.instance.getIndexForEntity(db, entity);
		this.db = db;
		this.entity = entity;
		this.facetCreator = new LayeringFilter<>();
	}

	// this method is not defined in Store interface as lucene objects can't exist
	// independent in Lucene for Neo4J (there must exist corresponding Node/Relationship).
	// Implementing Store would make this method accept Ersatz, which would cause a need
	// for a class cast, but this way we have our own method with very specific signature
	@SuppressWarnings("unchecked")
	public Neo4JErsatz insert(Neo4JErsatz row) {
		Type container = (Type) row.getPropertyContainer();
		LuceneErsatz<Type> lucene = LuceneErsatz.createForInsert(entity, container, index);
		
		Ersatz.copy(row, lucene);
		
		return row;
	}
	
	@SuppressWarnings("unchecked")
	public void update(Ersatz newValues, Neo4JErsatz neoErsatz) {
		Type container = (Type) neoErsatz.getPropertyContainer();
		LuceneErsatz<Type> lucene = LuceneErsatz.createForUpdate(entity, container, index);
		
		Ersatz.copy(newValues, lucene);
	}
	
	@SuppressWarnings("unchecked")
	public void delete(Neo4JErsatz neoErsatz) {
		Type container = (Type) neoErsatz.getPropertyContainer();
		LuceneErsatz<Type> lucene = LuceneErsatz.createForUpdate(entity, container, index);
		
		lucene.delete();
	}
	
	public Cursor<Neo4JErsatz> query(EOQualifier q) {
		Results<Type> result = facetCreator.doFilter(db, entity, q);
		
		log.debug("Fetching {} where {}.", entity.name(), q);
		
		return new LinkingCursor(result, entity);
	}
	
	public Ersatz newPrimaryKey() {
		throw new UnsupportedOperationException();
	}
}

package er.neo4jadaptor.storage.neo4j;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.neo4jadaptor.ersatz.Ersatz;
import er.neo4jadaptor.ersatz.neo4j.Neo4JErsatz;
import er.neo4jadaptor.ersatz.neo4j.Neo4JNodeErsatz;
import er.neo4jadaptor.ersatz.webobjects.NSDictionaryErsatz;
import er.neo4jadaptor.storage.Store;
import er.neo4jadaptor.utils.EOUtilities;
import er.neo4jadaptor.utils.cursor.Cursor;

/**
 * Storage for entities that use only one numerical EO attribute for primary key. Each row is represented as a node.
 * Each to-one EO relationship is represented as a single outgoing relationship. Foreign key values are not stored,
 * those are calculated based on existing relationships whenever needed. 
 * 
 * @author Jedrzej Sobanski
 */
public class NodeStore implements Store<Ersatz, Neo4JErsatz> {
	private static final Logger log = LoggerFactory.getLogger(NodeStore.class);

	private final GraphDatabaseService db;
	private final EOEntity entity;
	
	private final NodeSpaceManager spaceManager;
	
	private final EOAttribute pk;
	private final TemporaryNodePool tempNodePool;
	
	public NodeStore(
			GraphDatabaseService db, 
			EOEntity entity, 
			NodeSpaceManager spaceManager, 
			TemporaryNodePool tempNodePool) {
		validateEntity(entity);
		
		this.entity = entity;
		this.db = db;
		pk = EOUtilities.primaryKeyAttribute(entity);
		this.spaceManager = spaceManager;
		this.tempNodePool = tempNodePool;
	}
	
	public Neo4JErsatz insert(Ersatz row) {
		Object pkValue = row.get(pk);
		
		if (pkValue == null) {
			// it happens in case of inserts of rows with attributes like "NeededByEOF0" which seem not to be real rows.
			log.warn("Primary key value is null for {}. Ignoring it (not sure if it's correct behaviour)", row);
			return null;
		}
		if (pkValue != null && false == pkValue instanceof NodeNumber) {
			throw new IllegalStateException("Value for primary key " + entity.name() + "." + pk.name() + " was set manually. This adaptor only supports inserts where primary keys aren't set on insert time.");
		}
		
		long id = ((NodeNumber) pkValue).longValue();
		final Node node = db.getNodeById(id);
		
		if (spaceManager.isPermanent(node)) {
			throw new IllegalStateException("Node with id=" + id + " already exists in permanent space");
		} else {			
			spaceManager.setIsPermanent(node, entity);
		}
		
		Neo4JErsatz ret = new Neo4JNodeErsatz(entity, node);
		
		Ersatz.copy(row, ret);
		
		return ret;
	}
	
	public void update(Ersatz newValues, Neo4JErsatz neoErsatz) {
		Ersatz.copy(newValues, neoErsatz);
	}
	
	public void delete(Neo4JErsatz neoErsatz) {
		neoErsatz.delete();
	}
	
	public Ersatz newPrimaryKey() {
		Node node = tempNodePool.getNextTemporaryNode();
		Number id = node.getId();
		
		id = EOUtilities.convertToAttributeType(pk, id);
		
		NSMutableDictionary<EOAttribute, Object> dict = new NSMutableDictionary<EOAttribute, Object>(new NodeNumber(id), pk);
		
		return NSDictionaryErsatz.fromDictionary(dict);
	}
	
	public Cursor<Neo4JErsatz> query(EOQualifier q) {
		throw new UnsupportedOperationException();
	}
	
	
	

	protected static void validateEntity(EOEntity entity) throws UnsupportedEntityException {
		NSArray<EOAttribute> pks = entity.primaryKeyAttributes();
		EOAttribute pk;
		
		if (pks.size() != 1) {
			throw new UnsupportedEntityException("Entity " + entity.name() + " must have one primary key attribute");
		} else {
			pk = pks.get(0);
		}
		
		if (entity.classPropertyNames().contains(pk.name())) {
//			if (! pk.isReadOnly()) {
//				// primary key values are assigned node id values therefore shouldn't be possible to overwrite them
//				throw new UnsupportedEntityException("Primary key " + entity.name() + "." + pk.name() + " can only be read only when it's a class property");
//			}
			log.warn("Primary key {}.{} is class property. Its value shouldn't be set manually.", entity.name(), pk.name());
		}
		
		try {
			Class<?> valueType;
			
			
			// check primary key type
			valueType = Class.forName(pk.valueTypeClassName());
			if (! Integer.class.equals(valueType) && ! Long.class.equals(valueType)) {
				throw new UnsupportedEntityException("Primary key " + entity.name() + "." + pk.name() + " value type must be integer or long type");
			}
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	

	/**
	 * Only used to distinguish primary key values being result of a {@link #newPrimaryKey()} call from
	 * those where primary key value is set manually.
	 */
	public static final class NodeNumber extends Number {
		private final Number number;
		
		private NodeNumber(Number number) {
			this.number = number;
		}
		
		@Override
		public int intValue() {
			return number.intValue();
		}

		@Override
		public long longValue() {
			return number.longValue();
		}

		@Override
		public float floatValue() {
			return number.floatValue();
		}

		@Override
		public double doubleValue() {
			return number.doubleValue();
		}
		
		@Override
		public boolean equals(Object obj) {
			return number.equals(obj);
		}
		
		@Override
		public int hashCode() {
			return number.hashCode();
		}
		
		@Override
		public String toString() {
			return Long.toString(longValue());
		}
	}
	
}

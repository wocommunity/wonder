package er.neo4jadaptor.query.neo4j_eval.retrievers;

import java.util.Iterator;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.foundation.NSArray;

import er.neo4jadaptor.ersatz.neo4j.Neo4JTranslator;
import er.neo4jadaptor.query.neo4j_eval.Cost;
import er.neo4jadaptor.storage.neo4j.RelationshipStore;
import er.neo4jadaptor.utils.iteration.Iterators;

/**
 * Retrieves object ID.
 * 
 * @author Jedrzej Sobanski
 *
 * @param <T>
 */
public abstract class PrimaryKeyRetriever<T> implements Retriever<T, Number> {
	private final EOAttribute attribute;
	protected abstract long getId(T t);
	
	public PrimaryKeyRetriever(EOAttribute att) {
		this.attribute = att;
	}
	
	public Iterator<Number> retrieve(T t) {
		long id = getId(t);
		Number ret = (Number) Neo4JTranslator.instance.toNeutralValue(id, attribute);
		
		return Iterators.singleton(ret);
	}
	
	@Override
	public String toString() {
		return attribute.name();
	}
	
	public Cost getCost() {
		return Cost.PRIMARY_KEY;
	}
	
	public static PrimaryKeyRetriever<? extends PropertyContainer> create(EOEntity entity) {
		NSArray<EOAttribute> pks = entity.primaryKeyAttributes();
		EOAttribute pk = pks.get(0);
		
		if (RelationshipStore.shouldBeStoredAsRelationship(entity)) {
			return new PrimaryKeyRetriever<Relationship>(pk) {
				@Override
				protected long getId(Relationship t) {
					return t.getId();
				}
			};
		} else {
			return new PrimaryKeyRetriever<Node>(pk) {
				@Override
				protected long getId(Node t) {
					return t.getId();
				}
			};
		}
	}
}

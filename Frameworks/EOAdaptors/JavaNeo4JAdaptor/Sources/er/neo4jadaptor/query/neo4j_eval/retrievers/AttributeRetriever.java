package er.neo4jadaptor.query.neo4j_eval.retrievers;

import java.util.Iterator;

import org.neo4j.graphdb.PropertyContainer;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.foundation.NSArray;

import er.neo4jadaptor.ersatz.neo4j.Neo4JErsatz;
import er.neo4jadaptor.query.neo4j_eval.Cost;
import er.neo4jadaptor.utils.iteration.Iterators;

/**
 * Gets attribute value.
 * 
 * @author Jedrzej Sobanski
 *
 * @param <T>
 */
public class AttributeRetriever <T> implements Retriever<PropertyContainer, T> {
	private final String propertyName;
	private final EOAttribute attribute;
	
	public static Retriever<PropertyContainer, ?> create(EOEntity entity, EOAttribute attribute) {
		for (EORelationship r : entity.relationships()) {
			if (! r.isToMany()) {
				NSArray<EOAttribute> atts = r.sourceAttributes();
				
				if (atts.size() == 1 && atts.get(0).equals(attribute)) {
					// it's a foreign key
					return new ForeignKeyValue(r);
				}
			}
		}
		return new AttributeRetriever<>(attribute);
	}
	
	private AttributeRetriever(EOAttribute attribute) {
		this.propertyName = attribute.name();
		this.attribute = attribute;	
	}

	@SuppressWarnings("unchecked")
	public Iterator<T> retrieve(PropertyContainer pc) {
		T ret = (T) Neo4JErsatz.getAttribute(pc, attribute);
		
		return Iterators.singleton(ret);
	}

	@Override
	public String toString() {
		return propertyName;
	}

	public Cost getCost() {
		return Cost.PROPERTIES;
	}
}

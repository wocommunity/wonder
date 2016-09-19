package er.neo4jadaptor.ersatz.lucene;

import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.index.Index;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;

import er.neo4jadaptor.ersatz.Ersatz;
import er.neo4jadaptor.storage.lucene.LuceneStore;

/**
 * Ersatz representation based on lucene document.
 * 
 * @author Jedrzej Sobanski
 *
 * @param <Type>
 */
public class LuceneErsatz <Type extends PropertyContainer> extends Ersatz {
	private final Index<Type> index;
	private final EOEntity entity;
	private final Type obj;
	private final boolean removeBeforeAdding;
	
	public static <Type extends PropertyContainer> LuceneErsatz<Type> createForInsert(EOEntity entity, Type obj, Index<Type> index) {
		// store object entity name
		index.add(obj, LuceneStore.TYPE_PROPERTY_NAME, entity.name());
		
		return new LuceneErsatz<>(entity, obj, index, false);
	}
	
	public static <Type extends PropertyContainer> LuceneErsatz<Type> createForUpdate(EOEntity entity, Type obj, Index<Type> index) {
		return new LuceneErsatz<>(entity, obj, index, true);
	}
	
	private LuceneErsatz(EOEntity entity, Type obj, Index<Type> index, boolean removeBeforeAdding) {
		this.index = index;
		this.obj = obj;
		this.entity = entity;
		this.removeBeforeAdding = removeBeforeAdding;
	}

	@Override
	public Iterable<EOAttribute> attributes() {
		return entity.attributes();
	}

	@Override
	public Object get(EOAttribute att) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void put(EOAttribute att, Object value) {
		String propertyName = att.name();
		String luceneValue = LuceneTranslator.instance.fromNeutralValue(value, att);
		
		if (removeBeforeAdding) {
			remove(att);
		}
		
		index.add(obj, propertyName, luceneValue);
		
		if (isAdditionalyIndexedLowercase(att)) {
			String lowercaseProperty = lowercasePropertyName(propertyName);
			
			if (value == null) {
				index.add(obj, lowercaseProperty, luceneValue);
			} else {
				index.add(obj, lowercaseProperty, luceneValue.toLowerCase());
			}
		}
	}

	@Override
	public void remove(EOAttribute att) {
		String propertyName = att.name();
		
		index.remove(obj, propertyName);
		if (isAdditionalyIndexedLowercase(att)) {
			index.remove(obj, lowercasePropertyName(propertyName));
		}
	}

	@Override
	public void delete() {
		index.remove(obj);
	}
	

	public static boolean isAdditionalyIndexedLowercase(EOAttribute att) {
		return String.class.getCanonicalName().equals(att.valueTypeClassName());
	}

	public static String lowercasePropertyName(String propertyName) {
		return "#_lo_" + propertyName;
	}
	
}

package er.neo4jadaptor.ersatz.webobjects;

import java.util.Map;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.neo4jadaptor.ersatz.Ersatz;

/**
 * Ersatz storing record representation in a map, particularly intended to be use as a bridge between
 * EOF record snapshots represented on {@link NSDictionary} instances and {@link Ersatz} world.
 * 
 * @author Jedrzej Sobanski
 */
public class NSDictionaryErsatz extends Ersatz {
	private final Map<String, Object> md;
	private final NSArray<EOAttribute> attributes;
	
	/**
	 * Creates ersatz representing all record EO attributes from the given EOF snapshot. 
	 * 
	 * @param entity entity that the record represented by this ersatz comes from
	 * @param snapshot EOF record snapshot
	 * @return full ersatz
	 */
	public static NSDictionaryErsatz full(EOEntity entity, NSDictionary<String, Object> snapshot) {
		return new NSDictionaryErsatz(entity.attributes(), snapshot);
	}

	/**
	 * Creates ersatz representing partial value of a record from the given partial EOF snapshot. 
	 * 
	 * @param entity entity that the record represented by this ersatz comes from
	 * @param snapshot partial EOF record snapshot
	 * @return partial ersatz
	 */
	public static NSDictionaryErsatz partial(EOEntity entity, NSDictionary<String, Object> snapshot) {
		NSMutableArray<EOAttribute> atts = new NSMutableArray<>();
		
		for (EOAttribute att : entity.attributes()) {
			if (snapshot.containsKey(att.name())) {
				atts.add(att);
			}
		}
		return new NSDictionaryErsatz(atts, snapshot);
	}

	/**
	 * Creates ersatz based on values in the given dictionary.
	 * 
	 * @param dict record snapshot
	 * @return ersatz with values specified in the given dictionary
	 */
	public static NSDictionaryErsatz fromDictionary(NSDictionary<EOAttribute, Object> dict) {
		NSMutableDictionary<String, Object> md = new NSMutableDictionary<>();
		
		for (Map.Entry<EOAttribute, Object> e : dict.entrySet()) {
			md.put(e.getKey().name(), e.getValue());
		}
		
		return new NSDictionaryErsatz(dict.allKeys(), md);
	}
	
	private NSDictionaryErsatz(NSArray<EOAttribute> attributes, NSDictionary<String, Object> d) {
		md = d.mutableClone();
		this.attributes = attributes;
	}

	/**
	 * Creates EOF snapshot for the given ersatz.
	 * 
	 * @param ersatz ersatz to create EOF snapshot for
	 * @return EOF snapshot
	 */
	public static NSMutableDictionary<String, Object> toSnapshot(Ersatz ersatz) {
		NSMutableDictionary<String, Object> md = new NSMutableDictionary<>();
		
		for (EOAttribute att : ersatz.attributes()) { 
			Object val = ersatz.get(att);
			
			md.put(att.name(), NSTranslator.instance.fromNeutralValue(val, att));
		}
		
		return md;
	}

	@Override
	public Iterable<EOAttribute> attributes() {
		return attributes;
	}

	@Override
	public Object get(EOAttribute att) {
		Object nsValue = md.get(att.name());
		
		return NSTranslator.instance.toNeutralValue(nsValue, att);
	}

	@Override
	public void put(EOAttribute att, Object value) {
		Object nsValue = NSTranslator.instance.fromNeutralValue(value, att);
		
		md.put(att.name(), nsValue);
	}

	@Override
	public void remove(EOAttribute att) {
		md.remove(att.name());
	}

	/**
	 * This operation is unsupported as this ersatz implementation is not bound to any record store.
	 */
	@Override
	public void delete() {
		throw new UnsupportedOperationException();
	}
}

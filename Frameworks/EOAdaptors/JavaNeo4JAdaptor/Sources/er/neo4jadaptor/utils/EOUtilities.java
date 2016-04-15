package er.neo4jadaptor.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;

import er.neo4jadaptor.ersatz.Ersatz;
import er.neo4jadaptor.utils.cursor.Cursor;
import er.neo4jadaptor.utils.cursor.IteratorCursor;

public class EOUtilities {
	private static final String INTEGER_CLASS_NAME = Integer.class.getCanonicalName();
	
	public static Number convertToAttributeType(EOAttribute att, Number value) {
		if (att.valueTypeClassName().equals(INTEGER_CLASS_NAME)) {
			return value.intValue();
		} else {
			return value;
		}
	}
	
	private static final Pattern DOT_PATTERN = Pattern.compile("\\.");

	public static String unflattenedKey(EOEntity entity, String keypath) {
		EOEntity currentEntity = entity;
		List<String> splits = new ArrayList<String>();
		StringBuilder b = new StringBuilder();
		
		Collections.addAll(splits, DOT_PATTERN.split(keypath));
		
		for (int i=0; i<splits.size(); i++) {
			String key = splits.get(i);
			EORelationship rel = currentEntity.relationshipNamed(key);
			
			if (rel != null) {				
				if (rel.isFlattened()) {
					String [] newDefinition = DOT_PATTERN.split(rel.definition());
					
					// perform replacement
					splits.remove(i);
					
					for (int j=0; j<newDefinition.length; j++) {
						splits.add(i+j, newDefinition[j]);
					}
					
					// possibly we have replaced flat relationship with another flat one, 
					// let's have a look at it again then
					i--;
				}
				currentEntity = rel.destinationEntity();
			}
		}
		for (int i=0; i<splits.size(); i++) {
			if (i > 0) {
				b.append('.');
			}
			b.append(splits.get(i));
		}
		return b.toString();
	}

	public static EOAttribute primaryKeyAttribute(EOEntity entity) {
		NSArray<EOAttribute> pks = entity.primaryKeyAttributes();
		
		if (pks.size() > 1) {
			throw new IllegalArgumentException("Compound primary keys are not supported");
		} else {
			return pks.get(0);
		}
	}
	
	public static EORelationship getRelationshipForSourceAttribute(EOEntity entity, EOAttribute att) {
		for (EORelationship r : entity.relationships()) {
			NSArray<EOAttribute> srcAtts = r.sourceAttributes();
			
			if (srcAtts.size() == 1 && srcAtts.get(0).equals(att)) {
				return r;
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static Cursor<Ersatz> sort(final Cursor<? extends Ersatz> c, EOEntity entity, NSArray<EOSortOrdering> sortOrderings) {
		for (EOSortOrdering so : sortOrderings) {
			if (entity.attributeNamed(so.key()) == null) {
				throw new IllegalArgumentException("Only sorting by attribute is supported");
			}
		}
		
		List<Ersatz> list = new ArrayList<Ersatz>();
		
		while (c.hasNext()) {
			list.add(c.next());
		}
		
		Collections.sort(list, new SortingComparator(entity, sortOrderings));
		
		Iterator<Ersatz> it = list.iterator();
		
		return new IteratorCursor<Ersatz>(it) {
			@Override
			public void close() {
				c.close();
			};
		};
	}
}

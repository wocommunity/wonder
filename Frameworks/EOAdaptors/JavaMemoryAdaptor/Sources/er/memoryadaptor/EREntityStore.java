package er.memoryadaptor;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOGeneralAdaptorException;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSRange;

import er.extensions.eof.ERXFetchSpecification;
import er.extensions.eof.ERXQ;
import er.extensions.foundation.ERXStringUtilities;

/**
 * EREntityStore is an abstract datastore implementation for a single "table"
 * in non relational EOAdaptors like ERMemoryAdaptor. It provides basic fetch support.
 * Additionally, this tracks a sequence number for the entity (for pk generation).
 * 
 * @author q
 */
public abstract class EREntityStore {
  private int _sequence = 0;
  
  public interface JoinEntityStore { }
  
  public void clear() {
    _sequence = 0;
  }
  
  public void commitFromTransactionStore(EREntityStore store) {
    throw new UnsupportedOperationException("Transactions are not supported in " + getClass().getName());
  }
  
  public int deleteRowsDescribedByQualifier(EOQualifier qualifier, EOEntity entity) {
    try {
      int count = 0;
      Iterator<NSMutableDictionary<String, Object>> i = iterator();
      while (i.hasNext()) {
        NSMutableDictionary<String, Object> rawRow = i.next();
        NSMutableDictionary<String, Object> row = rowFromStoredValues(rawRow, entity);
        if (qualifier == null || qualifier.evaluateWithObject(row)) {
          i.remove();
          count++;
        }
      }
      return count;
    }
    catch (EOGeneralAdaptorException e) {
      throw e;
    }
    catch (Throwable e) {
      e.printStackTrace();
      throw new EOGeneralAdaptorException("Failed to delete '" + entity.name() + "' with qualifier " + qualifier + ": " + e.getMessage());
    }
  }

	public NSMutableArray<NSMutableDictionary<String, Object>> fetch(NSArray<EOAttribute> attributesToFetch,
			EOFetchSpecification fetchSpecification, boolean shouldLock, EOEntity entity, ERMemoryAdaptorContext context) {
		EOQualifier qualifier = null;
		int fetchLimit = 0;
		NSArray<EOSortOrdering> sortOrderings = null;
		if (fetchSpecification != null) {
			qualifier = fetchSpecification.qualifier();
			fetchLimit = fetchSpecification.fetchLimit();
			sortOrderings = fetchSpecification.sortOrderings();
		}

		if (entity.restrictingQualifier() != null) {
			if (qualifier != null) {
				qualifier = new EOAndQualifier(new NSArray(new EOQualifier[] { qualifier, entity.restrictingQualifier() }));
			} else {
				qualifier = entity.restrictingQualifier();
			}
		}

		NSMutableArray<EORelationship> mergeRelationships = new NSMutableArray<EORelationship>();
		if (qualifier != null && context != null) {
			NSArray<EOKeyValueQualifier> keyValueQualifiers = ERXQ.extractKeyValueQualifiers(qualifier);
			for (EOKeyValueQualifier keyValueQualifier : keyValueQualifiers) {
				String qualifierKey = keyValueQualifier.key();
				String relationshipName = qualifierKey;
				if (relationshipName.contains(".")) {
					relationshipName = ERXStringUtilities.firstPropertyKeyInKeyPath(relationshipName);
				}
				EORelationship mergeRelationship = entity.relationshipNamed(relationshipName);
				if (mergeRelationship != null) {
					mergeRelationships.add(mergeRelationship);
					qualifier = ERXQ.replaceQualifierWithQualifier(qualifier, keyValueQualifier,
							ERXQ.has(qualifierKey, new NSArray(keyValueQualifier.value())));
				} else if (qualifierKey.equals(entity.primaryKeyAttributeNames().get(0))
						&& keyValueQualifier.selector().name().equals("doesContain") && !(keyValueQualifier.value() instanceof NSArray)) {
					// fix wrong schemaBasedQualifier
					qualifier= ERXQ.replaceQualifierWithQualifier(qualifier, keyValueQualifier,
							ERXQ.is(qualifierKey, keyValueQualifier.value()));
				}
			}
		}

		// int count = 0;
		NSMutableArray<NSMutableDictionary<String, Object>> fetchedRows = new NSMutableArray<NSMutableDictionary<String, Object>>();
		Iterator<NSMutableDictionary<String, Object>> i = iterator();
		while (i.hasNext()) {
			NSMutableDictionary<String, Object> rawRow = i.next();
			NSMutableDictionary<String, Object> row = rowFromStoredValues(rawRow, entity);
			for (EORelationship mergeRelationship : mergeRelationships) {
				NSArray<NSMutableDictionary<String, Object>> found = null;
				if (mergeRelationship.isFlattened() && mergeRelationship.isToMany()) {
					found = fetchRelatedManyToManyRows(entity, row, mergeRelationship, context);
				} else {
					found = fetchRelatedRows(entity, row, mergeRelationship, context);
				}
				if (found != null && !found.isEmpty()) {
					row.setObjectForKey(found, mergeRelationship.name());
				}
			}
			if (qualifier == null || qualifier.evaluateWithObject(row)) {
				for (EORelationship mergeRelationship : mergeRelationships) {
					row.removeObjectForKey(mergeRelationship.name());
				}
				fetchedRows.addObject(row);
				// count++;
			}
			// if (fetchLimit > 0 && count == fetchLimit) {
			// 	   break;
			// }
		}

		if (sortOrderings != null) {
			EOSortOrdering.sortArrayUsingKeyOrderArray(fetchedRows, sortOrderings);
		}

		if (fetchLimit > 0 && fetchedRows.count() > fetchLimit) {
			fetchedRows.removeObjectsInRange(new NSRange(fetchLimit, fetchedRows.count() - fetchLimit));
		}
		return fetchedRows;
	}

	/**
	 * Will fetch related rows for the given row via the passed many-to-many
	 * relationship. The context will be used to access the needed entity
	 * stores.
	 * 
	 * @param entity
	 *            the current entity
	 * @param row
	 *            the currently selected row
	 * @param relationship
	 *            the many-to-many relationship
	 * @param context
	 *            the memory adaptor context
	 * @return array of rows from related entity store
	 */
	protected NSArray<NSMutableDictionary<String, Object>> fetchRelatedManyToManyRows(EOEntity entity,
			NSDictionary<String, Object> row, EORelationship relationship, ERMemoryAdaptorContext context) {
		String relationshipPath = relationship.relationshipPath();
		String toJoinKey = ERXStringUtilities.firstPropertyKeyInKeyPath(relationshipPath);
		String toDestKey = ERXStringUtilities.keyPathWithoutFirstProperty(relationshipPath);
		EORelationship toJoinRelationship = entity.anyRelationshipNamed(toJoinKey);
		EOEntity joinEntity = toJoinRelationship.destinationEntity();
		EREntityStore joinStore = context._entityStoreForEntity(joinEntity);
		String sourceAttribute = toJoinRelationship.sourceAttributes().get(0).name();
		String destinationAttribute = toJoinRelationship.destinationAttributes().get(0).name();

		ERXFetchSpecification fs = new ERXFetchSpecification(joinEntity.name(), ERXQ.equals(destinationAttribute,
				row.valueForKey(sourceAttribute)), null);
		NSArray<NSMutableDictionary<String, Object>> fetchedObjects = joinStore.fetch(joinEntity.attributesToFetch(),
				fs, false, joinEntity, context);

		if (fetchedObjects.isEmpty()) {
			return NSArray.EmptyArray;
		}

		EORelationship destRelationship = joinEntity.anyRelationshipNamed(toDestKey);
		sourceAttribute = destRelationship.sourceAttributes().get(0).name();
		destinationAttribute = destRelationship.destinationAttributes().get(0).name();
		NSArray<Object> destValues = (NSArray<Object>) fetchedObjects.valueForKey(sourceAttribute);
		EOEntity destEntity = relationship.destinationEntity();

		fs = new ERXFetchSpecification(destEntity.name(), ERXQ.in(destinationAttribute, destValues), null);
		EREntityStore destinationStore = context._entityStoreForEntity(destEntity);
		fetchedObjects = destinationStore.fetch(destEntity.attributesToFetch(), fs, false, destEntity, context);

		return fetchedObjects;
	}

	/**
	 * Will fetch related rows for the given row relationship. The
	 * context will be used to access the needed entity store.
	 * 
	 * @param entity
	 *            the current entity
	 * @param row
	 *            the currently selected row
	 * @param relationship
	 *            the relationship
	 * @param context
	 *            the memory adaptor context
	 * @return array of rows from related entity store
	 */
	protected NSArray<NSMutableDictionary<String, Object>> fetchRelatedRows(EOEntity entity,
			NSDictionary<String, Object> row, EORelationship relationship, ERMemoryAdaptorContext context) {
		EOEntity destEntity = relationship.destinationEntity();
		EREntityStore destStore = context._entityStoreForEntity(destEntity);
		String sourceAttribute = relationship.sourceAttributes().get(0).name();
		String destinationAttribute = relationship.destinationAttributes().get(0).name();

		ERXFetchSpecification fs = new ERXFetchSpecification(destEntity.name(), ERXQ.equals(destinationAttribute,
				row.valueForKey(sourceAttribute)), null);
		return destStore.fetch(destEntity.attributesToFetch(), fs, false, destEntity, context);
	}

  protected NSMutableDictionary<String, Object> rowFromStoredValues(NSMutableDictionary<String, Object> rawRow, EOEntity entity) {
    NSMutableDictionary<String, Object> row = new NSMutableDictionary<String, Object>(rawRow.count()); 
    for (EOAttribute attribute : entity.attributesToFetch()) {
      Object value = rawRow.objectForKey(attribute.columnName());
      if (attribute.isDerived()) {
        if (!attribute.isFlattened()) {
          // Evaluate derived attribute expression

          /*
          //This is a hack to support SQL string concatenation in derived attributes
          String expression = attribute.definition().replaceAll("\\|\\|", "+ '' +");
          try {
            value = Ognl.getValue(expression, rawRow);
          } catch (Throwable t) {
            t.printStackTrace();
          }
          */
        } else {
          String dstKey = attribute.definition();
          value = rawRow.objectForKey(dstKey);
        }
      }
      row.setObjectForKey(value != null ? value : NSKeyValueCoding.NullValue, attribute.name());
    }
    return row;
  }

  protected abstract void _insertRow(NSMutableDictionary<String, Object> row, EOEntity entity);
  
  public void insertRow(NSDictionary<String, Object> row, EOEntity entity) {
    try {
      NSMutableDictionary<String, Object> mutableRow = new NSMutableDictionary<String, Object>(row.size());
      for (Enumeration e = entity.attributes().objectEnumerator(); e.hasMoreElements();) {
        EOAttribute attribute = (EOAttribute) e.nextElement();
        Object value = row.objectForKey(attribute.name());
        if (!attribute.isDerived())
          mutableRow.setObjectForKey(value != null ? value : NSKeyValueCoding.NullValue, attribute.columnName());
      }
      _insertRow(mutableRow, entity);
    }
    catch (EOGeneralAdaptorException e) {
      throw e;
    }
    catch (Throwable e) {
      e.printStackTrace();
      throw new EOGeneralAdaptorException("Failed to insert '" + entity.name() + "' with row " + row + ": " + e.getMessage());
    }
  }

  public abstract Iterator<NSMutableDictionary<String, Object>> iterator();
  
  public int nextSequence() {
    return ++_sequence;
  }

  public EREntityStore transactionStore() {
    throw new UnsupportedOperationException("Transactions are not supported in " + getClass().getName());
  }

  public int updateValuesInRowsDescribedByQualifier(NSDictionary<String, Object> updatedRow, EOQualifier qualifier, EOEntity entity) {
    try {
      int count = 0;
      Iterator<NSMutableDictionary<String, Object>> i = iterator();
      while (i.hasNext()) {
        NSMutableDictionary<String, Object> rawRow = i.next();
        NSMutableDictionary<String, Object> row = rowFromStoredValues(rawRow, entity);
        
        if (qualifier == null || qualifier.evaluateWithObject(row)) {
          for (Map.Entry<String, Object> entry : updatedRow.entrySet()) {
            EOAttribute attribute = entity.attributeNamed(entry.getKey());
            rawRow.setObjectForKey(entry.getValue(), attribute.columnName());
          }
          count++;
        }
      }

      return count;
    }
    catch (EOGeneralAdaptorException e) {
      e.printStackTrace();
      throw e;
    }
    catch (Throwable e) {
      e.printStackTrace();
      throw new EOGeneralAdaptorException("Failed to update '" + entity.name() + "' row " + updatedRow + " with qualifier " + qualifier + ": " + e.getMessage());
    }
  }  
  
}

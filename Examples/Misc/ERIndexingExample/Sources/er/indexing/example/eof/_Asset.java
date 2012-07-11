// $LastChangedRevision$ DO NOT EDIT.  Make changes to Asset.java instead.
package er.indexing.example.eof;

import er.extensions.foundation.*;
import er.extensions.eof.*;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;


@SuppressWarnings("all")
public abstract class _Asset extends ERXGenericRecord {

	public static final String ENTITY_NAME = "Asset";

    public interface Key {
	// Attributes
	   public static final String CONTENT = "content";
	   public static final String CREATION_DATE = "creationDate";
	   public static final String PRICE = "price";
	   public static final String USER_COUNT = "userCount";

	// Relationships
	   public static final String ASSET_GROUP = "assetGroup";
	   public static final String TAGS = "tags";
    }

    public static class _AssetClazz extends ERXGenericRecord.ERXGenericRecordClazz<Asset> {
        /* more clazz methods here */
    }

  public String content() {
    return (String) storedValueForKey(Key.CONTENT);
  }
  public void setContent(String value) {
    takeStoredValueForKey(value, Key.CONTENT);
  }

  public NSTimestamp creationDate() {
    return (NSTimestamp) storedValueForKey(Key.CREATION_DATE);
  }
  public void setCreationDate(NSTimestamp value) {
    takeStoredValueForKey(value, Key.CREATION_DATE);
  }

  public java.math.BigDecimal price() {
    return (java.math.BigDecimal) storedValueForKey(Key.PRICE);
  }
  public void setPrice(java.math.BigDecimal value) {
    takeStoredValueForKey(value, Key.PRICE);
  }

  public Long userCount() {
    return (Long) storedValueForKey(Key.USER_COUNT);
  }
  public void setUserCount(Long value) {
    takeStoredValueForKey(value, Key.USER_COUNT);
  }

  public er.indexing.example.eof.AssetGroup assetGroup() {
    return (er.indexing.example.eof.AssetGroup)storedValueForKey(Key.ASSET_GROUP);
  }
  public void setAssetGroup(er.indexing.example.eof.AssetGroup value) {
    takeStoredValueForKey(value, Key.ASSET_GROUP);
  }

  public NSArray<er.indexing.example.eof.Tag> tags() {
    return (NSArray<er.indexing.example.eof.Tag>)storedValueForKey(Key.TAGS);
  }
  public void addToTags(er.indexing.example.eof.Tag object) {
      includeObjectIntoPropertyWithKey(object, Key.TAGS);
  }
  public void removeFromTags(er.indexing.example.eof.Tag object) {
      excludeObjectFromPropertyWithKey(object, Key.TAGS);
  }

}

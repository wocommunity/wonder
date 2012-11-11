// $LastChangedRevision$ DO NOT EDIT.  Make changes to AssetGroup.java instead.
package er.indexing.example.eof;

import er.extensions.foundation.*;
import er.extensions.eof.*;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;


@SuppressWarnings("all")
public abstract class _AssetGroup extends ERXGenericRecord {

	public static final String ENTITY_NAME = "AssetGroup";

    public interface Key {
	// Attributes
	   public static final String NAME = "name";

	// Relationships
	   public static final String ASSETS = "assets";
    }

    public static class _AssetGroupClazz extends ERXGenericRecord.ERXGenericRecordClazz<AssetGroup> {
        /* more clazz methods here */
    }

  public String name() {
    return (String) storedValueForKey(Key.NAME);
  }
  public void setName(String value) {
    takeStoredValueForKey(value, Key.NAME);
  }

  public NSArray<er.indexing.example.eof.Asset> assets() {
    return (NSArray<er.indexing.example.eof.Asset>)storedValueForKey(Key.ASSETS);
  }
  public void addToAssets(er.indexing.example.eof.Asset object) {
      includeObjectIntoPropertyWithKey(object, Key.ASSETS);
  }
  public void removeFromAssets(er.indexing.example.eof.Asset object) {
      excludeObjectFromPropertyWithKey(object, Key.ASSETS);
  }

}

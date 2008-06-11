// $LastChangedRevision: 7719 $ DO NOT EDIT.  Make changes to ERIDirectory.java instead.
package er.indexing.eof;

import er.extensions.foundation.*;
import er.extensions.eof.*;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;


@SuppressWarnings("all")
public abstract class _ERIDirectory extends ERXGenericRecord {

	public static final String ENTITY_NAME = "ERIDirectory";

    public interface Key {
	// Attributes
	   public static final String NAME = "name";

	// Relationships
	   public static final String FILES = "files";
    }

    public static class _ERIDirectoryClazz extends ERXGenericRecord.ERXGenericRecordClazz<ERIDirectory> {
        /* more clazz methods here */
    }

  public String name() {
    return (String) storedValueForKey(Key.NAME);
  }
  public void setName(String value) {
    takeStoredValueForKey(value, Key.NAME);
  }

  public NSArray<er.indexing.eof.ERIFile> files() {
    return (NSArray<er.indexing.eof.ERIFile>)storedValueForKey(Key.FILES);
  }
  public void addToFiles(er.indexing.eof.ERIFile object) {
      includeObjectIntoPropertyWithKey(object, Key.FILES);
  }
  public void removeFromFiles(er.indexing.eof.ERIFile object) {
      excludeObjectFromPropertyWithKey(object, Key.FILES);
  }

}

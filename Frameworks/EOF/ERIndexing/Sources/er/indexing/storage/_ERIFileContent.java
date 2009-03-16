// $LastChangedRevision: 7719 $ DO NOT EDIT.  Make changes to ERIFileContent.java instead.
package er.indexing.storage;

import er.extensions.foundation.*;
import er.extensions.eof.*;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;


@SuppressWarnings("all")
public abstract class _ERIFileContent extends ERXGenericRecord {

	public static final String ENTITY_NAME = "ERIFileContent";

    public interface Key {
	// Attributes
	   public static final String CONTENT = "content";

	// Relationships
    }

    public static class _ERIFileContentClazz extends ERXGenericRecord.ERXGenericRecordClazz<ERIFileContent> {
        /* more clazz methods here */
    }

  public NSData content() {
    return (NSData) storedValueForKey(Key.CONTENT);
  }
  public void setContent(NSData value) {
    takeStoredValueForKey(value, Key.CONTENT);
  }

}

// _ERS3Attachment.java
// DO NOT EDIT.  Make changes to ERS3Attachment.java instead.
package er.attachment.model;


import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;
import org.apache.log4j.Logger;

@SuppressWarnings("all")
public abstract class _ERS3Attachment extends er.attachment.model.ERAttachment {
	public static final String ENTITY_NAME = "ERS3Attachment";

	public static final String S3_PATH_KEY = "s3Path";


	private static Logger LOG = Logger.getLogger(_ERS3Attachment.class);

	public _ERS3Attachment() {
		super();
	}

	public ERS3Attachment localInstanceOfERS3Attachment(EOEditingContext editingContext) {
		ERS3Attachment localInstance = (ERS3Attachment)EOUtilities.localInstanceOfObject(editingContext, this);
		if (localInstance == null) {
			throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
		}
		return localInstance;
	}


	public String s3Path() {
		return (String) storedValueForKey("s3Path");
	}

	public void setS3Path(String aValue) {
		if (_ERS3Attachment.LOG.isDebugEnabled()) {
			_ERS3Attachment.LOG.debug( "updating s3Path from "+s3Path()+" to "+aValue );
		}
		takeStoredValueForKey(aValue, "s3Path");
	}

	public static ERS3Attachment createERS3Attachment(EOEditingContext editingContext, String mimeType, String originalFileName, java.lang.Boolean proxied, Integer size, String webPath) {
		ERS3Attachment eoObject = (ERS3Attachment)EOUtilities.createAndInsertInstance(editingContext, _ERS3Attachment.ENTITY_NAME);
		eoObject.setMimeType(mimeType);
		eoObject.setOriginalFileName(originalFileName);
		eoObject.setProxied(proxied);
		eoObject.setSize(size);
		eoObject.setWebPath(webPath);
		return eoObject;
	}

	public static NSArray<ERS3Attachment> fetchAllERS3Attachments(EOEditingContext editingContext) {
		return _ERS3Attachment.fetchAllERS3Attachments(editingContext, null);
	}

	public static NSArray<ERS3Attachment> fetchAllERS3Attachments(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
		return _ERS3Attachment.fetchERS3Attachments(editingContext, null, sortOrderings);
	}

	public static NSArray<ERS3Attachment> fetchERS3Attachments(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
		EOFetchSpecification fetchSpec = new EOFetchSpecification(_ERS3Attachment.ENTITY_NAME, qualifier, sortOrderings);
		fetchSpec.setIsDeep(true);
		NSArray<ERS3Attachment> eoObjects = (NSArray<ERS3Attachment>)editingContext.objectsWithFetchSpecification(fetchSpec);
		return eoObjects;
	}

	public static ERS3Attachment fetchERS3Attachment(EOEditingContext editingContext, String keyName, Object value) {
		return _ERS3Attachment.fetchERS3Attachment(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
	}

	public static ERS3Attachment fetchERS3Attachment(EOEditingContext editingContext, EOQualifier qualifier) {
		NSArray<ERS3Attachment> eoObjects = _ERS3Attachment.fetchERS3Attachments(editingContext, qualifier, null);
		ERS3Attachment eoObject;
		int count = eoObjects.count();
		if (count == 0) {
			eoObject = null;
		}
		else if (count == 1) {
			eoObject = (ERS3Attachment)eoObjects.objectAtIndex(0);
		}
		else {
			throw new IllegalStateException("There was more than one ERS3Attachment that matched the qualifier '" + qualifier + "'.");
		}
		return eoObject;
	}
	
	public static ERS3Attachment fetchRequiredERS3Attachment(EOEditingContext editingContext, String keyName, Object value) {
		return _ERS3Attachment.fetchRequiredERS3Attachment(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
	}

	public static ERS3Attachment fetchRequiredERS3Attachment(EOEditingContext editingContext, EOQualifier qualifier) {
		ERS3Attachment eoObject = _ERS3Attachment.fetchERS3Attachment(editingContext, qualifier);
		if (eoObject == null) {
			throw new NoSuchElementException("There was no ERS3Attachment that matched the qualifier '" + qualifier + "'.");
		}
		return eoObject;
	}

	public static ERS3Attachment localInstanceOfERS3Attachment(EOEditingContext editingContext, ERS3Attachment eo) {
		ERS3Attachment localInstance = (eo == null) ? null : (ERS3Attachment)EOUtilities.localInstanceOfObject(editingContext, eo);
		if (localInstance == null && eo != null) {
			throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
		}
		return localInstance;		
	}

}

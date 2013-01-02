package er.indexing;

import org.apache.lucene.document.Document;

import com.webobjects.eocontrol.EOKeyGlobalID;
import com.webobjects.foundation.NSKeyValueCoding;

import er.extensions.eof.ERXKeyGlobalID;

public class ERDocument implements NSKeyValueCoding {
	
	private static final String GID = "EOGlobalID";
	 
	private Document _doc;
	private Float _score;
	
	public ERDocument(Document doc, float score) {
		super();
		_doc = doc;
		_score = Float.valueOf(score);
	}
	
	// ACCESSORS
	
	public Float score() {
		return _score;
	}
	
	public void setScore(Float score) {
		_score = score;
	}
	
	public EOKeyGlobalID eoKeyGlobalId() {
		String gidString = _doc.get(GID);
		EOKeyGlobalID gid = ERXKeyGlobalID.fromString(gidString).globalID();
		return gid;
	}
	
	// KVC
	
	public Object valueForKey(String key) {
		Object result =  _doc.get(key);
		if (result == null) {
			result =  NSKeyValueCoding.DefaultImplementation.valueForKey(this, key);
		}
		return result;
	}
	
	public void takeValueForKey(Object obj, String key) {
		// do nuttin'
	}
	
}

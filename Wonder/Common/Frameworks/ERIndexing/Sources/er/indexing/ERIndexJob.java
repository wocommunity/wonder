/**
 * 
 */
package er.indexing;

import com.webobjects.foundation.NSArray;

class ERIndexJob {
	private NSArray _added;
	private NSArray _deleted;

	public ERIndexJob(NSArray added, NSArray deleted) {
		_added = added;
		_deleted = deleted;
	}

	public NSArray added() {
		return _added;
	}

	public NSArray deleted() {
		return _deleted;
	}
}
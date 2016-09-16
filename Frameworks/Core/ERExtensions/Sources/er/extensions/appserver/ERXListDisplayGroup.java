package er.extensions.appserver;

import java.util.List;

import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.batching.ERXBatchingDisplayGroup;
import er.extensions.batching.IBatchingList;

/**
 * A WODisplayGroup that takes Lists instead of NSArrays and supports the
 * IBatchingList interface. This should behave just like a WODisplayGroup except
 * that you call setObjectList instead of setObjectArray. The backing NSArray of
 * this object is a fake array just like ERXBatchingDisplayGroup uses.
 * 
 * @author mschrag
 * @param <T> 
 */
public class ERXListDisplayGroup<T> extends WODisplayGroup {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private List<T> _objectList;
	private NSArray<T> _displayedObjects;

	public ERXListDisplayGroup() {
	}

	public List<T> getObjectList() {
		return _objectList;
	}

	protected int size() {
		int size;
		if (_objectList instanceof NSArray) {
			size = ((NSArray<T>) _objectList).count();
		}
		else {
			size = _objectList.size();
		}
		return size;
	}

	public void setObjectList(List<T> objectList) {
		_objectList = objectList;
		if (objectList instanceof IBatchingList) {
			((IBatchingList) objectList).setSortOrderings(sortOrderings());
			((IBatchingList) objectList).setPageIndex(currentBatchIndex());
			((IBatchingList) objectList).setPageSize(numberOfObjectsPerBatch());
		}
		ERXBatchingDisplayGroup.FakeArray fa = new ERXBatchingDisplayGroup.FakeArray(size());
		setObjectArray(fa);
	}

	@Override
	public int batchCount() {
		if (numberOfObjectsPerBatch() == 0) {
			return 0;
		}
		int size = size();
		if (size == 0) {
			return 1;
		}
		return (size - 1) / numberOfObjectsPerBatch() + 1;
	}

	@Override
	public void setCurrentBatchIndex(int index) {
		if (currentBatchIndex() != index) {
			_displayedObjects = null;
		}
		super.setCurrentBatchIndex(index);
		if (_objectList instanceof IBatchingList) {
			((IBatchingList) _objectList).setPageIndex(currentBatchIndex());
		}
	}

	@Override
	public void setNumberOfObjectsPerBatch(int count) {
		if (numberOfObjectsPerBatch() != count) {
			_displayedObjects = null;
		}
		super.setNumberOfObjectsPerBatch(count);
		if (_objectList instanceof IBatchingList) {
			((IBatchingList) _objectList).setPageSize(numberOfObjectsPerBatch());
		}
	}

	@Override
	public NSArray<T> displayedObjects() {
		NSMutableArray<T> displayedObjects = new NSMutableArray<>(numberOfObjectsPerBatch());
		if (_displayedObjects == null) {
			int numberOfObjectsPerBatch = numberOfObjectsPerBatch();
			int startIndex = (currentBatchIndex() - 1) * numberOfObjectsPerBatch;
			int size = (_objectList == null) ? 0 : size();
			int endIndex = Math.min(startIndex + numberOfObjectsPerBatch, size);
			for (int i = startIndex; i < endIndex; i++) {
				displayedObjects.addObject(_objectList.get(i));
			}
		}
		return displayedObjects;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void setSortOrderings(NSArray sortOrderings) {
		if (_objectList instanceof IBatchingList) {
			((IBatchingList) _objectList).setSortOrderings(sortOrderings());
		}
		super.setSortOrderings(sortOrderings);
	}
}

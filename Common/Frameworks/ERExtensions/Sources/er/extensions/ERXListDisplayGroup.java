package er.extensions;

import java.util.List;

import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

/**
 * A WODisplayGroup that takes Lists instead of NSArrays and 
 * supports the IBatchingList interface.  This should behave
 * just like a WODisplayGroup except that you call setObjectList
 * instead of setObjectArray.  The backing NSArray of this
 * object is a fake array just like ERXBatchingDisplayGroup
 * uses.
 * 
 * @author mschrag
 */
public class ERXListDisplayGroup extends WODisplayGroup {
  private List _objectList;
  private NSArray _displayedObjects;

  public ERXListDisplayGroup() {
  }

  public List get_objectList() {
    return _objectList;
  }

  public void set_objectList(List objectList) {
    _objectList = objectList;
    if (objectList instanceof IBatchingList) {
      ((IBatchingList) objectList).setSortOrderings(sortOrderings());
      ((IBatchingList) objectList).setPageIndex(currentBatchIndex());
      ((IBatchingList) objectList).setPageSize(numberOfObjectsPerBatch());
    }
    setObjectArray(new FakeArray(objectList.size()));
  }

  public int batchCount() {
    if (numberOfObjectsPerBatch() == 0) {
      return 0;
    }
    if (_objectList.size() == 0) {
      return 1;
    }
    return (_objectList.size() - 1) / numberOfObjectsPerBatch() + 1;
  }

  public void setCurrentBatchIndex(int index) {
    if (currentBatchIndex() != index) {
      _displayedObjects = null;
    }
    super.setCurrentBatchIndex(index);
    if (_objectList instanceof IBatchingList) {
      ((IBatchingList) _objectList).setPageIndex(currentBatchIndex());
    }
  }

  public void setNumberOfObjectsPerBatch(int count) {
    if (numberOfObjectsPerBatch() != count) {
      _displayedObjects = null;
    }
    super.setNumberOfObjectsPerBatch(count);
    if (_objectList instanceof IBatchingList) {
      ((IBatchingList) _objectList).setPageSize(numberOfObjectsPerBatch());
    }
  }

  public NSArray displayedObjects() {
    NSMutableArray displayedObjects = new NSMutableArray(numberOfObjectsPerBatch());
    if (_displayedObjects == null) {
      int numberOfObjectsPerBatch = numberOfObjectsPerBatch();
      int startIndex = (currentBatchIndex() - 1) * numberOfObjectsPerBatch;
      int size = (_objectList == null) ? 0 : _objectList.size();
      int endIndex = Math.min(startIndex + numberOfObjectsPerBatch, size);
      for (int i = startIndex; i < endIndex; i++) {
        displayedObjects.addObject(_objectList.get(i));
      }
    }
    return displayedObjects;
  }

  public void setSortOrderings(NSArray sortOrderings) {
    if (_objectList instanceof IBatchingList) {
      ((IBatchingList) _objectList).setSortOrderings(sortOrderings());
    }
    super.setSortOrderings(sortOrderings);
  }

  protected class FakeArray extends NSMutableArray {
    private int _count;

    public FakeArray(int count) {
      super(count);
      _count = count;
      Object obj = new Object();
      for (int i = 0; i < count; i++) {
        addObject(obj);
      }
    }

    public int count() {
      return _count;
    }

    public int size() {
      return _count;
    }
  }
}

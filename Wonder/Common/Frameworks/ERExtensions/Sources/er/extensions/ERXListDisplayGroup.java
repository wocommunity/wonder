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
  private List myObjectList;
  private NSArray myDisplayedObjects;

  public ERXListDisplayGroup() {
  }

  public List getObjectList() {
    return myObjectList;
  }

  public void setObjectList(List _objectList) {
    myObjectList = _objectList;
    if (_objectList instanceof IBatchingList) {
      ((IBatchingList) _objectList).setSortOrderings(sortOrderings());
      ((IBatchingList) _objectList).setPageIndex(currentBatchIndex());
      ((IBatchingList) _objectList).setPageSize(numberOfObjectsPerBatch());
    }
    setObjectArray(new FakeArray(_objectList.size()));
  }

  public int batchCount() {
    if (numberOfObjectsPerBatch() == 0) {
      return 0;
    }
    if (myObjectList.size() == 0) {
      return 1;
    }
    return (myObjectList.size() - 1) / numberOfObjectsPerBatch() + 1;
  }

  public void setCurrentBatchIndex(int _index) {
    if (currentBatchIndex() != _index) {
      myDisplayedObjects = null;
    }
    super.setCurrentBatchIndex(_index);
    if (myObjectList instanceof IBatchingList) {
      ((IBatchingList) myObjectList).setPageIndex(currentBatchIndex());
    }
  }

  public void setNumberOfObjectsPerBatch(int _count) {
    if (numberOfObjectsPerBatch() != _count) {
      myDisplayedObjects = null;
    }
    super.setNumberOfObjectsPerBatch(_count);
    if (myObjectList instanceof IBatchingList) {
      ((IBatchingList) myObjectList).setPageSize(numberOfObjectsPerBatch());
    }
  }

  public NSArray displayedObjects() {
    NSMutableArray displayedObjects = new NSMutableArray(numberOfObjectsPerBatch());
    if (myDisplayedObjects == null) {
      int numberOfObjectsPerBatch = numberOfObjectsPerBatch();
      int startIndex = (currentBatchIndex() - 1) * numberOfObjectsPerBatch;
      int size = (myObjectList == null) ? 0 : myObjectList.size();
      int endIndex = Math.min(startIndex + numberOfObjectsPerBatch, size);
      for (int i = startIndex; i < endIndex; i++) {
        displayedObjects.addObject(myObjectList.get(i));
      }
    }
    return displayedObjects;
  }

  public void setSortOrderings(NSArray _sortOrderings) {
    if (myObjectList instanceof IBatchingList) {
      ((IBatchingList) myObjectList).setSortOrderings(sortOrderings());
    }
    super.setSortOrderings(_sortOrderings);
  }

  protected class FakeArray extends NSMutableArray {
    private int myCount;

    public FakeArray(int _count) {
      super(_count);
      myCount = _count;
      Object obj = new Object();
      for (int i = 0; i < _count; i++) {
        addObject(obj);
      }
    }

    public int count() {
      return myCount;
    }

    public int size() {
      return myCount;
    }
  }
}

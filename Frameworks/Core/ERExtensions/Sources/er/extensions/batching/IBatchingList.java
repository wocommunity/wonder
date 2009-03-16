package er.extensions.batching;

import java.util.List;

import com.webobjects.foundation.NSArray;

/**
 * IBatchingList provides an interface to a List that can be
 * loaded in paginated batches.  This is mainly for use
 * with WOListDisplaygroup.
 * 
 * @author mschrag
 */
public interface IBatchingList extends List {
  /**
   * Sets the sort orderings of this list.
   * 
   * @param _sortOrderings the sort orderings array
   */
  public void setSortOrderings(NSArray _sortOrderings);

  /**
   * Sets the page size of this list.
   * 
   * @param _pageSize the page size
   */
  public void setPageSize(int _pageSize);

  /**
   * Sets the page number that is currently being viewed on this list.
   * 
   * @param _pageIndex the page number
   */
  public void setPageIndex(int _pageIndex);
}

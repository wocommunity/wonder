package er.extensions;

import java.lang.ref.WeakReference;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

/*
 * ERTransactionRecord is a reimplementation of WOTransactionRecord for
 * use with Ajax background request page caching.
 * 
 * @author mschrag
 */
public class ERTransactionRecord {
  private WeakReference _context;
  private WeakReference _page;
  private String _key;

  public ERTransactionRecord(WOComponent page, WOContext context, String key) {
    _page = new WeakReference(page);
    _context = new WeakReference(context);
    _key = key;
  }

  public WOComponent page() {
    return (WOComponent) _page.get();
  }

  public WOContext context() {
    return (WOContext) _context.get();
  }

  public String key() {
    return _key;
  }
}

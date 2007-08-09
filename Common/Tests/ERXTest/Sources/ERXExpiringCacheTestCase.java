import junit.framework.TestCase;
import er.extensions.ERXExpiringCache;

public class ERXExpiringCacheTestCase extends TestCase {
  public void testThatItWorksAtAll() {
    ERXExpiringCache<String, String> cache = new ERXExpiringCache<String, String>(10);
    cache.setObjectForKey("Krank", "Anjo");
    cache.setObjectForKey("Schrag", "Mike");
    assertEquals("Schrag", cache.objectForKey("Mike"));
    assertEquals("Krank", cache.objectForKey("Anjo"));
    assertEquals(null, cache.objectForKey("Chuck"));
  }
  
  public void testThatNoTimeoutWorksAtAll() {
    ERXExpiringCache<String, String> noTimeoutCache = new ERXExpiringCache<String, String>(ERXExpiringCache.NO_TIMEOUT);
    noTimeoutCache.setObjectForKey("Krank", "Anjo");
    noTimeoutCache.setObjectForKey("Schrag", "Mike");
    assertEquals("Schrag", noTimeoutCache.objectForKey("Mike"));
    assertEquals("Krank", noTimeoutCache.objectForKey("Anjo"));
    assertEquals(null, noTimeoutCache.objectForKey("Chuck"));
  }
  
  public void testVersionsWithVersionlessGet() {
    ERXExpiringCache<String, String> cache = new ERXExpiringCache<String, String>(ERXExpiringCache.NO_TIMEOUT);
    cache.setObjectForKeyWithVersion("Krank", "Anjo", Integer.valueOf(1));
    cache.setObjectForKeyWithVersion("Schrag", "Mike", Integer.valueOf(1));
    assertEquals("Schrag", cache.objectForKey("Mike"));
    assertEquals("Krank", cache.objectForKey("Anjo"));
    assertEquals(null, cache.objectForKey("Chuck"));
  }

  public void testVersionsWithSameVersionGet() {
    ERXExpiringCache<String, String> cache = new ERXExpiringCache<String, String>(ERXExpiringCache.NO_TIMEOUT);
    cache.setObjectForKeyWithVersion("Krank", "Anjo", Integer.valueOf(1));
    cache.setObjectForKeyWithVersion("Schrag", "Mike", Integer.valueOf(1));
    assertEquals("Schrag", cache.objectForKeyWithVersion("Mike", Integer.valueOf(1)));
    assertEquals("Krank", cache.objectForKeyWithVersion("Anjo", Integer.valueOf(1)));
    assertEquals(null, cache.objectForKeyWithVersion("Chuck", Integer.valueOf(1)));
  }

  public void testVersionsWithNewVersionGetMakeSureItIsStillGone() {
    ERXExpiringCache<String, String> cache = new ERXExpiringCache<String, String>(ERXExpiringCache.NO_TIMEOUT);
    cache.setObjectForKeyWithVersion("Krank", "Anjo", Integer.valueOf(1));
    cache.setObjectForKeyWithVersion("Schrag", "Mike", Integer.valueOf(1));
    assertEquals(null, cache.objectForKeyWithVersion("Mike", Integer.valueOf(2)));
    assertEquals("Krank", cache.objectForKeyWithVersion("Anjo", Integer.valueOf(1)));
    assertEquals(null, cache.objectForKeyWithVersion("Chuck", Integer.valueOf(1)));

    assertEquals(null, cache.objectForKeyWithVersion("Mike", Integer.valueOf(1)));
  }

  public void testVersionsWithNewVersionGet() {
    ERXExpiringCache<String, String> cache = new ERXExpiringCache<String, String>(ERXExpiringCache.NO_TIMEOUT);
    cache.setObjectForKeyWithVersion("Krank", "Anjo", Integer.valueOf(1));
    cache.setObjectForKeyWithVersion("Schrag", "Mike", Integer.valueOf(1));
    assertEquals(null, cache.objectForKeyWithVersion("Mike", Integer.valueOf(2)));
    assertEquals("Krank", cache.objectForKeyWithVersion("Anjo", Integer.valueOf(1)));
    assertEquals(null, cache.objectForKeyWithVersion("Chuck", Integer.valueOf(1)));
  }

  public void testTimeExpiration() {
    ERXExpiringCache<String, String> cache = new ERXExpiringCache<String, String>(10);
    cache.setObjectForKey("Krank", "Anjo");
    cache.setObjectForKey("Schrag", "Mike");
    assertEquals("Schrag", cache.objectForKey("Mike"));
    assertEquals("Krank", cache.objectForKey("Anjo"));
    assertEquals(null, cache.objectForKey("Chuck"));

    try {
      Thread.sleep(5000);
    }
    catch (Throwable t) {
      t.printStackTrace();
    }
    assertEquals("Schrag", cache.objectForKey("Mike"));
    assertEquals("Krank", cache.objectForKey("Anjo"));
    assertEquals(null, cache.objectForKey("Chuck"));

    cache.setObjectForKey("Hill", "Chuck");
    assertEquals("Schrag", cache.objectForKey("Mike"));
    assertEquals("Krank", cache.objectForKey("Anjo"));
    assertEquals("Hill", cache.objectForKey("Chuck"));

    try {
      Thread.sleep(7000);
    }
    catch (Throwable t) {
      t.printStackTrace();
    }
    assertEquals(null, cache.objectForKey("Mike"));
    assertEquals(null, cache.objectForKey("Anjo"));
    assertEquals("Hill", cache.objectForKey("Chuck"));
    
    try {
      Thread.sleep(5000);
    }
    catch (Throwable t) {
      t.printStackTrace();
    }
    assertEquals(null, cache.objectForKey("Mike"));
    assertEquals(null, cache.objectForKey("Anjo"));
    assertEquals(null, cache.objectForKey("Chuck"));
  }
}

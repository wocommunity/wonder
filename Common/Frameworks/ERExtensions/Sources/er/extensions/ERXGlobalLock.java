package er.extensions;

import java.io.File;
import java.io.IOException;

import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSLocking;
/**
 * Simple lock to sync access for multiple instances on one machine. This implementation 
 * uses a lock file with the given name in the temp directory. In case of a VM crash, the file
 * is *not* deleted, so you have to do this yourself.<br>
 * Additionally, you can't use it reliably on an NFS server. The lock otherwise behaves like an
 * NSRecursiveLock in that the same thread can re-lock as often as he wants.
 * <code><pre>
 * App1:
 * NSLocking lock = ERXGlobalLock.lockForName("test");
 * lock.lock();
 * ...
 * lock.unlock();
 * App2:
 * NSLocking lock = ERXGlobalLock.lockForName("test");
 * lock.lock();
 * ...
 * lock.unlock();
 * </pre></code>
 * @author ak
 */
public class ERXGlobalLock implements NSLocking {
    private File _lockfile;
    private int _lockcnt;
    private Thread _owner;
    
    public ERXGlobalLock(String name) {
        _lockfile = new File(System.getProperty("java.io.tmpdir") + File.separator + "ERXGlobalLock_" + name);
        _lockcnt = 0;
    }

    public void lock() {
        if(_owner == Thread.currentThread() && _lockcnt > 0) {
            _lockcnt++;
            return;
        }
        try {
            while(!(_lockfile.createNewFile())) {
                Thread.sleep(5);
            }
        } catch (InterruptedException e) {
            throw new NSForwardException(e, "Error while locking " + _lockfile);
        } catch (IOException e) {
            throw new NSForwardException(e, "Error while locking " + _lockfile);
        }
        _lockfile.deleteOnExit();
        _lockcnt++;
        _owner = Thread.currentThread();
    }

    public void unlock() {
        if(_lockcnt == 0) {
            throw new IllegalStateException("Can't unlock without lock");
        }
        if(_owner != Thread.currentThread()) {
            throw new IllegalStateException("Can't unlock, not owner");
        }
        _lockcnt--;
        if(_lockcnt == 0) {
            _owner = null;
            if(!_lockfile.delete()) {
               throw new IllegalStateException("Error can't delete "+  _lockfile);
            }
        }
    }
    
    /**
     * Call this on startup.
     */
    public boolean cleanup() {
    	return _lockfile.delete();
    }
    
    public static NSLocking lockForName(String name) {
        return new ERXGlobalLock(name);
    }
}

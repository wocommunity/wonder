package er.erxtest.tests;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import com.webobjects.foundation.NSForwardException;

import er.erxtest.ERXTestCase;
import er.extensions.appserver.ERXApplication;
import er.extensions.eof.ERXEC;


/*
This class should test locking of ECs:
- lock() when not locked in same thread
- lock() when locked in same thread
- lock() when saveChanges() delegate also locks

- autolock() when not locked in same thread
- autolock() when locked in same thread

- lock() when not locked in other thread
- lock() when locked in other thread

- autolock() when not locked in other thread
- autolock() when locked in other thread

- lock() when not autolocked in other thread
- lock() when autolocked in other thread

- autolock() when not locked in other thread
- autolock() when locked in other thread

- finalize()

and all variations with coalesce() true/false
and all variations with shouldAutolock() true/false

expected results are:
- all should have been locked()
- lock() context should not been autolocked()
- autolock() context should not been autolocked() if shouldAutolock() is false
- autolock() context should been autolocked() if shouldAutolock() is true
- autolock() context should not been called in finalize()

- if coalesce, autolock should stay open?

 */
public class ERXECLockingTestCase extends ERXTestCase {
    static ExecutorService executor = Executors.newCachedThreadPool();

    protected static Object call(Callable<? extends Object> aCallable, long timeout) throws TimeoutException {

        Future<? extends Object> future = executor.submit(aCallable);
        try {
            return future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e1) {
            throw new TimeoutException();
        } catch (ExecutionException e1) {
            throw NSForwardException._runtimeExceptionForThrowable(e1.getCause());
        }
    }
    
    public static final Logger log = Logger.getLogger(ERXECLockingTestCase.class);
    private static final long JOIN_TIME = 2000L;
    
    public static class EC extends ERXEC {
        public boolean beforeLock;
        public boolean afterLock;
        public boolean beforeUnlock;
        public boolean afterUnlock;

        public boolean beforeAutoLock;
        public boolean afterAutoLock;
        public boolean beforeAutoUnlock;
        public boolean afterAutoUnlock;
        
        public boolean wasAutolocked;
        public int autoLocks;

        public long waitTime;

        @Override
        public void lock() {
            beforeLock = true;
            super.lock();
            afterLock = true;
        }

        @Override
        public void unlock() {
            beforeUnlock = true;
            super.unlock();
            afterUnlock = true;
        }

        @Override
        protected boolean autoLock(String method) {
            beforeAutoLock = true;
            boolean result = super.autoLock(method);
            if(result) {
                wasAutolocked = true;
                autoLocks++;
            }
            afterAutoLock = true;
            return result;
        }

        @Override
        protected void autoUnlock(boolean wasLocked) {
            beforeAutoUnlock = true;
            super.autoUnlock(wasLocked);
            afterAutoUnlock = true;
        }
        
        public void saveChangesWithWait() {
            waitTime = 400;
            saveChanges();
        }

        @Override
        public void _saveChanges() {
            if(waitTime > 0) {
                synchronized(this) {
                    try {
                        wait(waitTime);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            super._saveChanges();
        }
    }
    
    private EC createEC() {
        return new EC() ;
    }

    private void assertLockable(final EC ec) {
        assertTrue(isLockable(ec));
   }

    @SuppressWarnings("boxing")
	private boolean isLockable(final EC ec) {
        try {
            return (Boolean) call(new Callable<Boolean>() {
                public Boolean call() throws Exception {
                    Boolean r = ec.tryLock();
                    if(r) {
                        ec.unlock();
                    }
                    return r;
                }
                
            }, 20);
        } catch (TimeoutException e) {
            fail(e.getMessage());
        }
        return false;
    }

    private void assertNotLockable(EC ec) {
        assertFalse(isLockable(ec));
    }
    
    public void test() {
        EC ec = createEC();
        assertFalse(ec.beforeLock);
        assertFalse(ec.beforeAutoLock);
        assertFalse(ec.beforeAutoUnlock);
        assertFalse(ec.beforeUnlock);
        assertFalse(ec.isAutoLocked());
        assertFalse(ec.wasAutolocked);
        assertLockable(ec);
    }

    public void testPlainOneThreadLocking() {
        EC ec = createEC();
        ec.lock();
        assertTrue(ec.beforeLock);
        assertTrue(ec.afterLock);
        assertFalse(ec.beforeAutoLock);
        assertFalse(ec.beforeAutoUnlock);
        assertFalse(ec.beforeUnlock);
        ec.unlock();
        assertTrue(ec.beforeLock);
        assertTrue(ec.afterLock);
        assertTrue(ec.beforeUnlock);
        assertTrue(ec.afterUnlock);
        assertFalse(ec.beforeAutoLock);
        assertFalse(ec.beforeAutoUnlock);
        assertFalse(ec.isAutoLocked());
        assertFalse(ec.wasAutolocked);
        assertEquals(ec.autoLocks, 0);
        assertLockable(ec);
   }
    
    public void testPlainOneThreadAutoLocking() {
        EC ec = createEC();
        ec.saveChanges();
        assertTrue(ec.beforeLock);
        assertTrue(ec.afterLock);
        assertTrue(ec.beforeAutoLock);
        assertTrue(ec.afterAutoLock);
        assertTrue(ec.beforeAutoUnlock);
        assertTrue(ec.afterAutoUnlock);
        assertTrue(ec.beforeUnlock);
        assertTrue(ec.afterUnlock);
        assertFalse(ec.isAutoLocked());
        assertTrue(ec.wasAutolocked);
        assertTrue("autoLocks: " + ec.autoLocks, ec.autoLocks >= 1);
        assertLockable(ec);
    }

    public void testPlainOneThreadAutoLockingCoalesceNotInRequest() {
        // AK: not in a request->coalesce doesn't work
        EC ec = createEC();
        ec.setCoalesceAutoLocks(true);
        ec.saveChanges();
        ec.saveChanges();
        assertTrue(ec.beforeLock);
        assertTrue(ec.afterLock);
        assertTrue(ec.beforeAutoLock);
        assertTrue(ec.afterAutoLock);
        assertTrue(ec.beforeAutoUnlock);
        assertTrue(ec.afterAutoUnlock);
        assertTrue(ec.beforeUnlock);
        assertTrue(ec.afterUnlock);
        assertFalse(ec.isAutoLocked());
        assertTrue(ec.wasAutolocked);
        assertEquals(2, ec.autoLocks);
        assertLockable(ec);
    }

    public void testPlainOneThreadAutoLockingCoalesceInRequest() {
        EC ec = createEC();
        ERXApplication._startRequest();
        ec.setCoalesceAutoLocks(true);
        ec.saveChanges();
        ec.saveChanges();
        assertNotLockable(ec);
        assertTrue(ec.isAutoLocked());
        ERXApplication._endRequest();
        assertTrue(ec.beforeLock);
        assertTrue(ec.afterLock);
        assertTrue(ec.beforeAutoLock);
        assertTrue(ec.afterAutoLock);
        assertTrue(ec.beforeAutoUnlock);
        assertTrue(ec.afterAutoUnlock);
        assertTrue(ec.beforeUnlock);
        assertTrue(ec.afterUnlock);
        assertEquals(1, ec.autoLocks);
        assertFalse(ec.isAutoLocked());
        assertTrue(ec.wasAutolocked);
        assertLockable(ec);
    }
    
    public void testPlainOneThreadAutoLockingWithLock() {
        EC ec = createEC();
        ec.lock();
        ec.saveChanges();
        ec.unlock();
        assertFalse(ec.wasAutolocked);
        ec.saveChanges();
        assertTrue(ec.beforeLock);
        assertTrue(ec.afterLock);
        assertTrue(ec.beforeAutoLock);
        assertTrue(ec.afterAutoLock);
        assertTrue(ec.beforeAutoUnlock);
        assertTrue(ec.afterAutoUnlock);
        assertTrue(ec.beforeUnlock);
        assertTrue(ec.afterUnlock);
        assertFalse(ec.isAutoLocked());
        assertTrue(ec.wasAutolocked);
    }

    public void testTwoThreadWithAutoLock() {
        final EC ec = createEC();
        
        Runnable r = new Runnable() {
            
            public void run() {
                log.info("Saving: " + Thread.currentThread().getName());
                ec.saveChangesWithWait();
                log.info("Saved: " + Thread.currentThread().getName());
            }
            
        };
        Thread t1 = new Thread(r);
        Thread t2 = new Thread(r);
        t1.start();
        t2.start();
        try {
            t1.join(JOIN_TIME);
            t2.join(JOIN_TIME);
            assertFalse(t1.isAlive());
            assertFalse(t2.isAlive());
        } catch (InterruptedException e) {
            assertTrue(false);
        }
        assertTrue(ec.beforeLock);
        assertTrue(ec.afterLock);
        assertTrue(ec.beforeAutoLock);
        assertTrue(ec.afterAutoLock);
        assertTrue(ec.beforeAutoUnlock);
        assertTrue(ec.afterAutoUnlock);
        assertTrue(ec.beforeUnlock);
        assertTrue(ec.afterUnlock);
        assertFalse(ec.isAutoLocked());
        assertTrue(ec.wasAutolocked);
    }

    public void testTwoThreadWithLock() {
        final EC ec = createEC();
        
        Runnable r = new Runnable() {
            
            public void run() {
                log.info("Before lock: " + Thread.currentThread().getName());
                ec.lock();
                log.info("After lock: " + Thread.currentThread().getName());
                ec.saveChangesWithWait();
                log.info("Saved: " + Thread.currentThread().getName());
                ec.unlock();
            }
            
        };
        Thread t1 = new Thread(r);
        Thread t2 = new Thread(r);
        t1.start();
        t2.start();
        try {
            t1.join(JOIN_TIME);
            t2.join(JOIN_TIME);
            assertFalse(t1.isAlive());
            assertFalse(t2.isAlive());
        } catch (InterruptedException e) {
            assertTrue(false);
        }
        assertTrue(ec.beforeLock);
        assertTrue(ec.afterLock);
        assertTrue(ec.beforeAutoLock);
        assertTrue(ec.afterAutoLock);
        assertTrue(ec.beforeAutoUnlock);
        assertTrue(ec.afterAutoUnlock);
        assertTrue(ec.beforeUnlock);
        assertTrue(ec.afterUnlock);
        assertFalse(ec.isAutoLocked());
        assertFalse(ec.wasAutolocked);
    }

    public void testTwoThreadWithLockAutoLock() {
        final EC ec = createEC();
        
        Runnable r1 = new Runnable() {
            
            public void run() {
                log.info("Before save: " + Thread.currentThread().getName());
                ec.saveChangesWithWait();
                log.info("Saved: " + Thread.currentThread().getName());
                assertFalse(ec.isAutoLocked());
            }
            
        };
        Runnable r2 = new Runnable() {
            
            public void run() {
                log.info("Before lock: " + Thread.currentThread().getName());
                ec.lock();
                log.info("After lock: " + Thread.currentThread().getName());
                ec.saveChangesWithWait();
                log.info("Saved: " + Thread.currentThread().getName());
                ec.unlock();
                assertFalse(ec.isAutoLocked());
            }
            
        };
        Thread t1 = new Thread(r1);
        Thread t2 = new Thread(r2);
        t1.start();
        t2.start();
        try {
            t1.join(JOIN_TIME);
            t2.join(JOIN_TIME);
            assertFalse(t1.isAlive());
            assertFalse(t2.isAlive());
        } catch (InterruptedException e) {
            assertTrue(false);
        }
        assertTrue(ec.beforeLock);
        assertTrue(ec.afterLock);
        assertTrue(ec.beforeAutoLock);
        assertTrue(ec.afterAutoLock);
        assertTrue(ec.beforeAutoUnlock);
        assertTrue(ec.afterAutoUnlock);
        assertTrue(ec.beforeUnlock);
        assertTrue(ec.afterUnlock);
        assertFalse(ec.isAutoLocked());
        assertTrue(ec.wasAutolocked);
    }

    public void testTwoThreadWithAutoLockLock() {
        final EC ec = createEC();
        
        Runnable r2 = new Runnable() {
            
            public void run() {
                log.info("Before save: " + Thread.currentThread().getName());
                ec.saveChangesWithWait();
                log.info("Saved: " + Thread.currentThread().getName());
                assertFalse(ec.isAutoLocked());
            }
            
        };
        Runnable r1 = new Runnable() {
            
            public void run() {
                log.info("Before lock: " + Thread.currentThread().getName());
                ec.lock();
                log.info("After lock: " + Thread.currentThread().getName());
                ec.saveChangesWithWait();
                log.info("Saved: " + Thread.currentThread().getName());
                ec.unlock();
                assertFalse(ec.isAutoLocked());
            }
            
        };
        Thread t1 = new Thread(r1);
        Thread t2 = new Thread(r2);
        t1.start();
        t2.start();
        try {
            t1.join(JOIN_TIME);
            t2.join(JOIN_TIME);
            assertFalse(t1.isAlive());
            assertFalse(t2.isAlive());
        } catch (InterruptedException e) {
            assertTrue(false);
        }
        assertTrue(ec.beforeLock);
        assertTrue(ec.afterLock);
        assertTrue(ec.beforeAutoLock);
        assertTrue(ec.afterAutoLock);
        assertTrue(ec.beforeAutoUnlock);
        assertTrue(ec.afterAutoUnlock);
        assertTrue(ec.beforeUnlock);
        assertTrue(ec.afterUnlock);
        assertFalse(ec.isAutoLocked());
        assertTrue(ec.wasAutolocked);
    }
}

package er.erxtest.tests;

import org.apache.log4j.Logger;

import er.extensions.eof.ERXEC;
import junit.framework.TestCase;

public class ERXECLockingTestCase extends TestCase {

    public static final Logger log = Logger.getLogger(ERXECLockingTestCase.class);
    
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

        public long waitTime;

        public void lock() {
            beforeLock = true;
            super.lock();
            afterLock = true;
        }

        public void unlock() {
            beforeUnlock = true;
            super.unlock();
            afterUnlock = true;
        }

        protected boolean autoLock(String method) {
            beforeAutoLock = true;
            boolean result = super.autoLock(method);
            if(result) {
                wasAutolocked = true;
            }
            afterAutoLock = true;
            return result;
        }

        protected void autoUnlock(boolean wasLocked) {
            beforeAutoUnlock = true;
            super.autoUnlock(wasLocked);
            afterAutoUnlock = true;
        }
        
        public void saveChangesWithWait() {
            waitTime = 1000L;
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
    
    public void test() {
        EC ec = createEC();
        assertFalse(ec.beforeLock);
        assertFalse(ec.beforeAutoLock);
        assertFalse(ec.beforeAutoUnlock);
        assertFalse(ec.beforeUnlock);
        assertFalse(ec.isAutoLocked());
        assertFalse(ec.wasAutolocked);
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
            t1.join();
            t2.join();
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
            t1.join();
            t2.join();
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
}

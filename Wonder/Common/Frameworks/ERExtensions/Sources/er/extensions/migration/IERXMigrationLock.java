package er.extensions.migration;

import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EOModel;

/**
 * Because you might be running with multiple instances, there needs to be a
 * locking mechanism that is higher level than just a db context lock.
 * IERXMigrationLock is a simple interface for accessing and locking information
 * about the migration process.
 * 
 * @author mschrag
 */
public interface IERXMigrationLock {
  /**
   * Returns the current version number of the named model.
   *  
   * @param channel the adaptor channel associated with this process
   * @param model the model to lookup version information on
   * @return the current version number of the named model 
   */
	public int versionNumber(EOAdaptorChannel channel, EOModel model);

  /**
   * Sets the current version number of the named model.
   * 
   * @param channel the adaptor channel associated with this process
   * @param model the model to set version information on
   * @param versionNumber the new version number
   */
	public void setVersionNumber(EOAdaptorChannel channel, EOModel model, int versionNumber);

  /**
   * Attempts to retrieve a lock on the migration process for the named model.  A lockOwnerName
   * is provided that provides a simple mechanism to recover from a stale lock.  If the lock
   * owner name passed in matches the current lock owner name, then the lock is acquired.
   * 
   * @param channel the adaptor channel associated with this process
   * @param model the model to try to lock
   * @param lockOwnerName the name of the lock owner
   * @return true if the lock was acquired, false if it was not
   */
	public boolean tryLock(EOAdaptorChannel channel, EOModel model, String lockOwnerName);

  /**
   * Unlocks the given model.  This should only be called if you acquired a lock
   * with tryLock(..).
   * 
   * @param channel the adaptor channel associated with this process
   * @param model the model to unlock
   */
	public void unlock(EOAdaptorChannel channel, EOModel model);
}

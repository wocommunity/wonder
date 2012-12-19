package er.extensions.security;

import org.apache.log4j.Logger;

/**
 * AccessPermission
 * 
 * This class makes it easy to create AccessPermission into Frameworks.
 *  
 * The User can implement IERXAccessPermissionInterface and Delegate 
 * and then do what he like.
 * 
 * @author ishimoto
 *
 */
public class ERXAccessPermission implements IERXAccessPermissionInterface {

  private static final Logger log = Logger.getLogger(ERXAccessPermission.class);

  //********************************************************************
  //  Singleton
  //********************************************************************

  public static ERXAccessPermission instance() {
    return instance;
  }
  private static final ERXAccessPermission instance = new ERXAccessPermission();

  private ERXAccessPermission() {}

  //********************************************************************
  //  Delegate
  //********************************************************************

  public void setDelegate(IERXAccessPermissionInterface delegate) {
    this.delegate = delegate;
  }
  public IERXAccessPermissionInterface delegate() {
    return delegate;
  } 
  private IERXAccessPermissionInterface delegate = null;

  //********************************************************************
  //  AccessPermission
  //********************************************************************

  public boolean can(String key) {
    return canWithDefault(key, false);
  }

  public boolean canWithDefault(String key, boolean defaultValue) {
    if(delegate() != null) {
      return delegate().canWithDefault(key, defaultValue);
    }

    if(defaultValue) { // if true then return result without Displaying Warning
      return defaultValue;
    }
    
    log.warn("No Delegate is set. Result for '" + key + "' is false.");
    return false;
  }

  public boolean isDeveloper() {
    if(delegate() != null) {
      return delegate().isDeveloper();
    }

    log.warn("No Delegate is set. Result for 'isDeveloper' is false.");
    return false;
  }

  public boolean isAdministrator() {
    if(delegate() != null) {
      return delegate().isAdministrator();
    }

    log.warn("No Delegate is set. Result for 'isAdministrator' is false.");
    return false;
  }
}

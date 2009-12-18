package er.wojrebel;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.zeroturnaround.javarebel.Logger;
import org.zeroturnaround.javarebel.LoggerFactory;

import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation._NSUtilities;

public class D2WAccessor {
  private static final Logger log = LoggerFactory.getInstance();

  private static boolean initialized = false;
  private static boolean hasD2W = false;
  
  private static Field _entities;
  private static Field _defaultEntityNames;
  private static Field _lastDateRead;
  private static Field _model;
  private static Method flushCaches;
  
  private static void initialize() {
    if (initialized)
      return;
    initialized = true;

    Class<?> D2WUtils = _NSUtilities.classWithName("com.webobjects.directtoweb.D2WUtils");
    Class<?> D2WModel = _NSUtilities.classWithName("com.webobjects.directtoweb.D2WModel");

    if (D2WUtils == null) {
      return;
    }
    
    hasD2W = true;

    try {
      _entities = D2WUtils.getDeclaredField("_entities");
      _defaultEntityNames = D2WUtils.getDeclaredField("_defaultEntityNames");
      flushCaches = D2WUtils.getDeclaredMethod("flushCaches", new Class[0]);

      _entities.setAccessible(true);
      _defaultEntityNames.setAccessible(true);
      flushCaches.setAccessible(true);

      _lastDateRead = D2WModel.getDeclaredField("_lastDateRead");
      _model = D2WModel.getDeclaredField("_model");
      _lastDateRead.setAccessible(true);
      _model.setAccessible(true);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void flushCaches() {
    if (!hasD2W)
      return;
    log.echo("JRebel: Resetting D2W Caches");
    try {
      _entities.set(null, null);
      _defaultEntityNames.set(null, null);
      flushCaches.invoke(null, new Object[0]);
      _lastDateRead.set(_model.get(null), Long.valueOf(0));
    } catch (Exception e) {
      e.printStackTrace();
    }
    NSNotificationCenter.defaultCenter().postNotification("willCheckRules", null);
    NSNotificationCenter.defaultCenter().postNotification("resetModel", null);
    NSNotificationCenter.defaultCenter().postNotification("clearD2WRuleCache", null);
  }
  
  static {
    initialize();
  }
}

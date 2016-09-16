package er.wojrebel;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.zeroturnaround.javarebel.Logger;
import org.zeroturnaround.javarebel.LoggerFactory;

import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOCooperatingObjectStore;
import com.webobjects.eocontrol.EOKeyValueCoding;
import com.webobjects.eocontrol.EOObjectStoreCoordinator;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;
import com.webobjects.foundation.NSValidation;
import com.webobjects.foundation._NSUtilities;

public class WOJRebelEOModelReloadHandler {
  private static final String EO_ENTITY_CACHE_RESET = "EOEntityCacheReset";
  private static final Class<?>[] NotificationClassArray = new Class[] { NSNotification.class };
  private static boolean initialized = false;
  private static final WOJRebelEOModelReloadHandler instance = new WOJRebelEOModelReloadHandler();
  private static final Logger log = LoggerFactory.getInstance();


  private final Map<EOModel, Long> modelCache = Collections.synchronizedMap(new WeakHashMap<>());
  private final Map<EOObjectStoreCoordinator, EOModelGroup> oscCache = new WeakHashMap<>();
  private Field _ERXEntityCache;
  private Method _ERXEntityClassDescriptionCacheReset;
  private Object _ERXEntityClassDescriptionFactory;
  
  public static WOJRebelEOModelReloadHandler getInstance() {
    return instance;
  }

  public synchronized void updateLoadedModels(NSNotification n) {
    boolean reloaded = false;
    List<EOModel> modelList = new ArrayList<>(modelCache.keySet());
    for (EOModel model : modelList) {
      reloaded |= shouldUpdateModel(model);
    }
    if (reloaded) {
      flushCaches();
      for (EOModel model : modelList) {
        updateModel(model);
      }
    }
  }
  
  private boolean updateModel(EOModel model) {
    if (shouldUpdateModel(model)) {
      reloadModel(model);
      return true;
    }
    return false;
  }
  
  private boolean shouldUpdateModel(EOModel model) {
    if (modelCache.containsKey(model)) {
      if (lastModified(model) > modelCache.get(model)) {
        return true;
      }
    }
    return false;
  }

  private long lastModified(EOModel model) {
    URL url = model.pathURL();
    File modeld = new File (url.getPath());
    long lastmod = modeld.lastModified();
    for (File file : modeld.listFiles()) {
      if (file.lastModified() > lastmod)
        lastmod = file.lastModified();
    }
    return lastmod;
  }

  private void reloadModel(EOModel model) {
    log.echo("JRebel: reloading EOModel " + model.name() + " (" + model.hashCode() + ")");
    EOModel newModel = new EOModel(model.pathURL());
    EOModelGroup modelGroup = model.modelGroup();
    modelGroup.removeModel(model);
    modelGroup.addModel(newModel);
    for (Map.Entry<EOObjectStoreCoordinator, EOModelGroup> entry : oscCache.entrySet()) {
      if (modelGroup == entry.getValue()) {
        EOObjectStoreCoordinator osc = entry.getKey();
        for (Object obj : osc.cooperatingObjectStores()) {
          EOCooperatingObjectStore store = (EOCooperatingObjectStore) obj;
           osc.removeCooperatingObjectStore(store);
        }
        osc.invalidateAllObjects();
      }
    }
  }

  public void modelAdded(NSNotification n) {
    if (modelCache.containsKey(n.object()))
      return;
    EOModel model = (EOModel) n.object();
    if (model.pathURL() != null) {
      modelCache.put(model, lastModified(model));
    }
  }

  public void modelRemoved(NSNotification n) {
    modelCache.remove(n.object());
  }
  
  public void storeCoordinatorAdded(NSNotification n) {
    EOObjectStoreCoordinator osc = (EOObjectStoreCoordinator) n.object();
    oscCache.put(osc, EOModelGroup.modelGroupForObjectStoreCoordinator(osc));
  }
  
  public void storeCoordinatorRemoved(NSNotification n) {
    oscCache.remove(n.object());
  }

  @SuppressWarnings("unchecked")
  public void initialize() {
    if (initialized) {
      return;
    }

    initialized = true;
    NSNotificationCenter.defaultCenter().addObserver(this, new NSSelector("modelAdded", NotificationClassArray),
        EOModelGroup.ModelAddedNotification, null);
    NSNotificationCenter.defaultCenter().addObserver(this, new NSSelector("modelRemoved", NotificationClassArray),
        EOModelGroup.ModelInvalidatedNotification, null);
    NSNotificationCenter.defaultCenter().addObserver(this, new NSSelector("storeCoordinatorAdded", NotificationClassArray),
        EOObjectStoreCoordinator.CooperatingObjectStoreWasAddedNotification, null);
    NSNotificationCenter.defaultCenter().addObserver(this, new NSSelector("storeCoordinatorRemoved", NotificationClassArray),
        EOObjectStoreCoordinator.CooperatingObjectStoreWasRemovedNotification, null);

    Class<?> ERXClass = _NSUtilities.classWithName("er.extensions.foundation.ERXUtilities");
    if (ERXClass != null) {
      try {
        _ERXEntityCache = ERXClass.getDeclaredField("_entityNameEntityCache");
        _ERXEntityCache.setAccessible(true);

        ERXClass = _NSUtilities.classWithName("er.extensions.eof.ERXEntityClassDescription");
        Method factory = ERXClass.getDeclaredMethod("factory");
        _ERXEntityClassDescriptionFactory = factory.invoke(null, new Object[0]);

        ERXClass = _NSUtilities.classWithName("er.extensions.eof.ERXEntityClassDescription$Factory");
        _ERXEntityClassDescriptionCacheReset = ERXClass.getDeclaredMethod("reset");
        
        NSNotificationCenter.defaultCenter().addObserver(this, new NSSelector("resetWonderEntityCache", NotificationClassArray), EO_ENTITY_CACHE_RESET, null);
      } catch (Exception e) {
        e.printStackTrace();
      } 
    }
  }
  
  public void resetWonderEntityCache(NSNotification notification) {
    try {
      _ERXEntityCache.set(null, null);
      _ERXEntityClassDescriptionCacheReset.invoke(_ERXEntityClassDescriptionFactory, new Object[0]);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  private void flushCaches() {
    log.echo("JRebel: flushing EOModel caches");
    EOKeyValueCoding.DefaultImplementation._flushCaches();
    EOClassDescription.invalidateClassDescriptionCache();
    D2WAccessor.flushCaches();
    NSValidation.DefaultImplementation._flushCaches();
    
    NSNotificationCenter.defaultCenter().postNotification(EO_ENTITY_CACHE_RESET, null);
  }
}

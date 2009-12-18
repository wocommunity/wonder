package er.wojrebel;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import org.zeroturnaround.javarebel.Logger;
import org.zeroturnaround.javarebel.LoggerFactory;

import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOCooperatingObjectStore;
import com.webobjects.eocontrol.EOObjectStoreCoordinator;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;
import com.webobjects.foundation.NSValidation;
import com.webobjects.foundation._NSUtilities;

public class WOJRebelEOModelReloadHandler {
  private static boolean initialized = false;
  private static final WOJRebelEOModelReloadHandler instance = new WOJRebelEOModelReloadHandler();
  private static final Logger log = LoggerFactory.getInstance();


  private final Map<EOModel, Long> modelCache = Collections.synchronizedMap(new WeakHashMap<EOModel, Long>());
  private final Map<EOObjectStoreCoordinator, EOModelGroup> oscCache = new WeakHashMap<EOObjectStoreCoordinator, EOModelGroup>();
  private Field erxEntityCache;
  
  public static WOJRebelEOModelReloadHandler getInstance() {
    return instance;
  }

  public synchronized void updateLoadedModels(NSNotification n) {
    boolean reloaded = false;
    for (EOModel model : new ArrayList<EOModel>(modelCache.keySet())) {
      reloaded |= updateModel(model);
    }
    if (reloaded) {
      flushCaches();
    }
  }

  private boolean updateModel(EOModel model) {
    if (modelCache.containsKey(model)) {
      if (lastModified(model) > modelCache.get(model)) {
        reloadModel(model);
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
    NSNotificationCenter.defaultCenter().addObserver(this, new NSSelector("modelAdded", new Class[] { NSNotification.class }),
        EOModelGroup.ModelAddedNotification, null);
    NSNotificationCenter.defaultCenter().addObserver(this, new NSSelector("modelRemoved", new Class[] { NSNotification.class }),
        EOModelGroup.ModelInvalidatedNotification, null);
    NSNotificationCenter.defaultCenter().addObserver(this, new NSSelector("storeCoordinatorAdded", new Class[] { NSNotification.class }),
        EOObjectStoreCoordinator.CooperatingObjectStoreWasAddedNotification, null);
    NSNotificationCenter.defaultCenter().addObserver(this, new NSSelector("storeCoordinatorRemoved", new Class[] { NSNotification.class }),
        EOObjectStoreCoordinator.CooperatingObjectStoreWasRemovedNotification, null);

    Class<?> ERXUtilities = _NSUtilities.classWithName("er.extensions.foundation.ERXUtilities");
    if (ERXUtilities != null) {
      try {
        erxEntityCache = ERXUtilities.getDeclaredField("_entityNameEntityCache");
        erxEntityCache.setAccessible(true);
      } catch (Exception e) {
        e.printStackTrace();
      } 
    }
  }
  
  private void flushCaches() {
    EOClassDescription.invalidateClassDescriptionCache();
    D2WAccessor.flushCaches();
    NSValidation.DefaultImplementation._flushCaches();

    if (erxEntityCache != null) {
      try {
        erxEntityCache.set(null, null);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}

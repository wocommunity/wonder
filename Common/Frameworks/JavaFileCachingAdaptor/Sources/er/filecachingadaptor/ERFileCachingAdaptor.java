//
//  ERFileCachingAdaptor.java
//
//  Created by Thomas Burkholder on Thu May 10, 2005.
//
package er.filecachingadaptor;

import com.webobjects.eoaccess.EOAdaptorContext;

import er.extensions.ERXForwardingAdaptor;

/**
 * ERFileCachingAdaptor is not a flat-file adaptor.  Ok, it really is, but it's a poor-man's flat file adaptor.  A very poor
 * man's.<p>
 *
 * The purpose of ERFileCachingAdaptor is to provide a caching layer for shared objects.  It's motivated by a problem we've
 * had wherein, bouncing 400 application instances or so all at once really hammers the database when we load all the
 * shared object tables all at once in each instance.<p>
 *
 * This is totally avoidable.  ERFileCachingAdaptor may be used in one of two ways - it may be used as a traditional adaptor,
 * that you set in the model connection info dictionary.  This has a couple of problems - principally, it isn't really what
 * you want, as your shared entities may be scattered among all sorts of different models, which will contain both shared
 * and non-shared entities.  So, the other way that ERFileCachingAdaptor may be used is, you can override the adaptor at runtime
 * in your application, by resetting the adaptor name in each model before the adaptor has been loaded.  It can be tricky to
 * find a place early enough in the model loading process.  I use the class constructor for my Application class - the method
 * to call is ERFileCachingUtilities.subvertAllModels().  This method swizzles all the models to believe they use the
 * ERFileCachingAdaptor.  Doing it this way  will also ensure that the model will continue to be easily editable in EOModeler.<p>
 *
 * The way the adaptor specifically works is, when you are using the adaptor (which is really just a code-clean way of getting
 * the adaptor to use ERFileCachingAdaptorChannel), it looks for a file (defined by the ERFileCachingAdaptor.FileCache system property)
 * and loads the shared objects from there.  To generate the file, call generateFileCacheForAllModels() - this will generate the
 * cache file in the location determined by the ERFileCachingAdaptor.FileCache property.<p>
 *
 * to disable the adaptor completely (and dynamically), set the ERFileCachingAdaptor.disabled=true property.<p>
 *
 * to disable caching for a specific entity only, set the bloodyWellDontCacheMe property in the userInfo of that Entity, ie:<br>
 * <pre>
 *     userInfo = {
 *        bloodyWellDontCacheMe="true";
 *     };
 * </pre>
 * <p>
 * 
 * alternately, you can set a property - ERFileCachingAdapter.disableEntityCachingFor_MZGenre=true for instance.<p>
 *
 */
public class ERFileCachingAdaptor extends ERXForwardingAdaptor {

    public ERFileCachingAdaptor(String name) {
        super(name);
    }

    // Stupid hack - not allowed to know what model we're being asked to create for.
    protected String forwardedAdaptorName() {
        return "JDBC";
    }
    
//    public EOSchemaSynchronizationFactory schemaSynchronizationFactory() {
//    	throw new UnsupportedOperationException("You cannot request a schemaSynchronizationFactory for ERFileCachingAdaptor.");
//    }

    public EOAdaptorContext createAdaptorContext() {
        EOAdaptorContext raw = super.createAdaptorContext();
        EOAdaptorContext cooked = new ERFileCachingAdaptorContext(this,raw);
        return cooked;
    }
}

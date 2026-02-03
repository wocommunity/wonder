package er.r2d2w.assignments;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.D2WModel;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eocontrol.EOKeyValueArchiver;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSSet;

import er.directtoweb.assignments.ERDAssignment;
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.foundation.ERXValueUtilities;

public class R2DDefaultRSSAssignment extends ERDAssignment {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	// EOModel file keys
	public static final String RSS_KEY = "rssKey";
	public static final String MIME_TYPES = "mimeTypes";
	
	public static final String DESCRIPTION_KEY = "description";
	public static final String ENCLOSURE_KEY = "enclosure";
	public static final String PUB_DATE_KEY = "pubDate";
	public static final String TITLE_KEY = "title";
	
	public static final String AUTHOR_KEY = "podcastAuthor";
	public static final String DETAIL_KEY = "podcastDetail";
	public static final String DURATION_KEY = "podcastDuration";
	public static final String EXPLICIT_KEY = "podcastExplicit";
	
	
	// Rule system keys
	public static final String HAS_PODCAST = "hasPodcast";
	
	public static final String ITEM_DESCRIPTION_KEY = "rssItemDescription";
	public static final String ITEM_ENCLOSURE_KEY = "rssItemEnclosure";
	public static final String ITEM_PUB_DATE_KEY = "rssItemPubDate";
	public static final String ITEM_TITLE_KEY = "rssItemTitle";

	public static final String PODCAST_AUTHOR_KEY = "rssPodcastAuthor";
	public static final String PODCAST_DETAIL = "rssPodcastDetail";
	public static final String PODCAST_DURATION_KEY = "rssPodcastDuration";
	public static final String PODCAST_EXPLICIT_KEY = "rssPodcastExplicit";

	// Dependent Keys
	private static final NSArray<String> dependentKeys = new NSArray<String>(new String[] {D2WModel.EntityKey});
	private static final NSArray<String> dpkDependentKeys = new NSArray<String>(new String[] {D2WModel.EntityKey, D2WModel.DynamicPageKey, D2WModel.TaskKey});

	// Display Property Keys
	private static final NSArray<String> rssPropertyKeys = new NSArray<String>(new String[] {ITEM_TITLE_KEY, 
				ITEM_DESCRIPTION_KEY, ITEM_ENCLOSURE_KEY, ITEM_PUB_DATE_KEY});
	private static final NSArray<String> rssPodcastKeys = new NSArray<String>(new String[]{PODCAST_AUTHOR_KEY, 
				PODCAST_DETAIL, PODCAST_DURATION_KEY, PODCAST_EXPLICIT_KEY});
	
	private static final NSSet<String> podcastMimeTypes = new NSSet<String>(
				new String[] {"application/pdf", "audio/mpeg", "audio/mp4", "video/mp4", "video/quicktime"});
	public static final String TITLE_DEFAULT_KEY = "userPresentableDescription";
	
	public R2DDefaultRSSAssignment(EOKeyValueUnarchiver u) {
		super(u);
	}

	public R2DDefaultRSSAssignment(String key, Object value) {
		super(key, value);
	}

	public NSArray<String> dependentKeys(String keyPath) {
		return (keyPath.equals(D2WModel.DisplayPropertyKeysKey))?dpkDependentKeys:dependentKeys;
	}
	
	public void encodeWithKeyValueArchiver(EOKeyValueArchiver archiver) {
		super.encodeWithKeyValueArchiver(archiver);
	}

	public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver unarchiver) {
		return new R2DDefaultRSSAssignment(unarchiver);
	}
	
	public NSArray<String> displayPropertyKeys(D2WContext c) {
		NSMutableArray<String> result = new NSMutableArray<String>();
		for(String key: rssPropertyKeys) {
			String value = (String)c.valueForKey(key);
			if(!ERXStringUtilities.stringIsNullOrEmpty(value)) {
				result.add(value);
			}
		}
		if(ERXValueUtilities.booleanValueWithDefault(c.valueForKey(HAS_PODCAST), false)) {
			for(String key: rssPodcastKeys) {
				String value = (String)c.valueForKey(key);
				if(!ERXStringUtilities.stringIsNullOrEmpty(value)) {
					result.add(value);
				}
			}
		}
		return result.immutableClone();
	}
	
	@SuppressWarnings("unchecked")
	public Boolean hasPodcast(D2WContext c) {
		Boolean result = Boolean.FALSE;
		String enclosureKey = (String)c.valueForKey(ITEM_ENCLOSURE_KEY);
		if(!ERXStringUtilities.stringIsNullOrEmpty(enclosureKey)) {
			EORelationship r = c.entity().relationshipNamed(enclosureKey);
			if(r.userInfo() != null) {
				NSArray typeArray = ERXValueUtilities.arrayValueWithDefault(r.userInfo().get(MIME_TYPES), NSArray.EmptyArray);
				if(typeArray.count() > 0) {
					NSSet mimeTypes = new NSSet(typeArray);
					result = podcastMimeTypes.setByUnioningSet(mimeTypes).equals(podcastMimeTypes)?Boolean.TRUE:Boolean.FALSE;
				}
			}
		}
		return result;
	}
	
	public String rssItemDescription(D2WContext c) {
		return propertyNameForKeyInContext(DESCRIPTION_KEY, c);
	}

	public String rssItemEnclosure(D2WContext c) {
		return propertyNameForKeyInContext(ENCLOSURE_KEY, c);
	}

	public String rssItemPubDate(D2WContext c) {
		return propertyNameForKeyInContext(PUB_DATE_KEY, c);
	}

	public String rssItemTitle(D2WContext c) {
		String title = propertyNameForKeyInContext(TITLE_KEY, c);
		return (title == null)?TITLE_DEFAULT_KEY:title;
	}

	public String rssPodcastAuthor(D2WContext c) {
		return propertyNameForKeyInContext(AUTHOR_KEY, c);
	}

	public String rssPodcastDetail(D2WContext c) {
		return propertyNameForKeyInContext(DETAIL_KEY, c);
	}

	public String podcastDuration(D2WContext c) {
		return propertyNameForKeyInContext(DURATION_KEY, c);
	}

	public String podcastExplicit(D2WContext c) {
		return propertyNameForKeyInContext(EXPLICIT_KEY, c);
	}

	private String propertyNameForKeyInContext(String key, D2WContext c) {
		String result = null;
		EOEntity entity = c.entity();
		if(entity != null) {
			for(EORelationship r: entity.relationships()) {
				if(r.userInfo() != null && key.equals(r.userInfo().get(RSS_KEY))) {
					return r.name();
				}
			}
			for(EOAttribute a: entity.attributes()) {
				if(a.userInfo() != null && key.equals(a.userInfo().get(RSS_KEY))) {
					return a.name();
				}
			}
		}
		return result;
	}
}

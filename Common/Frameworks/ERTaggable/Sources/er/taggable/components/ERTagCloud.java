package er.taggable.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSComparator;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.ERXArrayUtilities;
import er.extensions.ERXComponent;
import er.extensions.ERXEC;
import er.extensions.ERXQ;
import er.taggable.ERTaggableEntity;

/**
 * ERTagCloud provides a simple tag cloud view.  The contents of this
 * component will be used as the renderer for each tag.  You can use
 * the example css file ERTagCloud.css for default styling.  By default
 * the class names will be "tagCloud1", "tagCloud2", etc.
 * 
 * As an example:
 *
 * <code>
 * &lt;wo:ERTagCloud entityName = "InventoryItem" tag = "$tag" tagClass = "$tagClass"&gt;
 *   &lt;wo:WOGenericContainer elementName = "span" class = "$tagClass"&gt;&lt;wo:str value = "$tag"/&gt;&lt;/wo:WOGenericContainer&gt;
 * &lt;/wo:ERTagCloud&gt;
 * </code>
 * 
 * @binding tag the name of the current tag being rendered
 * @binding tagClass the class name of the current tag
 * @binding entityName the name of the entity to load tags for
 * @binding limit the maximum number of tags to show
 * @binding minimum the minimum tag count required for a tag to be shown
 * @binding categories the tag category names to use (NSArray of Strings)
 * @binding categoryCount the number of categories to split into (default 5)
 * @binding tagClassPrefix the prefix to prepend to the tag cloud class name ("tagCloud" by default)
 * 
 * @author mschrag
 */
public class ERTagCloud extends ERXComponent {
  private NSDictionary<String, String> _tagCloud;
  private String _repetitionTag;

  public ERTagCloud(WOContext context) {
    super(context);
  }

  @Override
  public boolean synchronizesVariablesWithBindings() {
    return false;
  }

  @SuppressWarnings( { "cast", "unchecked" })
  public NSArray<String> tagNames() {
    return (NSArray<String>) ERXArrayUtilities.sortedArrayUsingComparator(tagCloud().allKeys(), NSComparator.AscendingStringComparator);
  }

  public String entityName() {
    return stringValueForBinding("entityName");
  }

  public void setRepetitionTag(String repetitionTag) {
    _repetitionTag = repetitionTag;
    setValueForBinding(_repetitionTag, "tag");
    setValueForBinding(tagClass(), "tagClass");
  }

  public String repetitionTag() {
    return _repetitionTag;
  }

  public String tagClass() {
    return _tagCloud.objectForKey(_repetitionTag);
  }

  public int minimum() {
    return intValueForBinding("minimum", -1);
  }

  public int limit() {
    return intValueForBinding("limit", 100);
  }

  public int categoryCount() {
    return intValueForBinding("categoryCount", 5);
  }

  @SuppressWarnings("unchecked")
  public NSArray<String> categories() {
    return (NSArray<String>) valueForBinding("categories");
  }
  
  public String tagClassPrefix() {
    return stringValueForBinding("tagClassPrefix", "tagCloud");
  }

  public NSDictionary<String, String> tagCloud() {
    if (_tagCloud == null) {
      EOEditingContext editingContext = ERXEC.newEditingContext();
      ERTaggableEntity<?> taggableEntity = ERTaggableEntity.taggableEntity(entityName());
      NSDictionary<String, Integer> tagCount;
      int limit = limit();
      int minimum = minimum();
      if (limit == -1 && minimum == -1) {
        tagCount = taggableEntity.tagCount(editingContext, -1);
      }
      else if (minimum == -1) {
        tagCount = taggableEntity.tagCount(editingContext, limit);
      }
      else {
        tagCount = taggableEntity.tagCount(editingContext, ERXQ.GTEQ, minimum, limit);
      }
      NSArray<String> categories = categories();
      if (categories == null) {
        NSMutableArray<String> mutableCategories = new NSMutableArray<String>();
        int categoryCount = categoryCount();
        for (int i = 1; i <= categoryCount; i++) {
          mutableCategories.addObject(tagClassPrefix() + i);
        }
        categories = mutableCategories;
      }
      _tagCloud = taggableEntity.cloud(tagCount, categories);
    }
    return _tagCloud;
  }
}
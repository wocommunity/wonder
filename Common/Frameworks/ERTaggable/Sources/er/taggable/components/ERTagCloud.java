package er.taggable.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSComparator;
import com.webobjects.foundation.NSDictionary;

import er.extensions.ERXArrayUtilities;
import er.extensions.ERXComponent;
import er.extensions.ERXEC;
import er.taggable.ERTaggableEntity;

/**
 * ERTagCloud provides a simple tag cloud view.  The contents of this
 * component will be used as the renderer for each tag.  You can use
 * the example css file ERTagCloud.css for default styling.
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

  @SuppressWarnings({ "cast", "unchecked" })
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

  public NSDictionary<String, String> tagCloud() {
    if (_tagCloud == null) {
      EOEditingContext editingContext = ERXEC.newEditingContext();
      ERTaggableEntity<?> taggableEntity = new ERTaggableEntity(entityName());
      NSDictionary<String, Integer> tagCount = taggableEntity.tagCount(editingContext, 100);
      _tagCloud = taggableEntity.cloud(tagCount, new NSArray<String>(new String[] { "tagCloud1", "tagCloud2", "tagCloud3", "tagCloud4", "tagCloud5" }));
    }
    return _tagCloud;
  }
}
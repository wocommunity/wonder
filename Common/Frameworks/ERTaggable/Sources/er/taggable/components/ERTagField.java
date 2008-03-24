package er.taggable.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSComparator;
import com.webobjects.foundation.NSDictionary;

import er.extensions.ERXArrayUtilities;
import er.extensions.ERXQ;
import er.extensions.ERXWOContext;
import er.taggable.ERTaggable;

/**
 * ERTagField implements a fancy del.icio.us-style javascript-enabled
 * tagging text field with tab completion, toggling pickers, etc.  If
 * you have a large tagset, you should not use this component, because
 * it renders all of the available tags for the user to choose from. 
 *  
 * @author mschrag
 * @binding taggable the ERTaggable to manage
 * @binding limit the maximum number of tags to show
 * @binding minimum the minimum tag count required for a tag to be shown
 */
public class ERTagField extends er.extensions.ERXComponent {
  private String _id;
  private NSArray<String> _availableTags;
  private String _tags;
  private boolean _tagsChanged;
  private ERTaggable<?> _taggable;

  public ERTagField(WOContext context) {
    super(context);
  }

  protected void clearCacheIfNecessary() {
    ERTaggable<?> taggable = taggable();
    if (taggable == null || (taggable != _taggable && !taggable.equals(_taggable))) {
      _tags = null;
      _availableTags = null;
      _taggable = taggable;
    }
  }
  
  public ERTaggable<?> taggable() {
    return (ERTaggable<?>) valueForBinding("taggable");
  }

  public int minimum() {
    return intValueForBinding("minimum", -1);
  }

  public int limit() {
    return intValueForBinding("limit", -1);
  }

  @SuppressWarnings("unchecked")
  public NSArray<String> availableTags() {
    clearCacheIfNecessary();
    if (_availableTags == null) {
      EOEditingContext editingContext = taggable().item().editingContext();
      int minimum = minimum();
      int limit = limit();
      NSArray<String> availableTags;
      if (minimum == -1 && limit == -1) {
        availableTags = taggable().taggableEntity().fetchAllTags(editingContext);
      }
      else if (minimum == -1) {
        NSDictionary<String, Integer> tagCount = taggable().taggableEntity().tagCount(editingContext, limit);
        availableTags = tagCount.allKeys();
      }
      else {
        NSDictionary<String, Integer> tagCount = taggable().taggableEntity().tagCount(editingContext, ERXQ.GTEQ, minimum, limit);
        availableTags = tagCount.allKeys();
      }
      _availableTags = ERXArrayUtilities.sortedArrayUsingComparator(availableTags, NSComparator.AscendingStringComparator);
    }
    return _availableTags;
  }

  public String javascriptAvailableTags() {
    StringBuffer sb = new StringBuffer();
    sb.append("[");
    NSArray<String> availableTags = availableTags();
    if (availableTags.count() > 0) {
      sb.append("'");
      sb.append(availableTags.componentsJoinedByString("','"));
      sb.append("'");
    }
    sb.append("]");
    return sb.toString();
  }

  public String tags() {
    clearCacheIfNecessary();
    if (_tags == null) {
      _tags = taggable().tagNames().componentsJoinedByString(" ");
      if (_tags.length() > 0) {
        _tags += " ";
      }
    }
    return _tags;
  }

  public void setTags(String tags) {
    clearCacheIfNecessary();
    if (tags == null || (tags != _tags && !tags.equals(_tags))) {
      taggable().setTags(tags);
      _tags = tags;
      _availableTags = null;
    }
  }

  public String id() {
    if (_id == null) {
      _id = stringValueForBinding("id");
      if (_id == null) {
        _id = ERXWOContext.toSafeElementID(context().elementID());
      }
    }
    return _id;
  }

  public String tagsID() {
    return id() + "_tags";
  }

  @Override
  public boolean synchronizesVariablesWithBindings() {
    return false;
  }

  @Override
  public void appendToResponse(WOResponse response, WOContext context) {
    ERXWOContext.addScriptResourceInHead(context, response, "Ajax", "prototype.js");
    ERXWOContext.addScriptResourceInHead(context, response, "ERTaggable", "ERTagField.js");
    ERXWOContext.addStylesheetResourceInHead(context, response, "ERTaggable", "ERTagField.css");
    super.appendToResponse(response, context);
  }
}
package er.taggable.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSComparator;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.appserver.ERXResponseRewriter;
import er.extensions.eof.ERXQ;
import er.extensions.foundation.ERXArrayUtilities;
import er.extensions.foundation.ERXStringUtilities;
import er.taggable.ERTaggable;
import er.taggable.ERTaggableEntity;
import er.taggable.model.ERTag;

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
 * @binding size the size of the text field. Defaults to 60.
 * @binding class the css class of the text field
 * @binding style the css style of the text field
 * @binding id the dom ID of the text field
 * @binding additionalQualifier an optional restricting qualifier
 */
public class ERTagField extends er.extensions.components.ERXComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

  private String _id;
  private NSArray<String> _availableTags;
  private String _tags;
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

  public String size() {
    String size;
    if (hasBinding("size")) {
      size = stringValueForBinding("size");
    }
    else {
      size = "60";
    }
    return size;
  }
  
  public int minimum() {
    return intValueForBinding("minimum", -1);
  }

  public int limit() {
    return intValueForBinding("limit", -1);
  }
  
  public EOQualifier additionalQualifier() {
    return (EOQualifier) valueForBinding("additionalQualifier");
  }

  public NSArray<String> availableTags() {
    clearCacheIfNecessary();
    if (_availableTags == null) {
      EOEditingContext editingContext = taggable().item().editingContext();
      int minimum = minimum();
      int limit = limit();
      NSArray<String> availableTags;
      if (minimum == -1) {
        NSDictionary<String, Integer> tagCount = taggable().taggableEntity().tagCount(editingContext, limit, additionalQualifier());
        availableTags = tagCount.allKeys();
      }
      else {
        NSDictionary<String, Integer> tagCount = taggable().taggableEntity().tagCount(editingContext, ERXQ.GTEQ, minimum, limit, additionalQualifier());
        availableTags = tagCount.allKeys();
      }
      _availableTags = ERXArrayUtilities.sortedArrayUsingComparator(availableTags, NSComparator.AscendingStringComparator);
    }
    return _availableTags;
  }

  public String javascriptAvailableTags() {
    StringBuilder sb = new StringBuilder();
    sb.append('[');
    NSMutableArray<String> availableTags = availableTags().mutableClone();
    int availableTagsCount = availableTags.count();
    if (availableTagsCount > 0) {
      for (int tagNum = 0; tagNum < availableTagsCount; tagNum ++) {
        String availableTag = availableTags.objectAtIndex(tagNum);
        availableTag = availableTag.replaceAll("'", "\\\\'");
        availableTag = ERTag.escapeTagNamed(availableTag);
        availableTags.replaceObjectAtIndex(availableTag, tagNum);
      }
      sb.append('\'');
      sb.append(availableTags.componentsJoinedByString("','"));
      sb.append('\'');
    }
    sb.append(']');
    return sb.toString();
  }

  public String tags() {
    clearCacheIfNecessary();
    if (_tags == null || true) {
      String separator = " ";
      NSMutableArray<String> tagNames = taggable().tagNames().mutableClone();
      if (ERTaggableEntity.isWhitespaceSeparator(separator)) {
        int tagsCount = tagNames.count();
        for (int tagNum = 0; tagNum < tagsCount; tagNum ++) {
          String tagName = tagNames.objectAtIndex(tagNum);
          tagNames.replaceObjectAtIndex(ERTag.escapeTagNamed(tagName), tagNum);
        }
      }
      _tags = tagNames.componentsJoinedByString(separator);
      if (_tags.length() > 0) {
        _tags += separator;
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
        _id = ERXStringUtilities.safeIdentifierName(context().elementID());
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
	ERXResponseRewriter.addScriptResourceInHead(response, context, "Ajax", "prototype.js");
    ERXResponseRewriter.addScriptResourceInHead(response, context, "ERTaggable", "ERTagField.js");
    ERXResponseRewriter.addStylesheetResourceInHead(response, context, "ERTaggable", "ERTagField.css");
    super.appendToResponse(response, context);
  }
}
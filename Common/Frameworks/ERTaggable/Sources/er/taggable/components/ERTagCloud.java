package er.taggable.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

import er.extensions.ERXComponent;
import er.extensions.ERXEC;
import er.taggable.ERTaggableEntity;

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

  public String entityName() {
    return stringValueForBinding("entityName");
  }

  public void setRepetitionTag(String repetitionTag) {
    _repetitionTag = repetitionTag;
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
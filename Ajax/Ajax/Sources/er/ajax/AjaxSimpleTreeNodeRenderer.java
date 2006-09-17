package er.ajax;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

public class AjaxSimpleTreeNodeRenderer extends WOComponent {
  private ITreeNode _treeNode;

  public AjaxSimpleTreeNodeRenderer(WOContext context) {
    super(context);
  }

  public void setTreeNode(ITreeNode treeNode) {
    _treeNode = treeNode;
  }
  
  public ITreeNode treeNode() {
    return _treeNode;
  }
}
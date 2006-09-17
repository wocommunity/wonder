package er.ajax;

import com.webobjects.foundation.NSMutableSet;

public class AjaxTreeModel {
  private ITreeNode _rootTreeNode;
  private NSMutableSet _expandedTreeNodes;

  public AjaxTreeModel(ITreeNode rootTreeNode) {
    _rootTreeNode = rootTreeNode;
    _expandedTreeNodes = new NSMutableSet();
  }
  
  public ITreeNode rootTreeNode() {
    return _rootTreeNode;
  }
  
  public boolean isExpanded(ITreeNode treeNode) {
    return _expandedTreeNodes.containsObject(treeNode);
  }
  
  public void setExpanded(ITreeNode treeNode, boolean expanded) {
    if (expanded) {
      _expandedTreeNodes.addObject(treeNode);
    }
    else {
      _expandedTreeNodes.removeObject(treeNode);
    }
  }
}

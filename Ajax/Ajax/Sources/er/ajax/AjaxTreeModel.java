package er.ajax;

import com.webobjects.foundation.NSMutableSet;

public class AjaxTreeModel {
  private ITreeNode _rootTreeNode;
  private NSMutableSet _expandedTreeNodes;
  private String _treeNodeRenderer;
  private String _collapsedImage;
  private String _collapsedImageFramework;
  private String _expandedImage;
  private String _expandedImageFramework;
  private String _leafImage;
  private String _leafImageFramework;

  public AjaxTreeModel() {
    _expandedTreeNodes = new NSMutableSet();
    _collapsedImage = "collapsed.gif";
    _collapsedImageFramework = "Ajax";
    _expandedImage = "expanded.gif";
    _expandedImageFramework = "Ajax";
    _leafImage = "leaf.gif";
    _leafImageFramework = "Ajax";
    _treeNodeRenderer = AjaxSimpleTreeNodeRenderer.class.getName();
  }

  public void setRootTreeNode(ITreeNode rootTreeNode) {
    if (rootTreeNode != _rootTreeNode) {
      _rootTreeNode = rootTreeNode;
      _expandedTreeNodes.removeAllObjects();
    }
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
  
  public void collapseAll() {
    _expandedTreeNodes.removeAllObjects();
  }
  
  public void setTreeNodeRenderer(String treeNodeRenderer) {
    _treeNodeRenderer = treeNodeRenderer;
  }

  public String treeNodeRenderer() {
    return _treeNodeRenderer;
  }

  public String collapsedImage() {
    return _collapsedImage;
  }

  public void setCollapsedImage(String collapsedImage) {
    _collapsedImage = collapsedImage;
  }

  public String collapsedImageFramework() {
    return _collapsedImageFramework;
  }

  public void setCollapsedImageFramework(String collapsedImageFramework) {
    _collapsedImageFramework = collapsedImageFramework;
  }

  public String expandedImage() {
    return _expandedImage;
  }

  public void setExpandedImage(String expandedImage) {
    _expandedImage = expandedImage;
  }

  public String expandedImageFramework() {
    return _expandedImageFramework;
  }

  public void setExpandedImageFramework(String expandedImageFramework) {
    _expandedImageFramework = expandedImageFramework;
  }

  public NSMutableSet expandedTreeNodes() {
    return _expandedTreeNodes;
  }

  public void setExpandedTreeNodes(NSMutableSet expandedTreeNodes) {
    _expandedTreeNodes = expandedTreeNodes;
  }

  public String leafImage() {
    return _leafImage;
  }

  public void setLeafImage(String leafImage) {
    _leafImage = leafImage;
  }

  public String leafImageFramework() {
    return _leafImageFramework;
  }

  public void setLeafImageFramework(String leafImageFramework) {
    _leafImageFramework = leafImageFramework;
  }
}

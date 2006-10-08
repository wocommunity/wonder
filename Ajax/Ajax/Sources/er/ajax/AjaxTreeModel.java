package er.ajax;

import java.util.Enumeration;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableSet;

public class AjaxTreeModel {
  private Object _rootTreeNode;
  private NSMutableSet _expandedTreeNodes;
  private NSMutableSet _collapsedTreeNodes;
  private String _parentTreeNodeKeyPath;
  private String _childrenTreeNodesKeyPath;
  private String _isLeafKeyPath;
  private boolean _allExpanded;
  private boolean _rootExpanded;

  public AjaxTreeModel() {
    _expandedTreeNodes = new NSMutableSet();
    _collapsedTreeNodes = new NSMutableSet();
  }

  public void setRootExpanded(boolean rootExpanded) {
    if (_rootExpanded != rootExpanded) {
      _rootExpanded = rootExpanded;
      expandRootIfNecessary();
    }
  }

  public boolean isRootExpanded() {
    return _rootExpanded;
  }

  public void setAllExpanded(boolean allExpanded) {
    _allExpanded = allExpanded;
  }

  public boolean isAllExpanded() {
    return _allExpanded;
  }

  public void setParentTreeNodeKeyPath(String parentTreeNodeKeyPath) {
    _parentTreeNodeKeyPath = parentTreeNodeKeyPath;
  }

  public String parentTreeNodeKeyPath() {
    return _parentTreeNodeKeyPath;
  }

  public void setChildrenTreeNodesKeyPath(String childrenTreeNodesKayPath) {
    _childrenTreeNodesKeyPath = childrenTreeNodesKayPath;
  }

  public String childrenTreeNodesKeyPath() {
    return _childrenTreeNodesKeyPath;
  }
  
  public void setIsLeafKeyPath(String isLeafKeyPath) {
    _isLeafKeyPath = isLeafKeyPath;
  }
  
  public String isLeafKeyPath() {
    return _isLeafKeyPath;
  }

  public void setRootTreeNode(Object rootTreeNode) {
    if (rootTreeNode != _rootTreeNode) {
      _rootTreeNode = rootTreeNode;
      _expandedTreeNodes.removeAllObjects();
      _collapsedTreeNodes.removeAllObjects();
      expandRootIfNecessary();
    }
  }

  public Object rootTreeNode() {
    return _rootTreeNode;
  }

  public boolean isLeaf(Object treeNode) {
    boolean isLeaf;
    if (_isLeafKeyPath == null) {
      NSArray childrenTreeNodes = childrenTreeNodes(treeNode);
      isLeaf = childrenTreeNodes == null || childrenTreeNodes.size() == 0;
    }
    else {
      Boolean isLeafBoolean = (Boolean) NSKeyValueCodingAdditions.Utility.valueForKeyPath(treeNode, _isLeafKeyPath);
      isLeaf = isLeafBoolean.booleanValue();
    }
    return isLeaf;
  }
  
  public boolean isExpanded(Object treeNode) {
    boolean expanded;
    if (_allExpanded) {
      expanded = !_collapsedTreeNodes.containsObject(treeNode);
    }
    else {
      expanded = _expandedTreeNodes.containsObject(treeNode);
    }
    return expanded;
  }

  public void setExpanded(Object treeNode, boolean expanded) {
    if (_rootExpanded && treeNode == _rootTreeNode && !expanded) {
      return;
    }
    
    if (expanded) {
      if (_allExpanded) {
        _collapsedTreeNodes.removeObject(treeNode);
      }
      else {
        _expandedTreeNodes.addObject(treeNode);
      }
    }
    else {
      if (_allExpanded) {
        _collapsedTreeNodes.addObject(treeNode);
      }
      else {
        _expandedTreeNodes.removeObject(treeNode);
      }
    }
  }

  public void collapseAll() {
    if (_allExpanded) {
      _allExpanded = false;
    }
    clearExpandedAndCollapsed();
  }

  public void expandAll() {
    if (!_allExpanded) {
      _allExpanded = true;
    }
    clearExpandedAndCollapsed();
  }

  protected void clearExpandedAndCollapsed() {
    _collapsedTreeNodes.removeAllObjects();
    _expandedTreeNodes.removeAllObjects();
    expandRootIfNecessary();
  }
  
  protected void expandRootIfNecessary() {
    if (_rootExpanded && _rootTreeNode != null) {
      setExpanded(_rootTreeNode, true);
    }
  }

  public int level(Object treeNode) {
    Object parentTreeNode = treeNode;
    int level;
    for (level = 0; parentTreeNode != null; parentTreeNode = parentTreeNode(parentTreeNode), level++) {
      // do nothing
    }
    return level - 1;
  }

  public Object parentTreeNode(Object node) {
    Object parentTreeNode = null;
    if (node != null) {
      parentTreeNode = NSKeyValueCodingAdditions.Utility.valueForKeyPath(node, _parentTreeNodeKeyPath);
    }
    return parentTreeNode;
  }

  public NSArray childrenTreeNodes(Object node) {
    NSArray childrenTreeNodes = (NSArray) NSKeyValueCodingAdditions.Utility.valueForKeyPath(node, _childrenTreeNodesKeyPath);
    return childrenTreeNodes;
  }

  public Enumeration depthFirstEnumeration(Object node, boolean enumeratedClosedNodes) {
    return new DepthFirstEnumeration(node, enumeratedClosedNodes);
  }

  public Enumeration rootDepthFirstEnumeration(boolean enumeratedClosedNodes) {
    return new DepthFirstEnumeration(_rootTreeNode, enumeratedClosedNodes);
  }

  protected class DepthFirstEnumeration implements Enumeration {
    private Object _rootNode;
    private Enumeration _childrenEnumeration;
    private Enumeration _subtreeEnumeration;
    private boolean _enumerateClosedNodes;

    public DepthFirstEnumeration(Object rootNode, boolean enumerateClosedNodes) {
      _rootNode = rootNode;
      _enumerateClosedNodes = enumerateClosedNodes;
      if (_enumerateClosedNodes || AjaxTreeModel.this.isExpanded(rootNode)) {
        _childrenEnumeration = AjaxTreeModel.this.childrenTreeNodes(rootNode).objectEnumerator();
      }
      _subtreeEnumeration = NSArray.EmptyArray.objectEnumerator();
    }

    public boolean hasMoreElements() {
      return _rootNode != null;
    }

    public Object nextElement() {
      Object retval;
      if (_subtreeEnumeration.hasMoreElements()) {
        retval = _subtreeEnumeration.nextElement();
      }
      else if (_childrenEnumeration != null && _childrenEnumeration.hasMoreElements()) {
        _subtreeEnumeration = new DepthFirstEnumeration(_childrenEnumeration.nextElement(), _enumerateClosedNodes);
        retval = _subtreeEnumeration.nextElement();
      }
      else {
        retval = _rootNode;
        _rootNode = null;
      }
      return retval;
    }
  }
}

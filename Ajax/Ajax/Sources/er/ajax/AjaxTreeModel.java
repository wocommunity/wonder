package er.ajax;

import java.util.Enumeration;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableSet;

public class AjaxTreeModel {
  private Object _rootTreeNode;
  private NSMutableSet _expandedTreeNodes;
  private String _parentTreeNodeKeyPath;
  private String _childrenTreeNodesKeyPath;

  public AjaxTreeModel() {
    _expandedTreeNodes = new NSMutableSet();
    _parentTreeNodeKeyPath = "parent";
    _childrenTreeNodesKeyPath = "children";
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

  public void setRootTreeNode(Object rootTreeNode) {
    if (rootTreeNode != _rootTreeNode) {
      _rootTreeNode = rootTreeNode;
      _expandedTreeNodes.removeAllObjects();
    }
  }

  public Object rootTreeNode() {
    return _rootTreeNode;
  }

  public boolean isExpanded(Object treeNode) {
    return _expandedTreeNodes.containsObject(treeNode);
  }

  public void setExpanded(Object treeNode, boolean expanded) {
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


  public NSMutableSet expandedTreeNodes() {
    return _expandedTreeNodes;
  }

  public void setExpandedTreeNodes(NSMutableSet expandedTreeNodes) {
    _expandedTreeNodes = expandedTreeNodes;
  }

  public int level(Object treeNode) {
    Object parentTreeNode = treeNode;
    int level;
    for (level = 0; parentTreeNode != null; parentTreeNode = parentTreeNode(parentTreeNode), level ++) {
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

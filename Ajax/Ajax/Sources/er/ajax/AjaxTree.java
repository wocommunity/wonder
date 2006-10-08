package er.ajax;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableSet;

public class AjaxTree extends WOComponent {
  private String _id;
  private AjaxTreeModel _treeModel;
  private String _collapsedImage;
  private String _collapsedImageFramework;
  private String _expandedImage;
  private String _expandedImageFramework;
  private String _leafImage;
  private String _leafImageFramework;

  private NSArray _nodes;
  private int _nodeIndex;
  private int _level;
  private int _closeCount;
  private Object _lastParent;
  private Object _item;

  public AjaxTree(WOContext context) {
    super(context);
    _collapsedImage = "collapsed.gif";
    _collapsedImageFramework = "Ajax";
    _expandedImage = "expanded.gif";
    _expandedImageFramework = "Ajax";
    _leafImage = "leaf.gif";
    _leafImageFramework = "Ajax";
  }

  public NSArray nodes() {
    if (_nodes == null) {
      NSMutableArray nodes = new NSMutableArray();
      _fillInOpenNodes(treeModel().rootTreeNode(), nodes);
      _nodes = nodes;
    }
    return _nodes;
  }

  public void _setNodeIndex(int nodeIndex) {
    _nodeIndex = nodeIndex;
  }

  public int _nodeIndex() {
    return _nodeIndex;
  }

  protected void _fillInOpenNodes(Object node, NSMutableArray nodes) {
    nodes.add(node);
    if (treeModel().isExpanded(node)) {
      NSArray childrenTreeNodes = treeModel().childrenTreeNodes(node);
      int childTreeNodeCount = childrenTreeNodes.count();
      for (int childTreeNodeNum = 0; childTreeNodeNum < childTreeNodeCount; childTreeNodeNum++) {
        Object childNode = childrenTreeNodes.objectAtIndex(childTreeNodeNum);
        _fillInOpenNodes(childNode, nodes);
      }
    }
  }

  public void appendToResponse(WOResponse aResponse, WOContext aContext) {
    super.appendToResponse(aResponse, aContext);
  }

  public void setItem(Object item) {
    if (item != _item) {
      Object parent = treeModel().parentTreeNode(item);
      int level;
      if (parent == null) {
        level = 0;
      }
      else if (parent == _item) {
        level = _level + 1;
      }
      else if (parent != _lastParent) {
        level = treeModel().level(item);
      }
      else {
        level = _level;
      }
      if (_level > level && parent != null) {
        _closeCount = (_level - level);
      }
      else {
        _closeCount = 0;
      }
      _lastParent = parent;
      _level = level;
      _item = item;
      setValueForBinding(item, "item");
    }
  }

  public Object item() {
    return _item;
  }

  public boolean isLeaf() {
    boolean leaf;
    NSArray nodes = nodes();
    if (_nodeIndex >= nodes.count() - 1) {
      leaf = true;
    }
    else {
      Object nextNode = nodes.objectAtIndex(_nodeIndex + 1);
      Object nextParent = treeModel().parentTreeNode(nextNode);
      leaf = (nextParent == _item);
    }
    if (leaf) {
      leaf = treeModel().childrenTreeNodes(_item).count() == 0;
    }
    return leaf;
  }

  public int _closeCount() {
    return _closeCount;
  }

  public void setTreeModel(AjaxTreeModel treeModel) {
    _treeModel = treeModel;
  }

  public AjaxTreeModel treeModel() {
    if (_treeModel == null) {
      _treeModel = new AjaxTreeModel();
    }
    return _treeModel;
  }

  public void setParentKeyPath(String keyPath) {
    treeModel().setParentTreeNodeKeyPath(keyPath);
  }

  public String parentKeyPath() {
    return treeModel().parentTreeNodeKeyPath();
  }

  public void setChildrenKeyPath(String keyPath) {
    treeModel().setChildrenTreeNodesKeyPath(keyPath);
  }

  public String childrenKeyPath() {
    return treeModel().childrenTreeNodesKeyPath();
  }

  public void setRoot(Object root) {
    treeModel().setRootTreeNode(root);
  }

  public Object root() {
    return treeModel().rootTreeNode();
  }

  public boolean isExpanded() {
    return treeModel().isExpanded(_item);
  }

  public void setId(String id) {
    _id = id;
  }

  public String id() {
    if (_id == null) {
      _id = AjaxUtils.toSafeElementID(context().elementID());
    }
    return _id;
  }

  public void setExpanded(Object treeNode, boolean expanded) {
    treeModel().setExpanded(treeNode, expanded);
  }

  public void setExpandedTreeNodes(NSMutableSet expandedTreeNodes) {
    treeModel().setExpandedTreeNodes(expandedTreeNodes);
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
  
  public String _toggleFunctionName() {
    return _id + "Toggle";
  }

  public WOActionResults expand() {
    treeModel().setExpanded(_item, true);
    _nodes = null;
    return null;
  }

  public WOActionResults collapse() {
    treeModel().setExpanded(_item, false);
    _nodes = null;
    return null;
  }
}
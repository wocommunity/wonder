package er.ajax;

import java.util.Enumeration;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation._NSDelegate;

public class AjaxTreeModel {
	private Object _rootTreeNode;
	private NSMutableSet _expandedTreeNodes;
	private NSMutableSet _collapsedTreeNodes;
	private String _parentTreeNodeKeyPath;
	private String _childrenTreeNodesKeyPath;
	private String _isLeafKeyPath;
	private boolean _allExpanded;
	private boolean _rootExpanded;
	private _NSDelegate _delegate;

	public AjaxTreeModel() {
		_expandedTreeNodes = new NSMutableSet();
		_collapsedTreeNodes = new NSMutableSet();
		_delegate = new _NSDelegate(AjaxTreeModel.Delegate.class);
	}

	public void setDelegate(Object delegate) {
		_delegate.setDelegate(delegate);
	}

	public Object delegate() {
		return _delegate.delegate();
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

	public boolean isLeaf(Object node) {
		boolean isLeaf;
		if (_isLeafKeyPath == null) {
			NSArray childrenTreeNodes = childrenTreeNodes(node);
			isLeaf = childrenTreeNodes == null || childrenTreeNodes.count() == 0;
		}
		else if (_delegate.respondsTo("isLeaf")) {
			isLeaf = _delegate.booleanPerform("isLeaf", node);
		}
		else {
			Boolean isLeafBoolean = (Boolean) NSKeyValueCodingAdditions.Utility.valueForKeyPath(node, _isLeafKeyPath);
			isLeaf = isLeafBoolean.booleanValue();
		}
		return isLeaf;
	}

	public Object parentTreeNode(Object node) {
		Object parentTreeNode = null;
		if (_delegate.respondsTo("parentTreeNode")) {
			parentTreeNode = _delegate.perform("parentTreeNode", node);
		}
		else if (node != null) {
			parentTreeNode = NSKeyValueCodingAdditions.Utility.valueForKeyPath(node, _parentTreeNodeKeyPath);
		}
		else {
			parentTreeNode = null;
		}
		return parentTreeNode;
	}

	public NSArray childrenTreeNodes(Object node) {
		NSArray childrenTreeNodes;
		if (_delegate.respondsTo("childrenTreeNodes")) {
			childrenTreeNodes = (NSArray) _delegate.perform("childrenTreeNodes", node);
		}
		else {
			childrenTreeNodes = (NSArray) NSKeyValueCodingAdditions.Utility.valueForKeyPath(node, _childrenTreeNodesKeyPath);
		}
		return childrenTreeNodes;
	}

	public Enumeration depthFirstEnumeration(Object node, boolean enumeratedClosedNodes) {
		return new DepthFirstEnumeration(node, enumeratedClosedNodes);
	}

	public Enumeration rootDepthFirstEnumeration(boolean enumeratedClosedNodes) {
		return new DepthFirstEnumeration(_rootTreeNode, enumeratedClosedNodes);
	}

	public static interface Delegate {
		public boolean isLeaf(Object node);

		public Object parentTreeNode(Object node);

		public NSArray childrenTreeNodes(Object node);
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

	/**
	 * WrapperNode is useful if your objects form a 
	 * graph instead of a tree and you want to maintain the unique
	 * branching to a particular node as the user navigates through
	 * the tree.  isLeaf has a default implementation that you may
	 * want to overried if you can provide a "smarter" 
	 * implementation.
	 * 
	 * @author mschrag
	 */
	public abstract static class WrapperNode {
		private WrapperNode _parent;
		private Object _userObject;

		public WrapperNode(WrapperNode parent, Object userObject) {
			_parent = parent;
			_userObject = userObject;
		}
		
		public Object userObject() {
			return _userObject;
		}

		protected abstract WrapperNode _createChildNode(Object userObject);
		
		protected abstract NSArray _childrenTreeNodes();
		
		public NSArray childrenTreeNodes() {
			NSArray childrenTreeNodes = _childrenTreeNodes();
			if (childrenTreeNodes != null && childrenTreeNodes.count() > 0) {
				NSMutableArray wrappedTreeNodes = new NSMutableArray();
				Enumeration childrenTreeNodesEnum = childrenTreeNodes.objectEnumerator();
				while (childrenTreeNodesEnum.hasMoreElements()) {
					Object obj = childrenTreeNodesEnum.nextElement();
					wrappedTreeNodes.addObject(_createChildNode(obj));
				}
				childrenTreeNodes = wrappedTreeNodes;
			}
			return childrenTreeNodes;
		}

		public boolean isLeaf() {
			NSArray childrenTreeNodes = _childrenTreeNodes();
			boolean isLeaf = childrenTreeNodes == null || childrenTreeNodes.count() == 0;
			return isLeaf;
		}

		public WrapperNode parentTreeNode() {
			return _parent;
		}

		public int hashCode() {
			int hashCode;
			if (_userObject == null) {
				hashCode = super.hashCode();
			}
			else {
				hashCode = _userObject.hashCode();
			}
			if (_parent != null) {
				hashCode *= _parent.hashCode();
			}
			return hashCode;
		}

		public boolean equals(Object obj) {
			boolean equals;
			if (obj instanceof WrapperNode) {
				WrapperNode wrapperNode = (WrapperNode)obj;
				if (_userObject == null) {
					equals = (wrapperNode._userObject == null);
				}
				else {
					equals = _userObject.equals(wrapperNode._userObject);
				}
				if (equals) {
					if (_parent == null) {
						equals = (wrapperNode._parent == null);
					}
					else {
						equals = _parent.equals(wrapperNode._parent);
					}
				}
			}
			else {
				equals = false;
			}
			return equals;
		}
	}
}

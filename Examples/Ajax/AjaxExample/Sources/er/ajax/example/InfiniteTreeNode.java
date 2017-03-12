package er.ajax.example;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.ajax.AjaxTreeModel;

public class InfiniteTreeNode {
	private Object _parentTreeNode;
	private NSMutableArray<InfiniteTreeNode> _children;
	private String _name;
	private int _depth;

	public InfiniteTreeNode(Object parentTreeNode, String name, int depth) {
		_parentTreeNode = parentTreeNode;
		_name = name;
		_depth = depth;
	}

	public synchronized NSArray childrenTreeNodes() {
		if (_children == null && _depth < 2) {
			_children = new NSMutableArray<>();
			for (int i = 0; i < 5; i++) {
				_children.addObject(new InfiniteTreeNode(this, _name + " Child " + i, _depth + 1));
			}
		}
		return _children;
	}

	public Object parentTreeNode() {
		return _parentTreeNode;
	}

	@Override
	public String toString() {
		return _name;
	}

	public static class Delegate implements AjaxTreeModel.Delegate {
		public NSArray childrenTreeNodes(Object node) {
			InfiniteTreeNode treeNode = (InfiniteTreeNode) node;
			return treeNode.childrenTreeNodes();
		}

		public boolean isLeaf(Object node) {
			InfiniteTreeNode treeNode = (InfiniteTreeNode) node;
			return treeNode.childrenTreeNodes() == null;
		}

		public Object parentTreeNode(Object node) {
			InfiniteTreeNode treeNode = (InfiniteTreeNode) node;
			if (treeNode == null) {
				return null;
			}
			return treeNode.parentTreeNode();
		}
	}
}

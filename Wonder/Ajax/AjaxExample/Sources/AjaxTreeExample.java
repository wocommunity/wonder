import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.ajax.AjaxTreeModel;

public class AjaxTreeExample extends WOComponent {
	public Object _rootTreeNode;
	public Object _rootTreeNode2;
	private Object _treeNode;
	private Object _delegate;

	public AjaxTreeExample(WOContext context) {
		super(context);
		_rootTreeNode = new FakeTreeNode(null, "Root", 0);
		_rootTreeNode2 = new FakeTreeNode(null, "Root2", 0);
	}

	public void setTreeNode(Object treeNode) {
		_treeNode = treeNode;
	}

	public Object getTreeNode() {
		return _treeNode;
	}

	public WOActionResults nodeSelected() {
		System.out.println("AjaxTreeExample.nodeSelected: selected " + _treeNode);
		return null;
	}

	public Object delegate() {
		if (_delegate == null) {
			_delegate = new FakeTreeDelegate();
		}
		return _delegate;

	}

	public static class FakeTreeDelegate implements AjaxTreeModel.Delegate {
		public NSArray childrenTreeNodes(Object node) {
			FakeTreeNode treeNode = (FakeTreeNode) node;
			return treeNode.childrenTreeNodes();
		}

		public boolean isLeaf(Object node) {
			FakeTreeNode treeNode = (FakeTreeNode) node;
			return treeNode.childrenTreeNodes() == null;
		}

		public Object parentTreeNode(Object node) {
			FakeTreeNode treeNode = (FakeTreeNode) node;
			if (treeNode == null) {
				return null;
			}
			return treeNode.parentTreeNode();
		}
	}

	public static class FakeTreeNode {
		private Object _parentTreeNode;
		private NSMutableArray _children;
		private String _name;
		private int _depth;

		public FakeTreeNode(Object parentTreeNode, String name, int depth) {
			_parentTreeNode = parentTreeNode;
			_name = name;
			_depth = depth;
		}

		public synchronized NSArray childrenTreeNodes() {
			if (_children == null && _depth < 2) {
				_children = new NSMutableArray();
				for (int i = 0; i < 5; i++) {
					_children.addObject(new FakeTreeNode(this, _name + " Child " + i, _depth + 1));
				}
			}
			return _children;
		}

		public Object parentTreeNode() {
			return _parentTreeNode;
		}

		public String toString() {
			return _name;
		}
	}
}
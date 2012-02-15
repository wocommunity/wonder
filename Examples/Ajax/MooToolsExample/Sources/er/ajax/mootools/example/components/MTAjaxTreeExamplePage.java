package er.ajax.mootools.example.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;

public class MTAjaxTreeExamplePage extends Main {

	public Object _rootTreeNode;
	public Object _rootTreeNode2;
	private Object _treeNode;
	private Object _delegate;
	public String selectedNode;
	
	public MTAjaxTreeExamplePage(WOContext context) {
        super(context);
		_rootTreeNode = new InfiniteTreeNode(null, "Root", 0);
		_rootTreeNode2 = new InfiniteTreeNode(null, "Root2", 0);
	}

	public void setTreeNode(Object treeNode) {
		_treeNode = treeNode;
	}

	public Object getTreeNode() {
		return _treeNode;
	}

	public WOActionResults nodeSelected() {
		selectedNode = _treeNode.toString();
		System.out.println("AjaxTreeExample.nodeSelected: selected " + _treeNode);
		return null;
	}

	public Object delegate() {
		if (_delegate == null) {
			_delegate = new InfiniteTreeNode.Delegate();
		}
		return _delegate;

	}
}
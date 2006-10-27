package er.ajax;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

public class AjaxTree extends WOComponent {
	private AjaxTreeModel _treeModel;

	private NSArray _nodes;
	private int _nodeIndex;
	private int _level;
	private int _closeCount;
	private Object _lastParent;
	private Object _item;

	public AjaxTree(WOContext context) {
		super(context);
	}

	public boolean synchronizesVariablesWithBindings() {
		return false;
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
			if (childrenTreeNodes != null) {
				int childTreeNodeCount = childrenTreeNodes.count();
				for (int childTreeNodeNum = 0; childTreeNodeNum < childTreeNodeCount; childTreeNodeNum++) {
					Object childNode = childrenTreeNodes.objectAtIndex(childTreeNodeNum);
					_fillInOpenNodes(childNode, nodes);
				}
			}
		}
	}

	public void appendToResponse(WOResponse aResponse, WOContext aContext) {
		_level = 0;
		_closeCount = 0;

		treeModel().setDelegate(valueForBinding("delegate"));
		if (hasBinding("allExpanded")) {
			treeModel().setAllExpanded(((Boolean) valueForBinding("allExpanded")).booleanValue());
		}
		if (hasBinding("rootExpanded")) {
			treeModel().setRootExpanded(((Boolean) valueForBinding("rootExpanded")).booleanValue());
		}
		treeModel().setIsLeafKeyPath(stringValueForBinding("isLeafKeyPath", null));
		treeModel().setParentTreeNodeKeyPath(stringValueForBinding("parentKeyPath", null));
		treeModel().setChildrenTreeNodesKeyPath(stringValueForBinding("childrenKeyPath", null));
		treeModel().setRootTreeNode(valueForBinding("root"));

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
			if (_level > level) {
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
		return treeModel().isLeaf(_item);
	}

	public boolean isExpanded() {
		return treeModel().isExpanded(_item);
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

	public String id() {
		String id;
		if (hasBinding("id")) {
			id = (String) valueForBinding("id");
		}
		else {
			id = AjaxUtils.toSafeElementID(context().elementID());
		}
		return id;
	}

	protected String stringValueForBinding(String bindingName, String defaultValue) {
		String value = defaultValue;
		if (hasBinding(bindingName)) {
			value = (String) valueForBinding(bindingName);
		}
		return value;
	}

	public String collapsedImage() {
		return stringValueForBinding("collapsedImage", "collapsed.gif");
	}

	public String collapsedImageFramework() {
		return stringValueForBinding("collapsedImageFramework", "Ajax");
	}

	public String expandedImage() {
		return stringValueForBinding("expandedImage", "expanded.gif");
	}

	public String expandedImageFramework() {
		return stringValueForBinding("expandedImageFramework", "Ajax");
	}

	public String leafImage() {
		return stringValueForBinding("leafImage", "leaf.gif");
	}

	public String leafImageFramework() {
		return stringValueForBinding("leafImageFramework", "Ajax");
	}

	public String _toggleFunctionName() {
		return id() + "Toggle";
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
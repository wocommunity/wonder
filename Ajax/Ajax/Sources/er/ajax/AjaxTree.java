package er.ajax;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

/**
 * AjaxTree provides an Ajax-refreshing tree view. AjaxTree acts like a WOComponentContent where the content you provide
 * will be the renderer for the tree nodes. The "item" binding provides you access to the current tree node as it
 * iterates over the tree.
 * 
 * If your node objects are homogenous in type, you can define parentKeyPath and childrenKeyPath. If your node objects
 * are heterogenous, you can instead define a delegate, as defined in the AjaxTreeModel.Delegate interface.
 * 
 * @binding root the root node of the tree
 * @binding item the current tree node (equivalent to "item" on WORepetition)
 * @binding itemClass the class of the current item
 * @binding rootExpanded if true, the tree will require the root node to be open; ignored if showRoot = false
 * @binding allExpanded if true, the tree defaults to have all its nodes expanded
 * @binding parentKeyPath the keypath to call on a node to get its parent node (ignored if delegate is set)
 * @binding childrenKeyPath the keypath to call on a node to get its children NSArray (ignored if delegate is set)
 * @binding isLeafKeyPath the keypath to call on a node to determine if it is a leaf node (ignored if delegate, and
 *          defaults to return childrenKeyPath.count() == 0 if not set)
 * @binding id the html id of the tree
 * @binding class the html class of the tree
 * @binding treeModel the treeModel to use (one will be created by default)
 * @binding collapsedImage the icon to use for a collapsed node
 * @binding collapsedImageFramework the framework to load the collapsed icon from
 * @binding expandedImage the icon to use for an expanded node
 * @binding expandedImageFramework the framework to load the expanded icon from
 * @binding leafImage the icon to use for a leaf node
 * @binding leafImageFramework the framework to load the leaf icon from
 * @binding delegate the delegate to use instead of keypaths (see AjaxTreeModel.Delegate)
 * @binding showRoot if false, the root node will be skipped and the tree will begin with its children
 * @binding cache whether to cache the nodes or determine them every time from the model (default: true)
 * 
 * @author mschrag
 */
public class AjaxTree extends WOComponent {
	private AjaxTreeModel _treeModel;

	private NSArray _nodes;
	private int _level;
	private int _closeCount;
	private Object _lastParent;
	private Object _item;
	private String _id = null;
	private Object _lastRootNode;

	public AjaxTree(WOContext context) {
		super(context);
	}

	public boolean synchronizesVariablesWithBindings() {
		return false;
	}

	public NSArray nodes() {
		Object rootNode = treeModel().rootTreeNode();
		if (_nodes == null || rootNode == null || !rootNode.equals(_lastParent) || !AjaxUtils.booleanValueForBinding("cache", true, _keyAssociations, parent())) {
			NSMutableArray nodes = new NSMutableArray();
			boolean showRoot = AjaxUtils.booleanValueForBinding("showRoot", true, _keyAssociations, parent());
			_fillInOpenNodes(treeModel().rootTreeNode(), nodes, showRoot);
			_nodes = nodes;
			_lastRootNode = rootNode;
		}
		return _nodes;
	}

	protected void _fillInOpenNodes(Object node, NSMutableArray nodes, boolean showNode) {
		if (showNode) {
			nodes.addObject(node);
		}
		if (treeModel().isExpanded(node)) {
			NSArray childrenTreeNodes = treeModel().childrenTreeNodes(node);
			if (childrenTreeNodes != null) {
				int childTreeNodeCount = childrenTreeNodes.count();
				for (int childTreeNodeNum = 0; childTreeNodeNum < childTreeNodeCount; childTreeNodeNum++) {
					Object childNode = childrenTreeNodes.objectAtIndex(childTreeNodeNum);
					_fillInOpenNodes(childNode, nodes, true);
				}
			}
		}
	}

	public void reset() {
		super.reset();
	}

	protected void resetTree() {
		_level = 0;
		_closeCount = 0;
		_lastParent = null;
		_item = null;

		treeModel().setDelegate(valueForBinding("delegate"));
		if (hasBinding("allExpanded")) {
			treeModel().setAllExpanded(AjaxUtils.booleanValueForBinding("allExpanded", false, _keyAssociations, parent()));
		}
		if (hasBinding("rootExpanded") || hasBinding("showRoot")) {
			treeModel().setRootExpanded(AjaxUtils.booleanValueForBinding("rootExpanded", false, _keyAssociations, parent()) || !AjaxUtils.booleanValueForBinding("showRoot", true, _keyAssociations, parent()));
		}
		treeModel().setIsLeafKeyPath(stringValueForBinding("isLeafKeyPath", null));
		treeModel().setParentTreeNodeKeyPath(stringValueForBinding("parentKeyPath", null));
		treeModel().setChildrenTreeNodesKeyPath(stringValueForBinding("childrenKeyPath", null));
		treeModel().setRootTreeNode(valueForBinding("root"));
		setItem(treeModel().rootTreeNode());
	}

	public void appendToResponse(WOResponse aResponse, WOContext aContext) {
		resetTree();
		super.appendToResponse(aResponse, aContext);
		resetTree();
	}

	public void takeValuesFromRequest(WORequest aRequest, WOContext aContext) {
		resetTree();
		super.takeValuesFromRequest(aRequest, aContext);
		resetTree();
	}

	public WOActionResults invokeAction(WORequest aRequest, WOContext aContext) {
		resetTree();
		WOActionResults results = super.invokeAction(aRequest, aContext);
		resetTree();
		return results;
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
			if (canGetValueForBinding("treeModel") && valueForBinding("treeModel") != null) {
				_treeModel = (AjaxTreeModel) valueForBinding("treeModel");
			}
			else {
				_treeModel = new AjaxTreeModel();
				if (canSetValueForBinding("treeModel")) {
					setValueForBinding(_treeModel, "treeModel");
				}
			}
		}
		return _treeModel;
	}

	public String id() {
		if (_id == null) {
			if (hasBinding("id")) {
				_id = (String) valueForBinding("id");
			}
			else {
				_id = AjaxUtils.toSafeElementID(context().elementID());
			}
		}
		return _id;
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

	public String imageLinkClass() {
		return stringValueForBinding("imageLinkClass", "");
	}

	public String nodeItem() {
		StringBuffer nodeItem = new StringBuffer();
		nodeItem.append("<li");
		if (hasBinding("itemClass")) {
			String itemClass = (String)valueForBinding("itemClass");
			if (itemClass != null) {
				nodeItem.append(" class = \"");
				nodeItem.append(itemClass);
				nodeItem.append("\"");
			}
		}
		nodeItem.append(">");
		return nodeItem.toString();
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
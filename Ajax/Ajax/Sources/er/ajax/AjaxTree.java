package er.ajax;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

public class AjaxTree extends WOComponent {
  private AjaxTreeModel _treeModel;
  private String _treeNodeRenderer;

  public AjaxTree(WOContext context) {
    super(context);
    _treeNodeRenderer = AjaxSimpleTreeNodeRenderer.class.getName();
  }

  public void setTreeModel(AjaxTreeModel treeModel) {
    _treeModel = treeModel;
  }

  public AjaxTreeModel treeModel() {
    return _treeModel;
  }

  public void setRootNode(ITreeNode rootNode) {
    if (_treeModel == null || _treeModel.rootTreeNode() != rootNode) {
      _treeModel = new AjaxTreeModel(rootNode);
    }
  }

  public ITreeNode rootNode() {
    return (_treeModel != null) ? _treeModel.rootTreeNode() : null;
  }

  public void setTreeNodeRenderer(String treeNodeRenderer) {
    _treeNodeRenderer = treeNodeRenderer;
  }

  public String treeNodeRenderer() {
    return _treeNodeRenderer;
  }
}
package er.ajax;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

public class AjaxTreeNode extends WOComponent {
  private AjaxTreeModel _treeModel;
  private ITreeNode _treeNode;
  private String _treeNodeRenderer;
  public ITreeNode _repetitionChildTreeNode;
  private String _elementID;

  public AjaxTreeNode(WOContext context) {
    super(context);
  }
  
  public void appendToResponse(WOResponse aResponse, WOContext aContext) {
    _elementID = AjaxUtils.toSafeElementID(aContext.elementID());
    super.appendToResponse(aResponse, aContext);
  }

  public int level() {
    ITreeNode parentTreeNode = _treeNode;
    int level;
    for (level = 0; parentTreeNode != null; parentTreeNode = parentTreeNode.parentTreeNode()) {
      // do nothing
    }
    return level;
  }
  
  public void setTreeModel(AjaxTreeModel treeModel) {
    _treeModel = treeModel;
  }

  public AjaxTreeModel treeModel() {
    return _treeModel;
  }

  public void setTreeNode(ITreeNode treeNode) {
    _treeNode = treeNode;
  }

  public ITreeNode treeNode() {
    return _treeNode;
  }
  
  public void setTreeNodeRenderer(String treeNodeRenderer) {
    _treeNodeRenderer = treeNodeRenderer;
  }
  
  public String treeNodeRenderer() {
    return _treeNodeRenderer;
  }

  public boolean isExpanded() {
    return _treeModel.isExpanded(_treeNode);
  }
  
  public String updateContainerID() {
    return AjaxUtils.toSafeElementID(_elementID);
  }

  public WOActionResults toggleExpanded() {
    _treeModel.setExpanded(_treeNode, !isExpanded());
    return null;
  }
}
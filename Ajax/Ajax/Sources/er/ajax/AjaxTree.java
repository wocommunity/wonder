package er.ajax;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSMutableSet;

public class AjaxTree extends WOComponent {
  private AjaxTreeModel _treeModel;

  public AjaxTree(WOContext context) {
    super(context);
    _treeModel = new AjaxTreeModel();
  }

  public void setTreeModel(AjaxTreeModel treeModel) {
    _treeModel = treeModel;
  }

  public AjaxTreeModel treeModel() {
    return _treeModel;
  }

  public String collapsedImage() {
    return _treeModel.collapsedImage();
  }

  public String collapsedImageFramework() {
    return _treeModel.collapsedImageFramework();
  }

  public String expandedImage() {
    return _treeModel.expandedImage();
  }

  public String expandedImageFramework() {
    return _treeModel.expandedImageFramework();
  }

  public String leafImage() {
    return _treeModel.leafImage();
  }

  public String leafImageFramework() {
    return _treeModel.leafImageFramework();
  }

  public ITreeNode rootTreeNode() {
    return _treeModel.rootTreeNode();
  }

  public void setCollapsedImage(String collapsedImage) {
    _treeModel.setCollapsedImage(collapsedImage);
  }

  public void setCollapsedImageFramework(String collapsedImageFramework) {
    _treeModel.setCollapsedImageFramework(collapsedImageFramework);
  }

  public void setExpanded(ITreeNode treeNode, boolean expanded) {
    _treeModel.setExpanded(treeNode, expanded);
  }

  public void setExpandedImage(String expandedImage) {
    _treeModel.setExpandedImage(expandedImage);
  }

  public void setExpandedImageFramework(String expandedImageFramework) {
    _treeModel.setExpandedImageFramework(expandedImageFramework);
  }

  public void setExpandedTreeNodes(NSMutableSet expandedTreeNodes) {
    _treeModel.setExpandedTreeNodes(expandedTreeNodes);
  }

  public void setLeafImage(String leafImage) {
    _treeModel.setLeafImage(leafImage);
  }

  public void setLeafImageFramework(String leafImageFramework) {
    _treeModel.setLeafImageFramework(leafImageFramework);
  }

  public void setRootTreeNode(ITreeNode rootTreeNode) {
    _treeModel.setRootTreeNode(rootTreeNode);
  }

  public void setTreeNodeRenderer(String treeNodeRenderer) {
    _treeModel.setTreeNodeRenderer(treeNodeRenderer);
  }

  public String treeNodeRenderer() {
    return _treeModel.treeNodeRenderer();
  }
}
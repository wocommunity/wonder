import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

public class AjaxTreeExample extends WOComponent {
  public Object _rootTreeNode;
  private Object _treeNode;
  
  public AjaxTreeExample(WOContext context) {
    super(context);
    _rootTreeNode = new FakeTreeNode(null, "Root", 0);
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
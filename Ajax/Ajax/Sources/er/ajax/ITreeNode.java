package er.ajax;

import com.webobjects.foundation.NSArray;

public interface ITreeNode {
  public boolean isLeaf();
  public ITreeNode parentTreeNode();
  public NSArray childrenTreeNodes();
}

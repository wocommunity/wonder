import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

public class LinkExampleSubComponent1 extends WOComponent {
  public LinkExampleSubComponent1(WOContext context) {
    super(context);
  }
  
  public WOActionResults testAction() {
    System.out.println("LinkExampleSubComponent1.testAction: test action performed!");
    return null;
  }
}

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

public class UpdaterExample extends WOComponent {

  public UpdaterExample(WOContext context) {
    super(context);
  }

  public long test() {
    return System.currentTimeMillis();
  }

  public WOComponent someAction() {
    System.out.println("UpdaterExample.someAction: action!");
    return pageWithName("SliderExample");
  }

  public WOActionResults anotherAction() {
    System.out.println("UpdaterExample.anotherAction: Fired requested action (return value is ignored right now) (" + context().request().formValues() + ")");
    return null;
  }
  
  public WOActionResults submit1() {
    System.out.println("UpdaterExample.submit1: Submit #1");
    return null;
  }
  
  public WOActionResults submit2() {
    System.out.println("UpdaterExample.submit2: Submit #2");
    return null;
  }
  
  public WOActionResults exampleAreaUpdated() {
	  System.out.println("UpdaterExample.exampleAreaUpdated: Yep");
	  return null;
  }
}

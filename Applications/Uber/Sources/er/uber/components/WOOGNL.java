package er.uber.components;

import com.webobjects.appserver.WOContext;

public class WOOGNL extends UberComponent {
  public String _helloWorld;
  public boolean _fixedOn = true;
  public boolean _fixedOff = false;
  public String _nullString = null;

  public WOOGNL(WOContext context) {
    super(context);
    _helloWorld = "Hello World";
  }
}
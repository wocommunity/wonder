package er.ajax.example2.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

import er.ajax.example2.model.ComplexPerson;
import er.ajax.example2.model.ExampleDataFactory;

public class ClientSideRendering extends AjaxWOWODCPage {
  private JSONProxy _proxy;
  private NSArray<ComplexPerson> _people;

  public ClientSideRendering(WOContext context) {
    super(context);
    _proxy = new JSONProxy();
    _people = ExampleDataFactory.family();
  }

  @Override
  protected boolean useDefaultComponentCSS() {
    return true;
  }

  public JSONProxy proxy() {
    return _proxy;
  }

  public class JSONProxy {
    public NSArray<ComplexPerson> family() {
      return _people;
    }

    public void voteForPerson(ComplexPerson person) {
      _people.get(_people.indexOf(person)).vote();
    }
  }
}
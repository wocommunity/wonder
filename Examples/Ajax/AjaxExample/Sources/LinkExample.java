import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSMutableArray;

import er.ajax.AjaxUtils;
import er.ajax.example.ExampleDataFactory;
import er.ajax.example.Word;

public class LinkExample extends WOComponent {
  private boolean _showingSubComponent1;
  public NSMutableArray _words;
  public Word _repetitionWord;
  public Word _selectedWord; 

  public LinkExample(WOContext context) {
    super(context);
    _showingSubComponent1 = true;
    _words = ExampleDataFactory.randomWords(10);
  }

  public WOActionResults wordSelected() {
    _selectedWord = _repetitionWord;
    System.out.println("LinkExample.wordSelected: " + _repetitionWord);
    return null;
  }
  
  public WOActionResults exampleAction() {
    System.out.println("LinkExample.exampleAction: Action Performed!");
    return null;
  }
  
  public String now() {
	  return String.valueOf(System.currentTimeMillis());
  }

  public WOActionResults replaceLinkAction() {
    WOActionResults results;
    if (_showingSubComponent1) {
      results = pageWithName(LinkExampleSubComponent2.class.getName());
    }
    else {
      results = pageWithName(LinkExampleSubComponent1.class.getName());
    }
    _showingSubComponent1 = !_showingSubComponent1;
    return results;
  }
  
  public WOActionResults onClickServerAction() {
	  System.out.println("LinkExample.onClickServerAction: Clicked");
	  return null;
  }
  
  public WOActionResults javascriptResponseAction() {
	  return AjaxUtils.javascriptResponse("alert('hi');", context());
  }
}

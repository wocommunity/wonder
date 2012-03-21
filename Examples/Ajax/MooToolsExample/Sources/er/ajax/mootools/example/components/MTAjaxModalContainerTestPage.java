package er.ajax.mootools.example.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

import er.extensions.foundation.ERXStringUtilities;

public class MTAjaxModalContainerTestPage extends Main {
	
	public NSArray<Word> _words;
	public Word _repetitionWord;	

    public MTAjaxModalContainerTestPage(WOContext context) {
        super(context);
		_words = ExampleDataFactory.randomWords(10);
	}
	
	public String wordID() {
		return ERXStringUtilities.safeIdentifierName(String.valueOf(_words.indexOf(_repetitionWord)));
	}

	public String onSuccessFunction() {
		return wordID() + ".hide().destroy();";
	}	
	
}
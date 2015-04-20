package er.jquerymobile.exampleapp.components;

import webobjectsexamples.businesslogic.eo.Movie;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.appserver.ERXResponse;
import er.extensions.foundation.ERXPropertyListSerialization;
import er.jquerymobile.exampleapp.businessLogic.SampleComponentBase;

public class FormAutoComplete extends SampleComponentBase {

	private static final long serialVersionUID = 1L;

	// ********************************************************************
	// Constructor : コンストラクタ
	// ********************************************************************

	public FormAutoComplete(WOContext context) {
		super(context);
	}

	// ********************************************************************
	// Methods : メソッド
	// ********************************************************************

	public String testString = null;
	public String testStringComplex = null;
	public String testStringComplexHidden = null;
	public String testStringLocal = null;
	public String testStringLocalAny = null;

	private static boolean _findString(String text, String searchFor) {
		boolean foundAll = false;
		if (text != null && searchFor != null) {
			foundAll = text.toLowerCase().contains(searchFor.toLowerCase());
		}
		return foundAll;
	}

	public NSArray<String> programmingLanguages() {
		return new NSArray<String>("C", "Clojure", "Java", "Scala", "Objective-C", "C++", "PHP", "C#", "Basic", "Swift", "(Visual) Basic", "Python",
				"Perl", "JavaScript", "Ruby", "Visual Basic .NET", "SQL", "Transact-SQL", "Lisp", "Pascal", "Bash", "PL/SQL", "Delphi/Object Pascal",
				"Ada", "MATLAB");
	}

	// ********************************************************************
	// Actions : アクション
	// ********************************************************************
	public WOActionResults doSubmitAction() {
		System.err.println("**doSubmitAction**");
		System.err.println(" testString = " + testString);
		System.err.println(" testStringComplex = " + testStringComplex);
		System.err.println(" testStringComplexHidden = " + testStringComplexHidden);
		System.err.println(" testStringLocal = " + testStringLocal);
		System.err.println(" testStringLocalAny = " + testStringLocalAny);

		return null;
	}

	private int maxResults = 10;

	public WOActionResults suggest() {
		String filter = (String) context().request().formValueForKey("term");

		NSMutableArray<String> array = new NSMutableArray<String>();
		if (filter != null && filter.length() > 0) {
			NSArray<Movie> movies = movies();

			for (int i = 0; i < movies.count(); i++) {
				Movie movie = movies.objectAtIndex(i);
				if (_findString(movie.title(), filter)) {
					array.add(movie.title());
				}
				if (array.count() > maxResults) {
					break;
				}
			}
		}

		String moviesTitlesAsJson = ERXPropertyListSerialization.jsonStringFromPropertyList(array);
		ERXResponse result = new ERXResponse(moviesTitlesAsJson);
		result.setHeader("text/plain", "content-type");
		return result;
	}

	public WOActionResults suggestComplex() {
		String filter = (String) context().request().formValueForKey("term");

		NSMutableArray<NSDictionary<String, String>> array = new NSMutableArray<NSDictionary<String, String>>();
		if (filter != null && filter.length() > 0) {
			NSArray<Movie> movies = movies();

			for (int i = 0; i < movies.count(); i++) {
				Movie movie = movies.objectAtIndex(i);
				if (_findString(movie.title(), filter)) {
					NSDictionary<String, String> tmp = new NSDictionary<String, String>(new NSArray<String>(movie.primaryKey(), movie.title()), new NSArray<String>("value",
							"label"));
					array.add(tmp);
				}
				if (array.count() > maxResults) {
					break;
				}
			}
		}

		String moviesTitlesAsJson = ERXPropertyListSerialization.jsonStringFromPropertyList(array);
		ERXResponse result = new ERXResponse(moviesTitlesAsJson);
		result.setHeader("text/plain", "content-type");
		return result;
	}

}
package er.examples.textsearchdemo.components.sections;

import java.io.File;
import java.net.URL;

import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.spell.Dictionary;
import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import webobjectsexamples.businesslogic.movies.common.Movie;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSLog;

import er.examples.textsearchdemo.components.shared.TSCommonComponent;
import er.extensions.foundation.ERXProperties;
import er.indexing.ERDocument;
import er.indexing.ERIndex;

public class SpellCheck extends TSCommonComponent {
	
	private ERIndex _moviesIndex;
	
	public String searchString;
	private SpellChecker _spellChecker;
	public ScoreDoc[] foundScoreDocs;
	public int foundSetIndex;
	public NSArray<String> suggestions;
	public boolean isIncorrect = false;
	public String suggestionItem;
	
    public SpellCheck(WOContext context) {
        super(context);
    }
    
	// ACTIONS
	
	public WOActionResults createSpellingIndexAction() {
		
		URL wordFilePath = application().resourceManager().pathURLForResourceNamed("englishwordlist.txt", "app", null);

        try {
        	
        		File wordDir = new File(wordFilePath.getPath());
            Dictionary dictionary = new PlainTextDictionary(wordDir);
            
            spellChecker().indexDictionary(dictionary);
            
        } catch (Exception e) {
        	e.printStackTrace();
        } 
		return null;
	}

	public WOActionResults searchAction() {
		suggestions = null;
		isIncorrect = false;
		if (searchString != null && searchString.length() > 0) {
			try {
				int maxSuggetions = 5;
				if (spellChecker().exist(searchString)) {
					isIncorrect = false;
				} else {
					isIncorrect = true;
				}
				suggestions = new NSArray<String>(spellChecker().suggestSimilar(searchString, maxSuggetions));
				NSLog.out.appendln( "SpellCheck.searchAction: " + isIncorrect + " suggestions: " + suggestions);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	// ACCESSORS
	
	public ERDocument currentDocument() {
		ERDocument doc = null;
		int docId = foundScoreDocs[foundSetIndex].doc;
		float score = foundScoreDocs[foundSetIndex].score;
		doc = moviesIndex().documentForId(docId, score);
		NSLog.out.appendln( "SimpleSearch.currentDocument: " + doc + " score: " + score);
		return doc;
	}
	
	public int foundScoreDocsCount() {
		int result = 0;
		if (foundScoreDocs != null) {
			result = foundScoreDocs.length;
		}
		return result;
	}
	
	public SpellChecker spellChecker() {
		if (_spellChecker == null) {
			String spellIndexDirectory = ERXProperties.stringForKey("er.examples.textsearchdemo.spellCheckIndexPath");
			File spellingDir = new File(spellIndexDirectory);
			try {
				Directory spellingDirectory = FSDirectory.open(spellingDir);
				_spellChecker = new SpellChecker(spellingDirectory);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return _spellChecker;
	}

	public ERIndex moviesIndex() {
		if (_moviesIndex == null) {
			_moviesIndex = ERIndex.indexNamed(Movie.ENTITY_NAME);
		}
		return _moviesIndex;
	}
	
	public void setMoviesIndex(ERIndex index) {
		_moviesIndex = index;
	}


}
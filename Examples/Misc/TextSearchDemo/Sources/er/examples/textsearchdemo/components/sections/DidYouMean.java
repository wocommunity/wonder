package er.examples.textsearchdemo.components.sections;

import java.io.File;
import java.net.URL;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.spell.Dictionary;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import webobjectsexamples.businesslogic.movies.common.Movie;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSPropertyListSerialization;

import er.examples.textsearchdemo.components.shared.TSCommonComponent;
import er.extensions.foundation.ERXProperties;
import er.indexing.ERDocument;
import er.indexing.ERIndex;

public class DidYouMean extends TSCommonComponent {
    
	private ERIndex _moviesIndex;
	private SpellChecker _spellChecker;
	public String searchString;
	public ScoreDoc[] foundScoreDocs;
	public int foundSetIndex;
	public String suggestion;
	
	public DidYouMean(WOContext context) {
        super(context);
    }
    
	// ACTIONS
	@SuppressWarnings("all")
	public WOActionResults createDidYouMeanIndexAction() {
		
		URL moviesModelPath = application().resourceManager().pathURLForResourceNamed("Movie.indexModel", "app", null);
		NSDictionary<String, String> modelDict = NSPropertyListSerialization.dictionaryWithPathURL(moviesModelPath);
		

        try {
        	
        	URL originalIndexDirectory = new URL((String)modelDict.valueForKey("store"));
        	File existingDir = new File(originalIndexDirectory.getFile());
            Directory existingDirectory = FSDirectory.open(existingDir);
            IndexReader indexReader = IndexReader.open(existingDirectory, false);
            
            Dictionary dictionary = new LuceneDictionary(indexReader, "content");
            
            spellChecker().indexDictionary(dictionary);
            
        } catch (Exception e) {
        	e.printStackTrace();
        } 
		return null;
	}

	public WOActionResults searchAction() {
		suggestion = null;
		if (searchString != null && searchString.length() > 0) {
			try {
				QueryParser parser = new QueryParser(Version.LUCENE_29, "content", new StandardAnalyzer(Version.LUCENE_29));
				Query q = parser.parse(searchString);
				foundScoreDocs = moviesIndex().findScoreDocs(q, 10);
				NSLog.out.appendln( "Found Hits: " + foundScoreDocs.length );
				if (foundScoreDocs.length == 0) {
					String[] suggestions = spellChecker().suggestSimilar(searchString, 2);
					suggestion = suggestions[0];
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public WOActionResults useSuggestionAction() {
		searchString = suggestion;
		return searchAction();
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
			String spellIndexDirectory = ERXProperties.stringForKey("er.examples.textsearchdemo.didYouMeanIndexPath");
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
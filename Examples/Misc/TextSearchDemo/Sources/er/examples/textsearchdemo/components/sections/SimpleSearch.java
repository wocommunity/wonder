package er.examples.textsearchdemo.components.sections;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.util.Version;

import webobjectsexamples.businesslogic.movies.common.Movie;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSLog;

import er.examples.textsearchdemo.components.shared.TSCommonComponent;
import er.extensions.eof.ERXEC;
import er.indexing.ERDocument;
import er.indexing.ERIndex;

public class SimpleSearch extends TSCommonComponent {
	
	private ERIndex _moviesIndex;
	private QueryParser _contentQueryParser;
	
	public String searchString;
	public ScoreDoc[] foundScoreDocs;
	public int foundSetIndex;
	
    public SimpleSearch(WOContext context) {
        super(context);
    }
    
	// ACTIONS
	
	public WOActionResults indexMoviesAction() {
		EOEditingContext ec = ERXEC.newEditingContext();
		NSArray<Movie> eos = Movie.fetchAllMovies(ec);
		moviesIndex().clear();
		moviesIndex().addObjectsToIndex(ec, eos);
		return null;
	}

	public WOActionResults searchAction() {
		if (searchString != null && searchString.length() > 0) {
			try {
				Query q = contentQueryParser().parse(searchString);
				foundScoreDocs = moviesIndex().findScoreDocs(q, 10);
				NSLog.out.appendln( "Found Hits: " + foundScoreDocs.length );
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public WOActionResults showDetailsAction() {
		EOEditingContext ec = ERXEC.newEditingContext();
		Movie movie = (Movie)ec.faultForGlobalID(currentDocument().eoKeyGlobalId(), ec);
		
		MovieDetails nextPage = pageWithName(MovieDetails.class);
		nextPage.setMovie(movie);

		return nextPage;
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

	public ERIndex moviesIndex() {
		if (_moviesIndex == null) {
			_moviesIndex = ERIndex.indexNamed(Movie.ENTITY_NAME);
		}
		return _moviesIndex;
	}
	
	public void setMoviesIndex(ERIndex index) {
		_moviesIndex = index;
	}

	public QueryParser contentQueryParser() {
		if (_contentQueryParser == null) {
			_contentQueryParser = new QueryParser(Version.LUCENE_29, "content", new StandardAnalyzer(Version.LUCENE_29));
		}
		return _contentQueryParser;
	}
	
	public void setContentQueryParser(QueryParser qp) {
		_contentQueryParser = qp;
	}
	
}
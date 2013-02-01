package er.luceneadaptor;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;

import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EOAdaptorContext;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOSQLExpressionFactory;
import com.webobjects.eoaccess.EOSchemaGeneration;
import com.webobjects.eoaccess.EOSynchronizationFactory;
import com.webobjects.foundation.NSDictionary;

/**
 * ERLuceneAdaptor is an EOAdaptor implementation for the lucene indexing
 * system.
 * 
 * @author ak
 */
public class ERLuceneAdaptor extends EOAdaptor {

	public static final Object QUERY_HINTS = "query";
	public static final Object SORT_HINTS = "sort";
	
	private Object _syncFactory;
	public Directory _directory;
	public Analyzer _analyzer;

	private IndexReader _reader;

	public ERLuceneAdaptor(String name) {
		super(name);
	}

	@Override
	public void setConnectionDictionary(NSDictionary dictionary) {
		if (dictionary == null) {
			super.setConnectionDictionary(NSDictionary.EmptyDictionary);
		} else {
			super.setConnectionDictionary(dictionary);
		}
		assertConnectionDictionaryIsValid();
	}

	public Directory directory() {
		return _directory;
	}

	public Analyzer analyzer() {
		return _analyzer;
	}

	public IndexReader indexReader() throws CorruptIndexException, IOException {
		if (_reader == null) {
			_reader = IndexReader.open(directory(), true);
		}
		if (!_reader.isCurrent()) {
			_reader = _reader.reopen();
		}
		return _reader;
	}

	public IndexWriter createWriter() {
		try {
			if (directory().fileExists(".")) {
				return new IndexWriter(directory(), analyzer(), false, MaxFieldLength.UNLIMITED);
			}
			return new IndexWriter(directory(), analyzer(), true, MaxFieldLength.UNLIMITED);
		} catch (CorruptIndexException e) {
			throw new ERLuceneAdaptorException("Create index failed: " + e.getMessage(), e);
		} catch (LockObtainFailedException e) {
			throw new ERLuceneAdaptorException("Create index failed: " + e.getMessage(), e);
		} catch (IOException e) {
			throw new ERLuceneAdaptorException("Create index failed: " + e.getMessage(), e);
		}
	}

	@Override
	public void assertConnectionDictionaryIsValid() {
		try {
			String url = (String) connectionDictionary().objectForKey("URL");
			if(url == null) {
				throw new ERLuceneAdaptorException("URL can't be empty.");
			}
			File indexDirectory = new File(new URL(url).getFile());
			_directory = FSDirectory.open(indexDirectory);
		} catch (MalformedURLException e) {
			throw new ERLuceneAdaptorException("Open Directory failed: " + e.getMessage(), e);
		} catch (IOException e) {
			throw new ERLuceneAdaptorException("Open Directory failed: " + e.getMessage(), e);
		}
		_analyzer = new StandardAnalyzer(Version.LUCENE_29);
	}

	@Override
	public EOAdaptorContext createAdaptorContext() {
		return new ERLuceneAdaptorContext(this);
	}

	@Override
	public boolean isValidQualifierType(String typeName, EOModel model) {
		return true;
	}

	// Required for Migrations
	@Override
	public Class defaultExpressionClass() {
		return ERLuceneExpression.class;
	}

	@Override
	public EOSQLExpressionFactory expressionFactory() {
		return null; // new ERLuceneExpressionFactory(this);
	}

	@Override
	public EOSchemaGeneration synchronizationFactory() {
		if (_syncFactory == null) {
			_syncFactory = new ERLuceneSynchronizationFactory(this);
		}
		return (EOSchemaGeneration) _syncFactory;
	}

	// MS: This has to return null to prevent a stack overflow in 5.4.
	@Override
	public EOSynchronizationFactory schemaSynchronizationFactory() {
		return null;
	}
}

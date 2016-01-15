package er.lucenceadaptor.example;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.NumericUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WORequest;
import com.webobjects.directtoweb.D2W;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSTimestamp;

import er.directtoweb.ERD2WDirectAction;
import er.extensions.components.ERXLoremIpsumGenerator;
import er.extensions.eof.ERXEC;
import er.extensions.eof.qualifiers.ERXBetweenQualifier;
import er.lucenceadaptor.example.components.Main;
import er.luceneadaptor.ERLuceneAdaptor;
import er.luceneadaptor.ERLuceneAdaptorChannel;

public class DirectAction extends ERD2WDirectAction {

	private final class SimpleCollector extends Collector {
		private Scorer scorer;
		private int docBase;
		private IndexSearcher _searcher;

		SimpleCollector(IndexSearcher searcher) {
			_searcher = searcher;
		}
		
		// simply print docId and score of every matching document
		@Override
		public void collect(int id) throws IOException {
			
			Document document = _searcher.doc(id);
			String fieldValue = document.get("userCount");
			Number userCount = NumericUtils.prefixCodedToLong(fieldValue);
			boolean contains = document.get("content").indexOf(" facilisis") > 0;
			System.out.println("doc=" + id + docBase + " score=" + scorer.score() + "->" + userCount + "-" + contains);
		}

		@Override
		public boolean acceptsDocsOutOfOrder() {
			return true;
		}

		@Override
		public void setNextReader(IndexReader reader, int docBase) throws IOException {
			this.docBase = docBase;
		}

		@Override
		public void setScorer(Scorer scorer) throws IOException {
			this.scorer = scorer;
		}
	}

	private final static Logger log = LoggerFactory.getLogger(DirectAction.class);

	public DirectAction(WORequest request) {
		super(request);
	}

	@Override
	public WOActionResults defaultAction() {
		return pageWithName(Main.class.getName());
	}

	/**
	 * Checks if a page configuration is allowed to render. Provide a more
	 * intelligent access scheme as the default just returns false. And be sure
	 * to read the javadoc to the super class.
	 * 
	 * @param pageConfiguration
	 * @return
	 */
	@Override
	protected boolean allowPageConfiguration(String pageConfiguration) {
		return true;
	}

	public WOActionResults loginAction() {

		String username = request().stringFormValueForKey("username");
		String password = request().stringFormValueForKey("password");

		NSLog.out.appendln("***DirectAction.loginAction - username: " + username + " : password: " + password + "***");

		// ENHANCEME - add appropriate login behaviour here

		return D2W.factory().defaultPage(session());
	}

	private static IndexReader reader;
	
	public WOActionResults directQueryAction() throws IOException {

		if(reader == null) {
			Directory directory = FSDirectory.open(new File("/tmp/test"));
			reader = IndexReader.open(directory);
		}
		IndexSearcher searcher = new IndexSearcher(reader);
		for(int i = 0; i < searcher.maxDoc() && i < 10; i++) {
			Document doc = searcher.doc(i);
			// log.info(doc);
		}
		Query query = null;
		String key = "ipse".toLowerCase();
		if(false) {
			NumericRangeQuery q = NumericRangeQuery.newIntRange("userCount", Integer.valueOf(30), Integer.valueOf(10000), true, true);
			//q = NumericRangeQuery.newLongRange("userCount", Long.valueOf(30), Long.valueOf(10000), true, true);
			query = q;
		} else {
			TermQuery q1 = new TermQuery(new Term("contents", key));
			TermRangeQuery q2 = new TermRangeQuery("userCount", NumericUtils.longToPrefixCoded(30), NumericUtils.longToPrefixCoded(300), false, false);
			BooleanQuery q = new BooleanQuery();
			//q.add(q1, BooleanClause.Occur.MUST);
			q.add(q2, BooleanClause.Occur.MUST);
			query = q;
		}
		log.info("{}", query);
		if (true) {
			TopDocs docs = searcher.search(query, Integer.MAX_VALUE);
			int hits = docs.totalHits;
			for (int i = 0; i < hits && i < 50; i++) {
				int docId = docs.scoreDocs[i].doc;
				Document document = searcher.doc(docId);
				String fieldValue = document.get("userCount");
				String content = document.get("contents");
				Number userCount = NumericUtils.prefixCodedToLong(fieldValue);
				boolean contains = content.indexOf(key) >= 0;
				log.info("{}/{}->{}-{}", i, docId, userCount, document);
			}
		} else {
			Collector streamingHitCollector = new SimpleCollector(searcher);
			searcher.search(query, streamingHitCollector);
		}
		return D2W.factory().defaultPage(session());
	}

	public WOActionResults eofQueryAction() throws IOException {
		EOEditingContext ec = ERXEC.newEditingContext();
		ec.lock();
		try {
			EOQualifier q1 = new EOKeyValueQualifier("contents", EOQualifier.QualifierOperatorLike, "suspend*");
			EOQualifier q2 = new ERXBetweenQualifier("userCount", Long.valueOf(30), Long.valueOf(800));
			// EOQualifier q3 = new EOKeyValueQualifier("userCount", EOQualifier.QualifierOperatorEqual, Long.valueOf(82));
			//EOQualifier q3 = new EOKeyValueQualifier("userCount", EOQualifier.QualifierOperatorLessThan, Long.valueOf(82));
			EOQualifier q3 = new EOKeyValueQualifier("userCount", EOQualifier.QualifierOperatorLessThanOrEqualTo, Long.valueOf(802));
			EOQualifier qualifier = new EOAndQualifier(new NSArray<EOQualifier>(new EOQualifier[]{q1, q2, q3}));
			EOFetchSpecification fs = new EOFetchSpecification("LuceneAsset", qualifier, null);
			fs.setFetchLimit(5);
			fs.setSortOrderings(new NSArray(new EOSortOrdering("userCount", EOSortOrdering.CompareAscending)));
			Query query = ERLuceneAdaptorChannel.queryForQualifier(new EOKeyValueQualifier("userCount", EOQualifier.QualifierOperatorLessThanOrEqualTo, 802), EOUtilities.entityNamed(ec, "LuceneAsset"));
			fs.setHints(new NSDictionary(query, ERLuceneAdaptor.QUERY_HINTS));
			NSArray<EOEnterpriseObject> result = ec.objectsWithFetchSpecification(fs);
			log.info("{}: {}", result.count(), result.valueForKey("userCount"));
			for (EOEnterpriseObject eo : result) {
				log.info("{}: {}", result.count(), eo);
			}
		} finally {
			ec.unlock();
		}
		return D2W.factory().defaultPage(session());
	}

	public WOActionResults createAction() {
		EOEditingContext ec = ERXEC.newEditingContext();
		ec.lock();
		try {
			Random random = new Random(12);
			for (int i = 0; i < 100; i++) {
				EOEnterpriseObject eo = EOUtilities.createAndInsertInstance(ec, "LuceneAsset");
				eo.takeValueForKey(new NSTimestamp().timestampByAddingGregorianUnits(0, 0, -random.nextInt(2500), 0, 0, 0), "creationDate");
				eo.takeValueForKey(ERXLoremIpsumGenerator.sentence(), "contents");
				eo.takeValueForKey(random.nextDouble(), "price");
				eo.takeValueForKey(Long.valueOf(random.nextInt(2500)), "userCount");
			}
			ec.saveChanges();
		} finally {
			ec.unlock();
		}
		return pageWithName(Main.class.getName());
	}
}

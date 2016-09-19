package er.movies;

import webobjectsexamples.businesslogic.movies.common.Movie;

import com.webobjects.eoaccess.EODatabaseDataSource;
import com.webobjects.eocontrol.EOGenericRecord;
import com.webobjects.foundation.NSMutableArray;

import er.chronic.Options;
import er.extensions.appserver.ERXSession;
import er.extensions.batching.ERXBatchingDisplayGroup;

public class Session extends ERXSession {
	
    private static final long serialVersionUID = 1L;
    private ERXBatchingDisplayGroup<Movie> movieDisplayGroup;
    private ChronicFormatter releaseDateFormatter;
    private int maxSeqId;
    private int previousSeqId;
    private NSMutableArray<Transaction> transactions;
    
	public Session() {
		setStoresIDsInCookies(true);
		setStoresIDsInURLs(false);
	}

    public ERXBatchingDisplayGroup<Movie> movieDisplayGroup() {
        if (movieDisplayGroup == null) {
            EODatabaseDataSource dataSource = new EODatabaseDataSource(
                    defaultEditingContext(), 
                    Movie.ENTITY_NAME);

            movieDisplayGroup = new ERXBatchingDisplayGroup<>();
            movieDisplayGroup.setDataSource(dataSource);
            movieDisplayGroup.setNumberOfObjectsPerBatch(10);
            movieDisplayGroup.setSortOrderings(Movie.TITLE.ascInsensitives());
        }
        return movieDisplayGroup;
    }

    public ChronicFormatter releaseDateFormatter() {
        if (releaseDateFormatter == null) {
            releaseDateFormatter = new ChronicFormatter(
                    "MMMM dd, yyyy", 
                    new Options());
        }
        return releaseDateFormatter;
    }

	public int maxSeqId() {
		return maxSeqId;
	}

	public void setMaxSeqId(int seqId) {
		maxSeqId = seqId;
	}
	
	public int previousSeqId() {
		// New session
		if (previousSeqId == 0) {
			if (transactions().valueForKeyPath("@min.seqId") != null) {
				previousSeqId = Integer.valueOf(transactions().valueForKeyPath("@min.seqId").toString());
				previousSeqId = previousSeqId - 1;
			}
		}
		return previousSeqId;
	}

	public void setPreviousSeqId(int previousSeqId) {
		this.previousSeqId = previousSeqId;
	}
	
	public NSMutableArray<Transaction> transactions() {
		if (transactions == null) {
			transactions = new NSMutableArray<>();
		}
		return transactions;
	}
	
	public void setTransactions(NSMutableArray<Transaction> transactions) {
		this.transactions = transactions;
	}

	public class Transaction {
		private EOGenericRecord record;
		private int seqId;
		private String state;
		
		public Transaction() {
		}

		public Transaction(EOGenericRecord record, int seqId, String state) {
			setRecord(record);
			setSeqId(seqId);
			setState(state);
		}
		
		public EOGenericRecord record() {
			return record;
		}

		public void setRecord(EOGenericRecord record) {
			this.record = record;
		}

		public int seqId() {
			return seqId;
		}

		public void setSeqId(int seqId) {
			this.seqId = seqId;
		}

		public String state() {
			return state;
		}

		public void setState(String state) {
			this.state = state;
		}
	}
    
}

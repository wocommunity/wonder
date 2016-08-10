package er.rest.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.eof.ERXEC;

public class ERXRestTransactionManager {
	private NSMutableDictionary<String, ERXRestTransaction> _transactions;
	private IntRangeSet _sequenceIDs;

	public ERXRestTransactionManager() {
		_transactions = new NSMutableDictionary<String, ERXRestTransaction>();
		_sequenceIDs = new IntRangeSet();
	}

	protected EOEditingContext newEditingContext() {
		return ERXEC.newEditingContext();
	}

	public void addSequenceID(int sequenceID) {
		_sequenceIDs.add(sequenceID);
	}

	public ERXRestTransaction transactionForID(String id) {
		ERXRestTransaction transaction = _transactions.objectForKey(id);
		if (transaction == null) {
			transaction = new ERXRestTransaction(id, newEditingContext());
			_transactions.setObjectForKey(transaction, id);
		}
		return transaction;
	}
	
	public void removeTransaction(ERXRestTransaction transaction) {
		_transactions.removeObjectForKey(transaction.identifier());
	}

	public boolean isTransactionReady(ERXRestTransaction transaction) {
		boolean transactionReady = false;
		// If we have a commit, the transaction might be ready
		if (transaction.hasCommit()) {
			int minimumSequenceID = transaction.minimumSequenceID();
			int maximumSequenceID = transaction.maximumSequenceID();
			// If we've seen every sequence ID in this transaction, the transaction might be ready
			if (_sequenceIDs.contains(minimumSequenceID, maximumSequenceID)) {
				// If the minimum sequence ID was the lowest we can possibly see, the transaction is ready
				if (minimumSequenceID == minimumPossibleSequenceID()) {
					transactionReady = true;
				}
				// If we've seen the sequence ID just below the minimum, the transaction is ready
				else if (_sequenceIDs.contains(minimumSequenceID - 1)) {
					transactionReady = true;
				}
				else {
					// This is the vague part. Say the transaction is [5-10] and 4 got dropped by the
					// client somehow. WTF now? Technically the transaction is ready, but we have no
					// way to know because we don't know if 4 was the real start of the transaction.
					// To protect against a hung transaction, we should probably let it go, but
					// from a safety perspective, we just don't know.
				}
			}
		}
		return transactionReady;
	}

	protected int minimumPossibleSequenceID() {
		return 1;
	}

	public static class IntRangeSet {
		private List<IntRange> _ranges;

		public IntRangeSet() {
			_ranges = new LinkedList<IntRange>();
		}

		public boolean contains(int start, int end) {
			boolean contains = false;
			for (IntRange range : _ranges) {
				if (start >= range.start && end <= range.end) {
					contains = true;
					break;
				}
				else if (range.start > start) {
					break;
				}
			}
			return contains;
		}

		public boolean contains(int value) {
			boolean contains = false;
			for (IntRange range : _ranges) {
				if (value >= range.start && value <= range.end) {
					contains = true;
					break;
				}
				else if (range.start > value) {
					break;
				}
			}
			return contains;
		}

		public void add(int value) {
			Iterator<IntRange> rangeIter = _ranges.iterator();
			int index = 0;
			boolean valueAdded = false;
			while (rangeIter.hasNext()) {
				IntRange range = rangeIter.next();
				if (value <= range.end) {
					if (value < range.start) {
						if (value == range.start - 1) {
							range.start = value;
						}
						else {
							_ranges.add(index, new IntRange(value, value));
						}
					}
					valueAdded = true;
					break;
				}
				else if (value == range.end + 1) {
					range.end = value;
					if (rangeIter.hasNext()) {
						IntRange nextRange = rangeIter.next();
						if (nextRange.start == range.end + 1) {
							range.end = nextRange.end;
							rangeIter.remove();
						}
					}
					valueAdded = true;
					break;
				}
				index++;
			}
			if (!valueAdded) {
				_ranges.add(new IntRange(value, value));
			}
		}

		protected static class IntRange {
			public int start;
			public int end;

			public IntRange(int start, int end) {
				this.start = start;
				this.end = end;
			}

			public boolean contains(int value) {
				return value >= start && value <= end;
			}

			@Override
			public String toString() {
				return "[" + start + "-" + end + "]";
			}
		}

	}
}

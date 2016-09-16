package er.rest.util;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import com.webobjects.eocontrol.EOEditingContext;

public class ERXRestTransaction {
	public static enum State {
		Open, Commit
	}

	private int _minimumSequenceID;
	private int _maximumSequenceID;
	private boolean _hasCommit;
	private String _identifier;
	private EOEditingContext _editingContext;
	private Map<Integer, Object> _records;

	public ERXRestTransaction(String identifier, EOEditingContext editingContext) {
		_minimumSequenceID = Integer.MAX_VALUE;
		_maximumSequenceID = Integer.MIN_VALUE;
		_identifier = identifier;
		_editingContext = editingContext;
		_records = new TreeMap<>();
	}

	public int minimumSequenceID() {
		return _minimumSequenceID;
	}

	public int maximumSequenceID() {
		return _maximumSequenceID;
	}

	public boolean hasCommit() {
		return _hasCommit;
	}

	public int size() {
		return _records.size();
	}
	
	public void addEvent(int sequenceID, ERXRestTransaction.State state, Object record) {
		if (sequenceID < _minimumSequenceID) {
			_minimumSequenceID = sequenceID;
		}
		if (sequenceID > _maximumSequenceID) {
			_maximumSequenceID = sequenceID;
		}
		if (state == ERXRestTransaction.State.Commit) {
			_hasCommit = true;
		}
		_records.put(Integer.valueOf(sequenceID), record);
	}

	public Collection<Object> records() {
		return _records.values();
	}

	public String identifier() {
		return _identifier;
	}

	public EOEditingContext editingContext() {
		return _editingContext;
	}
}

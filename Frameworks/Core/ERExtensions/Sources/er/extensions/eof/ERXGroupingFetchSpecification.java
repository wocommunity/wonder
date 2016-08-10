package er.extensions.eof;

import java.util.Iterator;

import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;

/**
 * Extended fetch specification (work in progress). 
 * <ul>
 * <li>has grouping support</li>
 * </ul>
 * @author ak
 *
 * @param <T>
 */
public class ERXGroupingFetchSpecification<T extends NSDictionary> extends ERXFetchSpecification {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * List of supported aggregate operators.
	 */
	public static interface Operators {
		public String SUM = "sum";
		public String AVG = "avg";
		public String MIN = "min";
		public String MAX = "max";
		public String CNT = "count";
	}
	
	private static class Aggregate {
		
		private String _operator;
		private String _keypath;
		
		public Aggregate(String operator, String keypath) {
			_operator = operator;
			_keypath = keypath;
		}
		
		public String operator() {
			return _operator;
		}
		
		public String keyPath() {
			return _keypath;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((_keypath == null) ? 0 : _keypath.hashCode());
			result = prime * result + ((_operator == null) ? 0 : _operator.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final Aggregate other = (Aggregate) obj;
			if (_keypath == null) {
				if (other._keypath != null)
					return false;
			}
			else if (!_keypath.equals(other._keypath))
				return false;
			if (_operator == null) {
				if (other._operator != null)
					return false;
			}
			else if (!_operator.equals(other._operator))
				return false;
			return true;
		}
		
		@Override
		public String toString() {
			return _operator + "(" + _keypath + ")";
		}
	}
	
	private NSMutableArray<Aggregate> _aggegrateKeyPaths = new NSMutableArray();
	private EOQualifier _havingQualifier;

	public ERXGroupingFetchSpecification(String entityName, EOQualifier qualifier, NSArray sortOrderings, NSArray<Aggregate> aggregates, EOQualifier havingQualifier) {
		super(entityName, qualifier, sortOrderings);
		init();
		setAggregates(aggregates);
		setHavingQualifier(havingQualifier);
	}

	public ERXGroupingFetchSpecification(EOFetchSpecification spec) {
		super(spec);
		init();
	}

	public ERXGroupingFetchSpecification(ERXFetchSpecification spec) {
		super(spec);
		init();
	}

	public ERXGroupingFetchSpecification(ERXGroupingFetchSpecification<T> spec) {
		this((ERXFetchSpecification)spec);
		setAggregates(spec.aggregates());
		setHavingQualifier(spec.havingQualifier());
	}
	
	private void init() {
		setFetchesRawRows(true);
		setRawRowKeyPaths(EOClassDescription.classDescriptionForEntityName(entityName()).attributeKeys());
	}

	@Override
	public void setRawRowKeyPaths(NSArray keyPaths) {
		super.setRawRowKeyPaths(keyPaths);
	}
	
	public NSArray<Aggregate> aggregates() {
		return _aggegrateKeyPaths.immutableClone();
	}

	public void setAggregates(NSArray<Aggregate> value) {
		_aggegrateKeyPaths.removeAllObjects();
		_aggegrateKeyPaths.addObjectsFromArray(value);
	}

	public void addAggregateForPath(String operator, String keyPath) {
		_aggegrateKeyPaths.addObject(new Aggregate(operator, keyPath));
	}

	public void removeAggregateForPath(String operator, String keyPath) {
		_aggegrateKeyPaths.removeObject(new Aggregate(operator, keyPath));
	}
	
	public NSArray<String> groupingKeyPaths() {
		NSMutableArray<String> result = new NSMutableArray<String>();
		result.addObjectsFromArray(rawRowKeyPaths());
		for (Iterator<String> iterator = result.iterator(); iterator.hasNext();) {
			String key = iterator.next();
			for (Iterator<Aggregate> iterator2 = aggregates().iterator(); iterator2.hasNext();) {
				Aggregate aggregate = iterator2.next();
				if(aggregate.keyPath().equals(key)) {
					iterator.remove();
				}
			}
		}
		return result;
	}

	public EOQualifier havingQualifier() {
		return _havingQualifier;
	}

	public void setHavingQualifier(EOQualifier qualifier) {
		_havingQualifier = qualifier;
	}
	
	@Override
	protected String additionalIdentifierInfo() {
		return super.additionalIdentifierInfo() + aggregates() + identifierForQualifier(havingQualifier());
	}

	/**
	 * Type-safe method to fetch the rows for this fetch spec.
	 * @param ec
	 */
	@Override
	public NSArray<T> fetchObjects(EOEditingContext ec) {
		NSArray oldKeyPaths = rawRowKeyPaths();
		try {
			setRawRowKeyPaths(groupingKeyPaths());
			return super.fetchObjects(ec);
		} finally {
			setRawRowKeyPaths(oldKeyPaths);
		}
	}
}

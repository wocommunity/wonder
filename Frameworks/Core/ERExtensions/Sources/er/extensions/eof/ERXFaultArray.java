package er.extensions.eof;

import java.lang.ref.WeakReference;
import java.util.Enumeration;
import java.util.Iterator;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.foundation.NSArray;

/**
 * Array that converts EOGlobalIDs to faults on the fly. The idea is that you can use this in
 * place of an array of real EOs, which may consume quite a lot of memory when instantiated in an EC.
 * May or may not only work without the Wonder version of NSArray.
 * @author ak
 *
 * @param <T>
 */
public class ERXFaultArray<T extends EOEnterpriseObject> extends NSArray<T> {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private EOEditingContext _editingContext;
	private EOGlobalID[] _array;
	private WeakReference<T>[] _refs;

	public ERXFaultArray(NSArray<T> array) {
		_refs = new WeakReference[array.size()];
		_array = new EOGlobalID[array.size()];
		if(array.count() > 0) {
			setEditingContext(array.lastObject().editingContext());
			int i = 0;
			for (T eo : array) {
				_refs[i] = new WeakReference<T>(eo);
				_array[i] = editingContext().globalIDForObject(eo);
				i++;
			}
		}
	}
	
	public ERXFaultArray(EOEditingContext ec, NSArray<EOGlobalID> array) {
		setEditingContext(ec);
		_refs = new WeakReference[array.size()];
		_array = new EOGlobalID[array.size()];
		int i = 0;
		for (EOGlobalID gid : array) {
			_refs[i] = null;
			_array[i] = gid;
			i++;
		}
	}
	
	@Override
	public T objectAtIndex(int index) {
		int count = count();
		if (index >= 0 && index < count) {
			T result = null;
			WeakReference<T> ref = _refs[index];
			if(ref != null) {
				result = ref.get();
			}
			if(result == null) {
				EOGlobalID gid = _array[index];
				result = (T)editingContext().objectForGlobalID(gid);
				if(result == null) {
					result = (T)editingContext().faultForGlobalID(gid, editingContext());
					// System.out.println("fault: " + result.isFault());
				} else {
					// System.out.println("object: " + result.isFault());
				}
				_refs[index] = new WeakReference<T>(result);
			} else {
				// System.out.println("no fault");
			}
			return result;
		}
		if (count == 0) {
			throw new IllegalArgumentException("Array is empty");
		}
		throw new IllegalArgumentException("Index (" + index + ") out of bounds [0, " + (count() - 1) + "]");
	}
	
	@Override
	public boolean containsObject(Object object) {
		if (object instanceof EOEnterpriseObject) {
			EOEnterpriseObject eo = (EOEnterpriseObject) object;
			EOGlobalID gid = editingContext().globalIDForObject(eo);
			for (int i = 0; i < count(); i++) {
				EOGlobalID current = _array[i];
				if(current.equals(gid)) {
					return true;
				}
			}
			return false;
		}
		return super.containsObject(object);
	}
	
	@Override
	public int indexOfObject(Object object) {
		if (object instanceof EOEnterpriseObject) {
			EOEnterpriseObject eo = (EOEnterpriseObject) object;
			EOGlobalID gid = editingContext().globalIDForObject(eo);
			for (int i = 0; i < count(); i++) {
				EOGlobalID current = _array[i];
				if(current.equals(gid)) {
					return i;
				}
			}
			return NotFound;
		}
		return super.indexOfObject(object);
	}
	
	@Override
	public int count() {
		return _array != null ? _array.length : 0;
	}

	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {
			int index = 0;
			public boolean hasNext() {
				return index < count();
			}

			public T next() {
				return objectAtIndex(index++);
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
			
		};
	}
	
	@Override
	public Enumeration objectEnumerator() {
		return new Enumeration<T>() {
			
			int index = 0;

			public boolean hasMoreElements() {
				return index < count();
			}

			public T nextElement() {
				return objectAtIndex(index++);
			}
			
		};
	}
	
	@Override
	public Enumeration<T> reverseObjectEnumerator() {
	return new Enumeration<T>() {
			
			int index = count();

			public boolean hasMoreElements() {
				return index > 0;
			}

			public T nextElement() {
				return objectAtIndex(--index);
			}
		};
	}
	
	@Override
	protected Object[] _objects() {
		Object[] result = new Object[count()];
		for (int i = 0; i < count(); i++) {
			result[i] = objectAtIndex(i);
		}
		return result;
	}
	
	public EOEditingContext editingContext() {
		return _editingContext;
	}
	
	@Override
	public Object clone() {
		ERXFaultArray other = new ERXFaultArray(_editingContext, NSArray.EmptyArray);
		other._array = _array;
		return other;
	}
	
	public void setEditingContext(EOEditingContext ec) {
		if(_editingContext != ec) {
			for (int i = 0; i < count(); i++) {
				_refs[i] = null;
			}
		}
		_editingContext = ec;
	}
}

package er.extensions.foundation;


import com.webobjects.foundation.*;
import java.io.*;
import java.util.*;

/**
 * Custom subclass of NSMutableArray. Implements {@see java.util.List} and
 * can be used as a EOF custom value class because it can automatically
 * en- and decode an NSMutableArray as blob into a database.
 * NOTE: As the List implementation
 * is based on the NSMutableArray implementation, care must be taken when subclassing;
 * it is best if you use only List-methods when extending List-methods and NSArray
 * methods in other cases. Otherwise you will most likely get into stack overflows.
 * NOTE: List allows for NULL values, NSMutableArray does not. Therefore you can't
 * use NULL objects.
 * The ERPrototype name is <code>mutableArray</code>
 */
public class ERXMutableArray extends NSMutableArray implements List {
    public static final long serialVersionUID = -6581075256974648875L;

    public ERXMutableArray() {
        super();
    }

    public ERXMutableArray(Collection c) {
        super(c.toArray());
    }

    public ERXMutableArray(NSArray array) {
        super(array);
    }

    public ERXMutableArray(int i) {
       super(i);
    }

    public ERXMutableArray(Object obj) {
        super(obj);
    }

    public ERXMutableArray(Object aobj[]) {
        super(aobj);
    }

    public ERXMutableArray(Object objects[], NSRange range) {
        super(objects, range);
    }

    public ERXMutableArray(Vector vector, NSRange range, boolean flag) {
       super(vector, range, flag);
    }

    public static NSData toBlob(ERXMutableArray d) throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bout);
        oos.writeObject(d);
        oos.close();
        NSData sp = new NSData(bout.toByteArray());
        return sp;
    }
    public static ERXMutableArray fromBlob(NSData d) throws Exception {
        ByteArrayInputStream bis = new ByteArrayInputStream(d.bytes());
        ObjectInputStream ois = new ObjectInputStream(bis);
        ERXMutableArray dd = (ERXMutableArray) ois.readObject();
        ois.close();
        return dd;
    }

    public NSData toBlob() throws Exception {
        return toBlob(this);
    }

    public NSMutableArray mutableClone() {
        return new ERXMutableArray((NSArray)this);
    }

    /**
     * Removes a single instance of the specified element from this
     * collection, if it is present (optional operation).  More formally,
     * removes an element <tt>e</tt> such that <tt>(o==null ? e==null :
                                                    * o.equals(e))</tt>, if the collection contains one or more such
     * elements.  Returns <tt>true</tt> if the collection contained the
     * specified element (or equivalently, if the collection changed as a
                          * result of the call).<p>
     *
     * This implementation iterates over the collection looking for the
     * specified element.  If it finds the element, it removes the element
     * from the collection using the iterator's remove method.<p>
     *
     * Note that this implementation throws an
     * <tt>UnsupportedOperationException</tt> if the iterator returned by this
     * collection's iterator method does not implement the <tt>remove</tt>
     * method and this collection contains the specified object.
     *
     * @param o element to be removed from this collection, if present.
     * @return <tt>true</tt> if the collection contained the specified
     *         element.
     * @throws UnsupportedOperationException if the <tt>remove</tt> method is
     * 		  not supported by this collection.
     */
    public boolean remove(Object o) {
        Iterator e = iterator();
        if (o==null) {
            while (e.hasNext()) {
                if (e.next()==null) {
                    e.remove();
                    return true;
                }
            }
        } else {
            while (e.hasNext()) {
                if (o.equals(e.next())) {
                    e.remove();
                    return true;
                }
            }
        }
        return false;
    }

    /**
        * Returns <tt>true</tt> if this collection contains all of the elements
     * in the specified collection. <p>
     *
     * This implementation iterates over the specified collection, checking
     * each element returned by the iterator in turn to see if it's
     * contained in this collection.  If all elements are so contained
     * <tt>true</tt> is returned, otherwise <tt>false</tt>.
     *
     * @param c collection to be checked for containment in this collection.
     * @return <tt>true</tt> if this collection contains all of the elements
     * 	       in the specified collection.
     * @throws NullPointerException if the specified collection is null.
     *
     * @see #contains(Object)
     */
    public boolean containsAll(Collection c) {
        Iterator e = c.iterator();
        while (e.hasNext())
            if(!contains(e.next()))
                return false;

        return true;
    }

    /**
        * Adds all of the elements in the specified collection to this collection
     * (optional operation).  The behavior of this operation is undefined if
     * the specified collection is modified while the operation is in
     * progress.  (This implies that the behavior of this call is undefined if
                   * the specified collection is this collection, and this collection is
                   * nonempty.) <p>
     *
     * This implementation iterates over the specified collection, and adds
     * each object returned by the iterator to this collection, in turn.<p>
     *
     * Note that this implementation will throw an
     * <tt>UnsupportedOperationException</tt> unless <tt>add</tt> is
     * overridden (assuming the specified collection is non-empty).
     *
     * @param c collection whose elements are to be added to this collection.
     * @return <tt>true</tt> if this collection changed as a result of the
     *         call.
     * @throws UnsupportedOperationException if this collection does not
     *         support the <tt>addAll</tt> method.
     * @throws NullPointerException if the specified collection is null.
     *
     * @see #add(Object)
     */
    public boolean addAll(Collection c) {
        boolean modified = false;
        Iterator e = c.iterator();
        while (e.hasNext()) {
            if(add(e.next()))
                modified = true;
        }
        return modified;
    }

    /**
        * Removes from this collection all of its elements that are contained in
     * the specified collection (optional operation). <p>
     *
     * This implementation iterates over this collection, checking each
     * element returned by the iterator in turn to see if it's contained
     * in the specified collection.  If it's so contained, it's removed from
     * this collection with the iterator's <tt>remove</tt> method.<p>
     *
     * Note that this implementation will throw an
     * <tt>UnsupportedOperationException</tt> if the iterator returned by the
     * <tt>iterator</tt> method does not implement the <tt>remove</tt> method
     * and this collection contains one or more elements in common with the
     * specified collection.
     *
     * @param c elements to be removed from this collection.
     * @return <tt>true</tt> if this collection changed as a result of the
     *         call.
     * @throws UnsupportedOperationException if the <tt>removeAll</tt> method
     * 	       is not supported by this collection.
     * @throws NullPointerException if the specified collection is null.
     *
     * @see #remove(Object)
     * @see #contains(Object)
     */
    public boolean removeAll(Collection c) {
        boolean modified = false;
        Iterator e = iterator();
        while (e.hasNext()) {
            if(c.contains(e.next())) {
                e.remove();
                modified = true;
            }
        }
        return modified;
    }

    /**
        * Retains only the elements in this collection that are contained in the
     * specified collection (optional operation).  In other words, removes
     * from this collection all of its elements that are not contained in the
     * specified collection. <p>
     *
     * This implementation iterates over this collection, checking each
     * element returned by the iterator in turn to see if it's contained
     * in the specified collection.  If it's not so contained, it's removed
     * from this collection with the iterator's <tt>remove</tt> method.<p>
     *
     * Note that this implementation will throw an
     * <tt>UnsupportedOperationException</tt> if the iterator returned by the
     * <tt>iterator</tt> method does not implement the <tt>remove</tt> method
     * and this collection contains one or more elements not present in the
     * specified collection.
     *
     * @param c elements to be retained in this collection.
     * @return <tt>true</tt> if this collection changed as a result of the
     *         call.
     * @throws UnsupportedOperationException if the <tt>retainAll</tt> method
     * 	       is not supported by this Collection.
     * @throws NullPointerException if the specified collection is null.
     *
     * @see #remove(Object)
     * @see #contains(Object)
     */
    public boolean retainAll(Collection c) {
        boolean modified = false;
        Iterator e = iterator();
        while (e.hasNext()) {
            if(!c.contains(e.next())) {
                e.remove();
                modified = true;
            }
        }
        return modified;
    }


    /**
     * Trims the capacity of this <tt>ERXMutableArray</tt> instance to be the
     * list's current size.  An application can use this operation to minimize
     * the storage of an <tt>ERXMutableArray</tt> instance.
     */
    public void trimToSize() {
	modCount++;
        // NO-OP
    }

    /**
     * Increases the capacity of this <tt>ERXMutableArray</tt> instance, if
     * necessary, to ensure  that it can hold at least the number of elements
     * specified by the minimum capacity argument.
     *
     * @param   minCapacity   the desired minimum capacity.
     */
    public void ensureCapacity(int minCapacity) {
	modCount++;
        // NO-OP
    }

    /**
     * Returns the number of elements in this list.
     *
     * @return  the number of elements in this list.
     */
    public int size() {
	return count();
    }

    /**
     * Tests if this list has no elements.
     *
     * @return  <tt>true</tt> if this list has no elements;
     *          <tt>false</tt> otherwise.
     */
    public boolean isEmpty() {
	return count() == 0;
    }

    /**
     * Returns <tt>true</tt> if this list contains the specified element.
     *
     * @param elem element whose presence in this List is to be tested.
     * @return  <code>true</code> if the specified element is present;
     *		<code>false</code> otherwise.
     */
    public boolean contains(Object elem) {
	return containsObject(elem);
    }

    /**
     * Searches for the first occurence of the given argument, testing
     * for equality using the <tt>equals</tt> method.
     *
     * @param   elem   an object.
     * @return  the index of the first occurrence of the argument in this
     *          list; returns <tt>-1</tt> if the object is not found.
     * @see     Object#equals(Object)
     */
    public int indexOf(Object elem) {
	return indexOfObject(elem);
    }

    /**
     * Returns the index of the last occurrence of the specified object in
     * this list.
     *
     * @param   elem   the desired element.
     * @return  the index of the last occurrence of the specified object in
     *          this list; returns -1 if the object is not found.
     */
    public int lastIndexOf(Object elem) {
        Object[] elementData = objectsNoCopy();
        int size = count();
	if (elem == null) {
	    for (int i = size-1; i >= 0; i--)
		if (elementData[i]==null)
		    return i;
	} else {
	    for (int i = size-1; i >= 0; i--)
		if (elem.equals(elementData[i]))
		    return i;
	}
	return -1;
    }

    /**
     * Returns an array containing all of the elements in this list
     * in the correct order.
     *
     * @return an array containing all of the elements in this list
     * 	       in the correct order.
     */
    public Object[] toArray() {
	return objects();
    }

    /**
     * Returns an array containing all of the elements in this list in the
     * correct order; the runtime type of the returned array is that of the
     * specified array.  If the list fits in the specified array, it is
     * returned therein.  Otherwise, a new array is allocated with the runtime
     * type of the specified array and the size of this list.<p>
     *
     * If the list fits in the specified array with room to spare (i.e., the
     * array has more elements than the list), the element in the array
     * immediately following the end of the collection is set to
     * <tt>null</tt>.  This is useful in determining the length of the list
     * <i>only</i> if the caller knows that the list does not contain any
     * <tt>null</tt> elements.
     *
     * @param a the array into which the elements of the list are to
     *		be stored, if it is big enough; otherwise, a new array of the
     * 		same runtime type is allocated for this purpose.
     * @return an array containing the elements of the list.
     * @throws ArrayStoreException if the runtime type of a is not a supertype
     *         of the runtime type of every element in this list.
     */
    public Object[] toArray(Object a[]) {
        Object[] elementData = objectsNoCopy();
        int size = count();
        if (a.length < size)
            a = (Object[])java.lang.reflect.Array.newInstance(
                                a.getClass().getComponentType(), size);

	System.arraycopy(elementData, 0, a, 0, size);

        if (a.length > size)
            a[size] = null;

        return a;
    }

    // Positional Access Operations

    /**
     * Returns the element at the specified position in this list.
     *
     * @param  index index of element to return.
     * @return the element at the specified position in this list.
     * @throws    IndexOutOfBoundsException if index is out of range <tt>(index
     * 		  &lt; 0 || index &gt;= size())</tt>.
     */
    public Object get(int index) {
	return objectAtIndex(index);
    }

    /**
     * Replaces the element at the specified position in this list with
     * the specified element.
     *
     * @param index index of element to replace.
     * @param element element to be stored at the specified position.
     * @return the element previously at the specified position.
     * @throws    IndexOutOfBoundsException if index out of range
     *		  <tt>(index &lt; 0 || index &gt;= size())</tt>.
     */
    public Object set(int index, Object element) {
	Object oldValue = objectAtIndex(index);
	replaceObjectAtIndex(element, index);
	return oldValue;
    }

    /**
     * Appends the specified element to the end of this list.
     *
     * @param o element to be appended to this list.
     * @return <tt>true</tt> (as per the general contract of Collection.add).
     */
    public boolean add(Object o) {
        addObject(o);
	return true;
    }

    /**
     * Inserts the specified element at the specified position in this
     * list. Shifts the element currently at that position (if any) and
     * any subsequent elements to the right (adds one to their indices).
     *
     * @param index index at which the specified element is to be inserted.
     * @param element element to be inserted.
     * @throws    IndexOutOfBoundsException if index is out of range
     *		  <tt>(index &lt; 0 || index &gt; size())</tt>.
     */
    public void add(int index, Object element) {
        insertObjectAtIndex(element, index);
    }

    /**
     * Removes the element at the specified position in this list.
     * Shifts any subsequent elements to the left (subtracts one from their
     * indices).
     *
     * @param index the index of the element to removed.
     * @return the element that was removed from the list.
     * @throws    IndexOutOfBoundsException if index out of range <tt>(index
     * 		  &lt; 0 || index &gt;= size())</tt>.
     */
    public Object remove(int index) {
        Object oldValue = objectAtIndex(index);
        removeObjectAtIndex(index);
	return oldValue;
    }

    // Search Operations

    // Bulk Operations

    /**
     * Removes all of the elements from this collection (optional operation).
     * The collection will be empty after this call returns (unless it throws
     * an exception).<p>
     *
     * This implementation calls <tt>removeRange(0, size())</tt>.<p>
     *
     * Note that this implementation throws an
     * <tt>UnsupportedOperationException</tt> unless <tt>remove(int
     * index)</tt> or <tt>removeRange(int fromIndex, int toIndex)</tt> is
     * overridden.
     *
     * @throws UnsupportedOperationException if the <tt>clear</tt> method is
     * 		  not supported by this Collection.
     */
    public void clear() {
        removeAllObjects();
    }

    /**
     * Inserts all of the elements in the specified collection into this list
     * at the specified position (optional operation).  Shifts the element
     * currently at that position (if any) and any subsequent elements to the
     * right (increases their indices).  The new elements will appear in the
     * list in the order that they are returned by the specified collection's
     * iterator.  The behavior of this operation is unspecified if the
     * specified collection is modified while the operation is in progress.
     * (Note that this will occur if the specified collection is this list,
     * and it's nonempty.)<p>
     *
     * This implementation gets an iterator over the specified collection and
     * iterates over it, inserting the elements obtained from the iterator
     * into this list at the appropriate position, one at a time, using
     * <tt>add(int, Object)</tt>.  Many implementations will override this
     * method for efficiency.<p>
     *
     * Note that this implementation throws an
     * <tt>UnsupportedOperationException</tt> unless <tt>add(int, Object)</tt>
     * is overridden.
     *
     * @return <tt>true</tt> if this list changed as a result of the call.
     * @param index index at which to insert the first element from the
     *		    specified collection.
     * @param c elements to be inserted into this List.
     *
     * @throws UnsupportedOperationException if the <tt>addAll</tt> method is
     *		  not supported by this list.
     *
     * @throws ClassCastException if the class of an element of the specified
     * 		  collection prevents it from being added to this List.
     *
     * @throws IllegalArgumentException some aspect an element of the
     *		  specified collection prevents it from being added to this
     *		  List.
     *
     * @throws IndexOutOfBoundsException index out of range (<tt>index &lt; 0
     *            || index &gt; size()</tt>).
     *
     * @throws NullPointerException if the specified collection is null.
     */
    public boolean addAll(int index, Collection c) {
	boolean modified = false;
	Iterator e = c.iterator();
	while (e.hasNext()) {
	    add(index++, e.next());
	    modified = true;
	}
	return modified;
    }


    // Iterators

    /**
     * Returns an iterator over the elements in this list in proper
     * sequence. <p>
     *
     * This implementation returns a straightforward implementation of the
     * iterator interface, relying on the backing list's <tt>size()</tt>,
     * <tt>get(int)</tt>, and <tt>remove(int)</tt> methods.<p>
     *
     * Note that the iterator returned by this method will throw an
     * <tt>UnsupportedOperationException</tt> in response to its
     * <tt>remove</tt> method unless the list's <tt>remove(int)</tt> method is
     * overridden.<p>
     *
     * This implementation can be made to throw runtime exceptions in the face
     * of concurrent modification, as described in the specification for the
     * (protected) <tt>modCount</tt> field.
     *
     * @return an iterator over the elements in this list in proper sequence.
     *
     * @see #modCount
     */
    public Iterator iterator() {
	return new Itr();
    }

    /**
     * Returns an iterator of the elements in this list (in proper sequence).
     * This implementation returns <tt>listIterator(0)</tt>.
     *
     * @return an iterator of the elements in this list (in proper sequence).
     *
     * @see #listIterator(int)
     */
    public ListIterator listIterator() {
	return listIterator(0);
    }

    /**
     * Returns a list iterator of the elements in this list (in proper
     * sequence), starting at the specified position in the list.  The
     * specified index indicates the first element that would be returned by
     * an initial call to the <tt>next</tt> method.  An initial call to
     * the <tt>previous</tt> method would return the element with the
     * specified index minus one.<p>
     *
     * This implementation returns a straightforward implementation of the
     * <tt>ListIterator</tt> interface that extends the implementation of the
     * <tt>Iterator</tt> interface returned by the <tt>iterator()</tt> method.
     * The <tt>ListIterator</tt> implementation relies on the backing list's
     * <tt>get(int)</tt>, <tt>set(int, Object)</tt>, <tt>add(int, Object)</tt>
     * and <tt>remove(int)</tt> methods.<p>
     *
     * Note that the list iterator returned by this implementation will throw
     * an <tt>UnsupportedOperationException</tt> in response to its
     * <tt>remove</tt>, <tt>set</tt> and <tt>add</tt> methods unless the
     * list's <tt>remove(int)</tt>, <tt>set(int, Object)</tt>, and
     * <tt>add(int, Object)</tt> methods are overridden.<p>
     *
     * This implementation can be made to throw runtime exceptions in the
     * face of concurrent modification, as described in the specification for
     * the (protected) <tt>modCount</tt> field.
     *
     * @param index index of the first element to be returned from the list
     *		    iterator (by a call to the <tt>next</tt> method).
     *
     * @return a list iterator of the elements in this list (in proper
     * 	       sequence), starting at the specified position in the list.
     *
     * @throws IndexOutOfBoundsException if the specified index is out of
     *		  range (<tt>index &lt; 0 || index &gt; size()</tt>).
     *
     * @see #modCount
     */
    public ListIterator listIterator(final int index) {
	if (index<0 || index>size())
	  throw new IndexOutOfBoundsException("Index: "+index);

	return new ListItr(index);
    }

    private class Itr implements Iterator {
	/**
	 * Index of element to be returned by subsequent call to next.
	 */
	int cursor = 0;

	/**
	 * Index of element returned by most recent call to next or
	 * previous.  Reset to -1 if this element is deleted by a call
	 * to remove.
	 */
	int lastRet = -1;

	/**
	 * The modCount value that the iterator believes that the backing
	 * List should have.  If this expectation is violated, the iterator
	 * has detected concurrent modification.
	 */
	int expectedModCount = modCount;

	public boolean hasNext() {
	    return cursor != size();
	}

	public Object next() {
	    try {
		Object next = get(cursor);
		checkForComodification();
		lastRet = cursor++;
		return next;
	    } catch(IndexOutOfBoundsException e) {
		checkForComodification();
		throw new NoSuchElementException();
	    }
	}

	public void remove() {
	    if (lastRet == -1)
		throw new IllegalStateException();
            checkForComodification();

	    try {
		ERXMutableArray.this.remove(lastRet);
		if (lastRet < cursor)
		    cursor--;
		lastRet = -1;
		expectedModCount = modCount;
	    } catch(IndexOutOfBoundsException e) {
		throw new ConcurrentModificationException();
	    }
	}

	final void checkForComodification() {
	    if (modCount != expectedModCount)
		throw new ConcurrentModificationException();
	}
    }

    private class ListItr extends Itr implements ListIterator {
	ListItr(int index) {
	    cursor = index;
	}

	public boolean hasPrevious() {
	    return cursor != 0;
	}

        public Object previous() {
            try {
                int i = cursor - 1;
                Object previous = get(i);
                checkForComodification();
                lastRet = cursor = i;
                return previous;
            } catch(IndexOutOfBoundsException e) {
                checkForComodification();
                throw new NoSuchElementException();
            }
        }

	public int nextIndex() {
	    return cursor;
	}

	public int previousIndex() {
	    return cursor-1;
	}

	public void set(Object o) {
	    if (lastRet == -1)
		throw new IllegalStateException();
            checkForComodification();

	    try {
		ERXMutableArray.this.set(lastRet, o);
		expectedModCount = modCount;
	    } catch(IndexOutOfBoundsException e) {
		throw new ConcurrentModificationException();
	    }
	}

	public void add(Object o) {
            checkForComodification();

	    try {
		ERXMutableArray.this.add(cursor++, o);
		lastRet = -1;
		expectedModCount = modCount;
	    } catch(IndexOutOfBoundsException e) {
		throw new ConcurrentModificationException();
	    }
	}
    }

    /**
     * Returns a view of the portion of this list between <tt>fromIndex</tt>,
     * inclusive, and <tt>toIndex</tt>, exclusive.  (If <tt>fromIndex</tt> and
     * <tt>toIndex</tt> are equal, the returned list is empty.)  The returned
     * list is backed by this list, so changes in the returned list are
     * reflected in this list, and vice-versa.  The returned list supports all
     * of the optional list operations supported by this list.<p>
     *
     * This method eliminates the need for explicit range operations (of the
     * sort that commonly exist for arrays).  Any operation that expects a
     * list can be used as a range operation by operating on a subList view
     * instead of a whole list.  For example, the following idiom removes a
     * range of elements from a list:
     * <pre>
     *     list.subList(from, to).clear();
     * </pre>
     * Similar idioms may be constructed for <tt>indexOf</tt> and
     * <tt>lastIndexOf</tt>, and all of the algorithms in the
     * <tt>Collections</tt> class can be applied to a subList.<p>
     *
     * The semantics of the list returned by this method become undefined if
     * the backing list (i.e., this list) is <i>structurally modified</i> in
     * any way other than via the returned list.  (Structural modifications are
     * those that change the size of the list, or otherwise perturb it in such
     * a fashion that iterations in progress may yield incorrect results.)<p>
     *
     * This implementation returns a list that subclasses
     * <tt>ERXMutableArray</tt>.  The subclass stores, in private fields, the
     * offset of the subList within the backing list, the size of the subList
     * (which can change over its lifetime), and the expected
     * <tt>modCount</tt> value of the backing list.  There are two variants
     * of the subclass, one of which implements <tt>RandomAccess</tt>.
     * If this list implements <tt>RandomAccess</tt> the returned list will
     * be an instance of the subclass that implements <tt>RandomAccess</tt>.<p>
     *
     * The subclass's <tt>set(int, Object)</tt>, <tt>get(int)</tt>,
     * <tt>add(int, Object)</tt>, <tt>remove(int)</tt>, <tt>addAll(int,
     * Collection)</tt> and <tt>removeRange(int, int)</tt> methods all
     * delegate to the corresponding methods on the backing abstract list,
     * after bounds-checking the index and adjusting for the offset.  The
     * <tt>addAll(Collection c)</tt> method merely returns <tt>addAll(size,
     * c)</tt>.<p>
     *
     * The <tt>listIterator(int)</tt> method returns a "wrapper object" over a
     * list iterator on the backing list, which is created with the
     * corresponding method on the backing list.  The <tt>iterator</tt> method
     * merely returns <tt>listIterator()</tt>, and the <tt>size</tt> method
     * merely returns the subclass's <tt>size</tt> field.<p>
     *
     * All methods first check to see if the actual <tt>modCount</tt> of the
     * backing list is equal to its expected value, and throw a
     * <tt>ConcurrentModificationException</tt> if it is not.
     *
     * @param fromIndex low endpoint (inclusive) of the subList.
     * @param toIndex high endpoint (exclusive) of the subList.
     * @return a view of the specified range within this list.
     * @throws IndexOutOfBoundsException endpoint index value out of range
     *         <tt>(fromIndex &lt; 0 || toIndex &gt; size)</tt>
     * @throws IllegalArgumentException endpoint indices out of order
     * <tt>(fromIndex &gt; toIndex)</tt> */
    public List subList(int fromIndex, int toIndex) {
        return (this instanceof RandomAccess ?
                new RandomAccessSubList(this, fromIndex, toIndex) :
                new SubList(this, fromIndex, toIndex));
    }

    // Comparison and hashing

    /**
     * Removes from this list all of the elements whose index is between
     * <tt>fromIndex</tt>, inclusive, and <tt>toIndex</tt>, exclusive.
     * Shifts any succeeding elements to the left (reduces their index).  This
     * call shortens the ERXMutableArray by <tt>(toIndex - fromIndex)</tt>
     * elements.  (If <tt>toIndex==fromIndex</tt>, this operation has no
     * effect.)<p>
     *
     * This method is called by the <tt>clear</tt> operation on this list
     * and its subLists.  Overriding this method to take advantage of
     * the internals of the list implementation can <i>substantially</i>
     * improve the performance of the <tt>clear</tt> operation on this list
     * and its subLists.<p>
     *
     * This implementation gets a list iterator positioned before
     * <tt>fromIndex</tt>, and repeatedly calls <tt>ListIterator.next</tt>
     * followed by <tt>ListIterator.remove</tt> until the entire range has
     * been removed.  <b>Note: if <tt>ListIterator.remove</tt> requires linear
     * time, this implementation requires quadratic time.</b>
     *
     * @param fromIndex index of first element to be removed.
     * @param toIndex index after last element to be removed.
     */
    protected void removeRange(int fromIndex, int toIndex) {
        ListIterator it = listIterator(fromIndex);
        for (int i=0, n=toIndex-fromIndex; i<n; i++) {
            it.next();
            it.remove();
        }
    }

    /**
     * The number of times this list has been <i>structurally modified</i>.
     * Structural modifications are those that change the size of the
     * list, or otherwise perturb it in such a fashion that iterations in
     * progress may yield incorrect results.<p>
     *
     * This field is used by the iterator and list iterator implementation
     * returned by the <tt>iterator</tt> and <tt>listIterator</tt> methods.
     * If the value of this field changes unexpectedly, the iterator (or list
     * iterator) will throw a <tt>ConcurrentModificationException</tt> in
     * response to the <tt>next</tt>, <tt>remove</tt>, <tt>previous</tt>,
     * <tt>set</tt> or <tt>add</tt> operations.  This provides
     * <i>fail-fast</i> behavior, rather than non-deterministic behavior in
     * the face of concurrent modification during iteration.<p>
     *
     * <b>Use of this field by subclasses is optional.</b> If a subclass
     * wishes to provide fail-fast iterators (and list iterators), then it
     * merely has to increment this field in its <tt>add(int, Object)</tt> and
     * <tt>remove(int)</tt> methods (and any other methods that it overrides
     * that result in structural modifications to the list).  A single call to
     * <tt>add(int, Object)</tt> or <tt>remove(int)</tt> must add no more than
     * one to this field, or the iterators (and list iterators) will throw
     * bogus <tt>ConcurrentModificationExceptions</tt>.  If an implementation
     * does not wish to provide fail-fast iterators, this field may be
     * ignored.
     */
    protected transient int modCount = 0;
}

class SubList extends ERXMutableArray {
    private ERXMutableArray l;
    private int offset;
    private int size;
    private int expectedModCount;

    SubList(ERXMutableArray list, int fromIndex, int toIndex) {
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
        if (toIndex > list.size())
            throw new IndexOutOfBoundsException("toIndex = " + toIndex);
        if (fromIndex > toIndex)
            throw new IllegalArgumentException("fromIndex(" + fromIndex +
                                               ") > toIndex(" + toIndex + ")");
        l = list;
        offset = fromIndex;
        size = toIndex - fromIndex;
        expectedModCount = l.modCount;
    }

    public Object set(int index, Object element) {
        rangeCheck(index);
        checkForComodification();
        return l.set(index+offset, element);
    }

    public Object get(int index) {
        rangeCheck(index);
        checkForComodification();
        return l.get(index+offset);
    }

    public int size() {
        checkForComodification();
        return size;
    }

    public void add(int index, Object element) {
        if (index<0 || index>size)
            throw new IndexOutOfBoundsException();
        checkForComodification();
        l.add(index+offset, element);
        expectedModCount = l.modCount;
        size++;
        modCount++;
    }

    public Object remove(int index) {
        rangeCheck(index);
        checkForComodification();
        Object result = l.remove(index+offset);
        expectedModCount = l.modCount;
        size--;
        modCount++;
        return result;
    }

    protected void removeRange(int fromIndex, int toIndex) {
        checkForComodification();
        l.removeRange(fromIndex+offset, toIndex+offset);
        expectedModCount = l.modCount;
        size -= (toIndex-fromIndex);
        modCount++;
    }

    public boolean addAll(Collection c) {
        return addAll(size, c);
    }

    public boolean addAll(int index, Collection c) {
        if (index<0 || index>size)
            throw new IndexOutOfBoundsException(
                "Index: "+index+", Size: "+size);
        int cSize = c.size();
        if (cSize==0)
            return false;

        checkForComodification();
        l.addAll(offset+index, c);
        expectedModCount = l.modCount;
        size += cSize;
        modCount++;
        return true;
    }

    public Iterator iterator() {
        return listIterator();
    }

    public ListIterator listIterator(final int index) {
        checkForComodification();
        if (index<0 || index>size)
            throw new IndexOutOfBoundsException(
                "Index: "+index+", Size: "+size);

        return new ListIterator() {
            private ListIterator i = l.listIterator(index+offset);

            public boolean hasNext() {
                return nextIndex() < size;
            }

            public Object next() {
                if (hasNext())
                    return i.next();
                else
                    throw new NoSuchElementException();
            }

            public boolean hasPrevious() {
                return previousIndex() >= 0;
            }

            public Object previous() {
                if (hasPrevious())
                    return i.previous();
                else
                    throw new NoSuchElementException();
            }

            public int nextIndex() {
                return i.nextIndex() - offset;
            }

            public int previousIndex() {
                return i.previousIndex() - offset;
            }

            public void remove() {
                i.remove();
                expectedModCount = l.modCount;
                size--;
                modCount++;
            }

            public void set(Object o) {
                i.set(o);
            }

            public void add(Object o) {
                i.add(o);
                expectedModCount = l.modCount;
                size++;
                modCount++;
            }
        };
    }

    public List subList(int fromIndex, int toIndex) {
        return new SubList(this, fromIndex, toIndex);
    }

    private void rangeCheck(int index) {
        if (index<0 || index>=size)
            throw new IndexOutOfBoundsException("Index: "+index+
                                                ",Size: "+size);
    }

    private void checkForComodification() {
        if (l.modCount != expectedModCount)
            throw new ConcurrentModificationException();
    }


}

class RandomAccessSubList extends SubList implements RandomAccess {
    RandomAccessSubList(ERXMutableArray list, int fromIndex, int toIndex) {
        super(list, fromIndex, toIndex);
    }

    public List subList(int fromIndex, int toIndex) {
        return new RandomAccessSubList(this, fromIndex, toIndex);
    }
}
/*
 $Id$

 ERQueue.java - Camille Troillard - tuscland@mac.com
 */

package er.javamail;

import java.util.Vector;

public class ERQueue<T> extends Vector<T> {

	protected int _maxSize = 0;

	public int maxSize() {
		return _maxSize;
	}

	public void setMaxSize(int size) {
		_maxSize = size;
	}

	public static class SizeOverflowException extends Exception {
		public SizeOverflowException() {
			super();
		}
	}

	public ERQueue() {
		this(0);
	}

	public ERQueue(int maxSize) {
		super();
		this.setMaxSize(maxSize);
	}

	public T push(T item) throws SizeOverflowException {
		if ((_maxSize == 0) || (this.size() < _maxSize))
			this.addElement(item);
		else
			throw new SizeOverflowException();

		return item;
	}

	public synchronized T pop() {
		T element = this.elementAt(0);
		this.removeElementAt(0);
		return element;
	}

	public synchronized T peek() {
		return this.elementAt(0);
	}

	public boolean empty() {
		return (this.size() == 0);
	}

	public synchronized int search(Object o) {
		return this.indexOf(o);
	}
}

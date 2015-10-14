/*
 ERQueue.java - Camille Troillard - tuscland@mac.com
 */

package er.javamail;

import java.util.Vector;

public class ERQueue<T> extends Vector<T> {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	protected int _maxSize = 0;

	/**
	 * <span class="ja">
	 * キューの最大サイズを戻します。
	 * 
	 * @return キューの最大サイズ
	 * </span>
	 */
	public int maxSize() {
		return _maxSize;
	}

	/**
	 * <span class="ja">
	 * キューの最大サイズをセットします
	 * 
	 * @param size - キューの最大サイズ
	 * </span>
	 */
	public void setMaxSize(int size) {
		_maxSize = size;
	}

	public static class SizeOverflowException extends Exception {
		/**
		 * Do I need to update serialVersionUID?
		 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
		 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
		 */
		private static final long serialVersionUID = 1L;

		public SizeOverflowException() {
			super();
		}
	}

	public ERQueue() {
		this(0);
	}

	public ERQueue(int maxSize) {
		super();
		_maxSize = maxSize;
	}

	/**
	 * <span class="ja">
	 * 新しいアイテムをキューに登録します。
	 * 
	 * @param item - アイテム
	 * 
	 * @return 登録されたアイテム
	 * 
	 * @throws SizeOverflowException
	 * </span>
	 */
	public T push(T item) throws SizeOverflowException {
		if ((_maxSize == 0) || (size() < _maxSize)) {
			addElement(item);
		}
		else {
			throw new SizeOverflowException();
		}
		return item;
	}

	/**
	 * <span class="ja">
	 * 一番最初にキューに入ったアイテムをポップし、戻します。
	 * 
	 * @return 一番最初にキューに入ったアイテム
	 * </span>
	 */
	public synchronized T pop() {
		T element = elementAt(0);
		removeElementAt(0);
		return element;
	}

	/**
	 * <span class="ja">
	 * 一番最初にキューに入ったアイテムを消さすに戻します。
	 * 
	 * @return 一番最初にキューに入ったアイテム
	 * </span>
	 */
	public synchronized T peek() {
		return elementAt(0);
	}

	/**
	 * <span class="ja">
	 * キューサイズが 0 の場合
	 * 
	 * @return キューのサイズが 0 の場合には true が戻ります。
	 * </span>
	 */
	public boolean empty() {
		return size() == 0;
	}

	/**
	 * <span class="ja">
	 * キュー内のオブジェクトを探して、インデックスを戻します。
	 * 
	 * @param o - 検索するオブジェクト
	 * 
	 * @return インデックス番号
	 * </span>
	 */
	public synchronized int search(Object o) {
		return indexOf(o);
	}
}

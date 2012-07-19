/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.foundation;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSMutableSet;

/**
 * <span class="en">
 * Used as a generic way to retain a reference to an object so that it will not
 * be collected by the garbage collector. This class is most often used to
 * retain objects used to observe {link @NSNotification}s.
 * <br/> <br/> Note that the current implementation does not
 *                    implement reference counting so calling retain multiple
 *                    times on the same object does not have any effect after
 *                    the first call.
 * </span>
 * 
 * <span class="ja">
 * オブジェクトへのリファレンスを保持し、garbage collectorより回収されないようにする。<br>
 * <br>
 * このクラスを使う時は、{link @NSNotification}のオブジェクトを保持と観察する場合によく使用します。<br>
 * <br>
 * メモ：カレントの実装ではリファレンス・カウントしていません。
 * 一つのオブジェクトを複数回コールしても最初のコールと替わりません。
 * </span>
 */
// ENHANCEME: Should implement reference counting.
public class ERXRetainer {
	private static EOEditingContext ec;

	/** set used to retain references to objects */
	private static NSMutableSet _retainerSet = new NSMutableSet();

	/**
	 * <span class="en">
	 * Retains a reference to the object.
	 * 
	 * @param object
	 *            object to be retained.
	 * </span>
	 * 
	 * <span class="ja">
	 * オブジェクトへのリファレンスを保持する為に追加します
	 * 
	 * @param object - 保持するオブジェクト
	 * </span>
	 */
	public static void retain(Object object) {
		synchronized (_retainerSet) {
			/*if (object instanceof EOEnterpriseObject) {
				EOEnterpriseObject eo = (EOEnterpriseObject) object;
				if (ec == null) {
					ec = ERXEC.newEditingContext();

				}
				ec.lock();
				try {
					object = ERXEOControlUtilities.localInstanceOfObject(ec, eo);
				finally {
					ec.unlock();
				}

			}*/
			_retainerSet.addObject(object);

		}
	}

	/**
	 * <span class="en">
	 * Releases the reference to the object.
	 * 
	 * @param object
	 *            object to be released.
	 * </span>
	 * 
	 * <span class="ja">
	 * オブジェクトへのリファレンスの取り除きます
	 * 
	 * @param object - 取り除くオブジェクト
	 * </span>
	 */
	public static void release(Object object) {
		synchronized (_retainerSet) {
			_retainerSet.removeObject(object);
		}
	}

	/**
	 * <span class="en">
	 * Tests if the given object is being retained by the ERXRetainer class.
	 * 
	 * @param object
	 *            object to be tested.
	 *            
	 * @return returns if the given object is currently retained.
	 * </span>
	 * 
	 * <span class="ja">
	 * このクラスで保持されているかどうかをテストします
	 * 
	 * @param object - テストするオブジェクト
	 * @return オブジェクトが保持されていれば true を戻します
	 * </span>
	 */
	@SuppressWarnings("javadoc")
	public static boolean isObjectRetained(Object object) {
		synchronized (_retainerSet) {

			return _retainerSet.containsObject(object);
		}
	}
}

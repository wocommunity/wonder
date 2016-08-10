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
 * <div class="en">
 * Used as a generic way to retain a reference to an object so that it will not
 * be collected by the garbage collector. This class is most often used to
 * retain objects used to observe {link @NSNotification}s.
 * <p>
 * Note that the current implementation does not
 * implement reference counting so calling retain multiple
 * times on the same object does not have any effect after
 * the first call.
 * </div>
 * 
 * <div class="ja">
 * オブジェクトへのリファレンスを保持し、garbage collectorより回収されないようにする。
 * <p>
 * このクラスを使う時は、{link @NSNotification}のオブジェクトを保持と観察する場合によく使用します。
 * <p>
 * メモ：カレントの実装ではリファレンス・カウントしていません。
 * 一つのオブジェクトを複数回コールしても最初のコールと替わりません。
 * </div>
 */
// ENHANCEME: Should implement reference counting.
public class ERXRetainer {
	private static EOEditingContext ec;

	/** set used to retain references to objects */
	private static NSMutableSet _retainerSet = new NSMutableSet();

	/**
	 * <div class="en">
	 * Retains a reference to the object.
	 * </div>
	 * 
	 * <div class="ja">
	 * オブジェクトへのリファレンスを保持する為に追加します
	 * </div>
	 * 
	 * @param object <div class="en">object to be retained.</div>
	 *               <div class="ja">保持するオブジェクト</div>
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
	 * <div class="en">
	 * Releases the reference to the object.
	 * </div>
	 * 
	 * <div class="ja">
	 * オブジェクトへのリファレンスの取り除きます
	 * </div>
	 * 
	 * @param object <div class="en">object to be released.</div>
	 *               <div class="ja">取り除くオブジェクト</div>
	 */
	public static void release(Object object) {
		synchronized (_retainerSet) {
			_retainerSet.removeObject(object);
		}
	}

	/**
	 * <div class="en">
	 * Tests if the given object is being retained by the ERXRetainer class.
	 * </div>
	 * 
	 * <div class="ja">
	 * このクラスで保持されているかどうかをテストします
	 * </div>
	 * 
	 * @param object <div class="en">object to be tested.</div>
	 *               <div class="ja">テストするオブジェクト</div>
	 * @return <div class="en">returns if the given object is currently retained.</div>
	 *         <div class="ja">オブジェクトが保持されていれば true を戻します</div>
	 */
	public static boolean isObjectRetained(Object object) {
		synchronized (_retainerSet) {

			return _retainerSet.containsObject(object);
		}
	}
}

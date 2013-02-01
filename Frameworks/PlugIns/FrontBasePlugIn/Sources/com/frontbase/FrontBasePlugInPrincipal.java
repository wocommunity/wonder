package com.frontbase;

import com.webobjects.jdbcadaptor.JDBCPlugIn;
import com.webobjects.jdbcadaptor._FrontBasePlugIn;

/**
 * <span class="en">
 * 5.4 declares the same class name for the FrontBasePlugIn.  If your classpath isn't exactly right, they'll win,
 * so we pushed the real code into _FrontbasePlugIn and we set a custom principal class that registers the _variant
 * that is "guaranteed" to not have collisions as the plugin for the "frontbase" subprotocol.
 * </span>
 * 
 * <span class="ja">
 * 5.4 では FrontBasePlugIn で使用する同じ名称を定義しています。
 * クラス・パスが完全に正しくない場合 5.4 が有効です。
 * そのために本当のコードを _FrontbasePlugIn に記述し、カスタム主クラス（このクラス）を使って
 * "frontbase" サブプロトコルを重複しないように登録します。
 * </span>
 *  
 * @author mschrag
 */
public class FrontBasePlugInPrincipal {
	static {
		JDBCPlugIn.setPlugInNameForSubprotocol(_FrontBasePlugIn.class.getName(), "frontbase");
	}
}

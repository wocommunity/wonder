//
// ERXMutableUserInfoHolderInterface.java
// Project ERExtensionsJava
//
// Created by ak on Thu Jun 20 2002
//
package er.extensions.foundation;

import com.webobjects.foundation.NSMutableDictionary;
/** Interface to implement generic mutable containers */

public interface ERXMutableUserInfoHolderInterface {
    /** Returns the mutableUserInfo.*/
    public NSMutableDictionary mutableUserInfo();

    /** Set the mutableUserInfo */
    public void setMutableUserInfo(NSMutableDictionary userInfo);
}

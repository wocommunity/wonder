/*
 * WODictionaryRepetition.java
 * [JavaWOExtensions Project]
 *
 * © Copyright 2001 Apple Computer, Inc. All rights reserved.
 *
 * IMPORTANT:  This Apple software is supplied to you by Apple Computer,
 * Inc. (“Apple”) in consideration of your agreement to the following 
 * terms, and your use, installation, modification or redistribution of 
 * this Apple software constitutes acceptance of these terms.  If you do
 * not agree with these terms, please do not use, install, modify or
 * redistribute this Apple software.
 *
 * In consideration of your agreement to abide by the following terms, 
 * and subject to these terms, Apple grants you a personal, non-
 * exclusive license, under Apple’s copyrights in this original Apple 
 * software (the “Apple Software”), to use, reproduce, modify and 
 * redistribute the Apple Software, with or without modifications, in 
 * source and/or binary forms; provided that if you redistribute the  
 * Apple Software in its entirety and without modifications, you must 
 * retain this notice and the following text and disclaimers in all such
 * redistributions of the Apple Software.  Neither the name, trademarks,
 * service marks or logos of Apple Computer, Inc. may be used to endorse
 * or promote products derived from the Apple Software without specific
 * prior written permission from Apple.  Except as expressly stated in
 * this notice, no other rights or licenses, express or implied, are
 * granted by Apple herein, including but not limited to any patent
 * rights that may be infringed by your derivative works or by other
 * works in which the Apple Software may be incorporated.
 *
 * The Apple Software is provided by Apple on an "AS IS" basis.  APPLE 
 * MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION
 * THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE, REGARDING THE APPLE SOFTWARE OR ITS
 * USE AND OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS. 
 *
 * IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT,
 * INCIDENTAL OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE,
 * REPRODUCTION, MODIFICATION AND/OR DISTRIBUTION OF THE APPLE SOFTWARE,
 * HOWEVER CAUSED AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING
 * NEGLIGENCE), STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.webobjects.woextensions;

import java.util.*;
import com.webobjects.appserver.*;
import com.webobjects.foundation.*;

public class WODictionaryRepetition extends WOComponent {

    protected NSArray _keyList;
    protected NSDictionary _dictionary = null;

    public WODictionaryRepetition(WOContext aContext)  {
        super(aContext);
    }

    public boolean isStateless() {
        return true;
    }

    protected void _invalidateCaches() {
        // ** By setting these to null, we allow the dictionary to change after the action and before the next cycle of this component (if the component is on a page which is recycled)
        _dictionary = null;
        _keyList = null;
    }

    public void reset()  {
        _invalidateCaches();
    }

    public NSDictionary dictionary()  {
        if (_dictionary==null) {
            _dictionary = (NSDictionary)_WOJExtensionsUtil.valueForBindingOrNull("dictionary",this);
            _keyList = (_dictionary != null) ? _dictionary.allKeys() : null;
        }
        return _dictionary;
    }

    public NSArray keyList()  {
        if (_keyList==null) dictionary();
        return _keyList;
    }

    public Object currentKey() {
        // ** this is required by key/value coding.
        return "";
    }

    public void setCurrentKey(Object aKey)  {
        if ((dictionary()!=null) && (aKey!=null)) {
                Object anObject = dictionary().objectForKey(aKey);
                setValueForBinding(aKey, "key");
                setValueForBinding(anObject, "item");
        }
    }
}
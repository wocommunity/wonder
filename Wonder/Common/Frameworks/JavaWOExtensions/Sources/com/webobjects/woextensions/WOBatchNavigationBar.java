/*
 * WOBatchNavigationBar.java
 * [JavaWOExtensions Project]
 *
 * © Copyright 2001 Apple Computer, Inc. All rights reserved.
 *
 * IMPORTANT:  This Apple software is supplied to you by Apple Computer,
 * Inc. (ÒAppleÓ) in consideration of your agreement to the following 
 * terms, and your use, installation, modification or redistribution of 
 * this Apple software constitutes acceptance of these terms.  If you do
 * not agree with these terms, please do not use, install, modify or
 * redistribute this Apple software.
 *
 * In consideration of your agreement to abide by the following terms, 
 * and subject to these terms, Apple grants you a personal, non-
 * exclusive license, under AppleÕs copyrights in this original Apple 
 * software (the ÒApple SoftwareÓ), to use, reproduce, modify and 
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

public class WOBatchNavigationBar extends WOComponent {

    public WOBatchNavigationBar(WOContext aContext)  {
        super(aContext);
    }

    public boolean isStateless() {
        return true;
    }

    public boolean hasObjectName() {
        return hasBinding("objectName");
    }

    public boolean hasSortKeyList() {
        return hasBinding("sortKeyList");
    }

    public int numberOfObjectsPerBatch() {

        return ((WODisplayGroup)valueForBinding("displayGroup")).numberOfObjectsPerBatch();
    }

    public void setNumberOfObjectsPerBatch(Integer number) {
        int _number;
        
        //If a negative number is provided we default the number
        //of objects per batch to 0.
        _number = ((number != null) && (number.intValue() > 0)) ? number.intValue() : 0;
        
        ((WODisplayGroup)valueForBinding("displayGroup")).setNumberOfObjectsPerBatch(_number);      
    }

    public int batchIndex() {
        return ((WODisplayGroup)valueForBinding("displayGroup")).currentBatchIndex();
    }

    public void setBatchIndex(Integer index) {
        int _batchIndex;

        //Treat a null index as a 0 index. Negative numbers are handled
        //by the display group.
        _batchIndex = (index != null) ? index.intValue() : 0;

        ((WODisplayGroup)valueForBinding("displayGroup")).setCurrentBatchIndex(_batchIndex);       
    }
}
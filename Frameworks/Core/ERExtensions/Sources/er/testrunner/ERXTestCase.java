/* (ak) This class was adapted from WOUnitTest to accommodate the needs of ER classes.
It was moderately modified, but the copyright was left intact because I didn't know what else to do...
*/

/**
Copyright (c) 2001-2002, CodeFab, Inc.
 All rights reserved.

 Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the CodeFab, Inc. nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.


 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 **/

package er.testrunner;

import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.TestCase;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.eocontrol.EOValidation;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSValidation;

import er.extensions.eof.ERXEC;
/**
 * Basic test case class to do unit testing inside of WO. 
 * Provides an editingContext that is disposed on every setup/tearDown.
 */
public class ERXTestCase extends TestCase {
    private static final Logger log = LoggerFactory.getLogger(ERXTestCase.class);
    private EOEditingContext editingContext;
    private NSMutableArray persistentRootObjects;

    public ERXTestCase(String name){
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        persistentRootObjects = new NSMutableArray();
        editingContext = ERXEC.newEditingContext();
        editingContext().lock();
    }

    protected void registerPersistentRootObjectForDeletion(EOEnterpriseObject anEnterpriseObject) {
        persistentRootObjects.addObject(anEnterpriseObject);
    }

    protected void deletePersistentObjects() {
        boolean errorOccured = false;
        if(editingContext().hasChanges())
            editingContext().saveChanges();
        Enumeration persistentObjectEnum = persistentRootObjects.reverseObjectEnumerator();
        while (persistentObjectEnum.hasMoreElements()) {
            EOEnterpriseObject eo = (EOEnterpriseObject)persistentObjectEnum.nextElement();
            if( eoHasBeenSaved(eo) ) {
                try {
                    editingContext().deleteObject(eo);
                    editingContext().saveChanges();
                } catch (Throwable e) {
                    log.error("tearDown can't delete object.", e);
                    errorOccured = true;
                }
            }
        }
        if (errorOccured)
            throw new RuntimeException("deletePersistentObjects failed");
    }

    protected boolean eoHasBeenSaved(EOEnterpriseObject anEnterpriseObject) {
        EOGlobalID globalId = editingContext().globalIDForObject(anEnterpriseObject);
        return !(globalId == null || globalId.isTemporary());
    }

    @Override
    protected void tearDown() throws Exception {
        editingContext().revert();
        try {
            deletePersistentObjects();
        } finally {
            editingContext().unlock();
        }
        editingContext().dispose();
        editingContext = null;
        super.tearDown();
    }

    @Override
    public void runBare() throws Throwable {
        // We only want to see tearDown exceptions if runTest worked without exception
        setUp();
        try {
            runTest();
        } catch (Throwable e) {
            try {
                tearDown();
            } catch (Throwable e2) {
                log.error("WOUT  tearDown failure.", e2);
            }
            throw e;
        }
        tearDown();
    }

    protected void saveChanges(boolean assumeSuccess) {
        Exception exception= null;
        try {
            editingContext().saveChanges();
        } catch(Exception e) {
            exception = e;
            if (assumeSuccess) {
                e.printStackTrace();
                editingContext().revert();
            }
        }
        if (assumeSuccess)
            assertNull(exception);
        else
            assertNotNull(exception);
    }

    protected EOEditingContext editingContext() {
        return editingContext;
    }

    public static void assertValidates(boolean expectsSuccess, EOValidation validationObject) {
        try {
            validationObject.validateForSave();
            if (!expectsSuccess)
                fail("validation succeeded unexpectedly for: " + validationObject);
        } catch (NSValidation.ValidationException exception) {
            if (expectsSuccess)
                fail( "Validation unexpectedly failed: " + exception.getMessage());
        }
    }

}

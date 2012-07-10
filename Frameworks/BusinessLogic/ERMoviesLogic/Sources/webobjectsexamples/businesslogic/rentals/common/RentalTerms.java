/*
 * RentalTerms.java [JavaBusinessLogic Project] © Copyright 2005 Apple Computer, Inc. All rights reserved. IMPORTANT: This Apple software is supplied to you by Apple Computer, Inc. ("Apple") in consideration of your agreement to the following terms, and your use, installation, modification or
 * redistribution of this Apple software constitutes acceptance of these terms. If you do not agree with these terms, please do not use, install, modify or redistribute this Apple software. In consideration of your agreement to abide by the following terms, and subject to these terms, Apple grants
 * you a personal, non-exclusive license, under Apple's copyrights in this original Apple software (the "Apple Software"), to use, reproduce, modify and redistribute the Apple Software, with or without modifications, in source and/or binary forms; provided that if you redistribute the Apple Software
 * in its entirety and without modifications, you must retain this notice and the following text and disclaimers in all such redistributions of the Apple Software. Neither the name, trademarks, service marks or logos of Apple Computer, Inc. may be used to endorse or promote products derived from the
 * Apple Software without specific prior written permission from Apple. Except as expressly stated in this notice, no other rights or licenses, express or implied, are granted by Apple herein, including but not limited to any patent rights that may be infringed by your derivative works or by other
 * works in which the Apple Software may be incorporated. The Apple Software is provided by Apple on an "AS IS" basis. APPLE MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE,
 * REGARDING THE APPLE SOFTWARE OR ITS USE AND OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS. IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE, REPRODUCTION, MODIFICATION AND/OR DISTRIBUTION OF THE APPLE SOFTWARE, HOWEVER CAUSED AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING NEGLIGENCE), STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package webobjectsexamples.businesslogic.rentals.common;

import java.math.BigDecimal;

import com.webobjects.eocontrol.EOCustomObject;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;

// RentalTerms inherits from EOCustomObject as an example of not inheriting from EOGenericRecord
public class RentalTerms extends EOCustomObject {
	private static final long		serialVersionUID			= 4273410078165804242L;

	public static final String		RentalTermsEntityName		= "RentalTerms";

	public static final String		RentalTermsIDKey			= "rentalTermsID";

	private transient Number		_checkOutLength;

	private transient BigDecimal	_cost;

	private transient BigDecimal	_depositAmount;

	private transient String		_name;

	private static EOGlobalID		_defaultRentalTermsGlobalID	= null;

	private static EOGlobalID _globalIDForPrimaryKey(int primaryKey, EOEditingContext editingContext) {
		EOFetchSpecification fetchSpecification = new EOFetchSpecification(RentalTermsEntityName,
				new EOKeyValueQualifier(RentalTermsIDKey, EOQualifier.QualifierOperatorEqual, Integer.valueOf(primaryKey)), null);
		NSArray objects = editingContext.objectsWithFetchSpecification(fetchSpecification);
		return (objects.count() > 0) ? editingContext.globalIDForObject((EOEnterpriseObject) (objects.objectAtIndex(0))) : null;
	}

	public static RentalTerms defaultRentalTerms(EOEditingContext editingContext) {
		if (_defaultRentalTermsGlobalID == null) {
			// default rental terms are the object with primary key = 1
			_defaultRentalTermsGlobalID = _globalIDForPrimaryKey(1, editingContext);
		}
		return (_defaultRentalTermsGlobalID != null) ? (RentalTerms) (editingContext.faultForGlobalID(_defaultRentalTermsGlobalID, editingContext)) : null;
	}

	public RentalTerms() {
		super();
	}

	public Number checkOutLength() {
		willRead();
		return _checkOutLength;
	}

	public void setCheckOutLength(Number value) {
		willChange();
		_checkOutLength = value;
	}

	public BigDecimal cost() {
		willRead();
		return _cost;
	}

	public void setCost(BigDecimal value) {
		willChange();
		_cost = value;
	}

	public BigDecimal depositAmount() {
		willRead();
		return _depositAmount;
	}

	public void setDepositAmount(BigDecimal value) {
		willChange();
		_depositAmount = value;
	}

	public String name() {
		willRead();
		return _name;
	}

	public void setName(String value) {
		willChange();
		_name = value;
	}
}

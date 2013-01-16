/*
 * Customer.java [JavaBusinessLogic Project] © Copyright 2005 Apple Computer, Inc. All rights reserved. IMPORTANT: This Apple software is supplied to you by Apple Computer, Inc. ("Apple") in consideration of your agreement to the following terms, and your use, installation, modification or
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

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOGenericRecord;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation.NSValidation;

public class Customer extends EOGenericRecord {
	private static final long		serialVersionUID		= -1996155567059278076L;

	public static final String		CityKey					= "city";

	public static final String		CreditCardKey			= "creditCard";

	public static final String		FirstNameKey			= "firstName";

	public static final String		LastNameKey				= "lastName";

	public static final String		MemberSinceKey			= "memberSince";

	public static final String		PhoneKey				= "phone";

	public static final String		RentalsKey				= "rentals";

	public static final String		StateKey				= "state";

	public static final String		StreetAddressKey		= "streetAddress";

	public static final String		ZipKey					= "zip";

	private static final String		_DepositAmountKeyPath	= "unit.video.rentalTerms.depositAmount";

	public static final BigDecimal	DefaultCostRestriction	= new BigDecimal(50);

	public Customer() {
		super();
	}

	@Override
	public void awakeFromInsertion(EOEditingContext editingContext) {
		super.awakeFromInsertion(editingContext);
		if (memberSince() == null) {
			setMemberSince(new NSTimestamp());
		}
	}

	@Override
	public void validateForSave() throws NSValidation.ValidationException {
		// calculate deposit
		BigDecimal deposit = new BigDecimal(0);
		NSArray outRentals = outRentals();
		int count = outRentals.count();
		for (int i = 0; i < count; i++) {
			BigDecimal amount = (BigDecimal) (((EOEnterpriseObject) (outRentals.objectAtIndex(i))).valueForKeyPath(_DepositAmountKeyPath));
			if (amount != null) {
				deposit = deposit.add(amount);
			}
		}

		// check whether deposit is too high
		BigDecimal maxDeposit = costRestriction();
		if (deposit.compareTo(maxDeposit) > 0) {
			throw new NSValidation.ValidationException("The total value of the rented videos (" + deposit + ") exceed limitations (" + maxDeposit + ") of " + fullName() + "!");
		}
		super.validateForSave();
	}

	public CreditCard creditCard() {
		return (CreditCard) (storedValueForKey(CreditCardKey));
	}

	public String firstName() {
		return (String) (storedValueForKey(FirstNameKey));
	}

	public void setFirstName(String value) {
		takeStoredValueForKey(value, FirstNameKey);
	}

	public String lastName() {
		return (String) (storedValueForKey(LastNameKey));
	}

	public void setLastName(String value) {
		takeStoredValueForKey(value, LastNameKey);
	}

	public NSTimestamp memberSince() {
		return (NSTimestamp) (storedValueForKey(MemberSinceKey));
	}

	public void setMemberSince(NSTimestamp value) {
		takeStoredValueForKey(value, MemberSinceKey);
	}

	public NSArray rentals() {
		return (NSArray) (storedValueForKey(RentalsKey));
	}

	public BigDecimal costRestriction() {
		CreditCard creditCard = creditCard();
		return (creditCard != null) ? creditCard.limit() : DefaultCostRestriction;
	}

	public String fullName() {
		StringBuilder buffer = new StringBuilder();
		buffer.append(firstName());
		buffer.append(' ');
		buffer.append(lastName());
		return buffer.toString();
	}

	@SuppressWarnings("unchecked")
	public NSArray allFees() {
		NSMutableArray<Object> allFees = new NSMutableArray<Object>();
		NSArray rentals = rentals();
		if (rentals != null) {
			int count = rentals.count();
			for (int i = 0; i < count; i++) {
				NSArray fees = ((Rental) (rentals.objectAtIndex(i))).fees();
				if (fees != null) {
					allFees.addObjectsFromArray(fees);
				}
			}
		}
		return allFees;
	}

	public Number numberOfAllFees() {
		return Integer.valueOf(allFees().count());
	}

	public NSArray unpaidFees() {
		EOQualifier qualifier = new EOKeyValueQualifier(Fee.DatePaidKey, EOQualifier.QualifierOperatorEqual, NSKeyValueCoding.NullValue);
		return EOQualifier.filteredArrayWithQualifier(allFees(), qualifier);
	}

	public Number numberOfUnpaidFees() {
		return Integer.valueOf(unpaidFees().count());
	}

	public boolean hasUnpaidFees() {
		return (unpaidFees().count() > 0);
	}

	public NSArray allRentals() {
		NSArray rentals = rentals();
		return (rentals != null) ? rentals : new NSArray();
	}

	public Number numberOfAllRentals() {
		return Integer.valueOf(allRentals().count());
	}

	public NSArray outRentals() {
		EOQualifier qualifier = new EOKeyValueQualifier(Rental.IsOutKey, EOQualifier.QualifierOperatorEqual, Boolean.TRUE);
		return EOQualifier.filteredArrayWithQualifier(allRentals(), qualifier);
	}

	public Number numberOfOutRentals() {
		return Integer.valueOf(outRentals().count());
	}

	public boolean hasOutRentals() {
		return (outRentals().count() > 0);
	}

	public NSArray overdueRentals() {
		EOQualifier qualifier = new EOKeyValueQualifier(Rental.IsOverdueKey, EOQualifier.QualifierOperatorEqual, Boolean.TRUE);
		return EOQualifier.filteredArrayWithQualifier(allRentals(), qualifier);
	}

	public Number numberOfOverdueRentals() {
		return Integer.valueOf(overdueRentals().count());
	}

	public boolean hasOverdueRentals() {
		return (overdueRentals().count() > 0);
	}

	public boolean isAllowedToRent() {
		return (!(hasOverdueRentals()));
	}

	public void rentUnit(Unit unit) {
		// create new objects and insert them into editing context
		EOEditingContext editingContext = editingContext();
		// generate fee and rental and insert it into editing context
		Fee fee = new Fee();
		Rental rental = new Rental();
		editingContext.insertObject(fee);
		editingContext.insertObject(rental);
		// manipulate relationships after inserting objects
		rental.addObjectToBothSidesOfRelationshipWithKey(unit, Rental.UnitKey);
		rental.addObjectToBothSidesOfRelationshipWithKey(fee, Rental.FeesKey);
		addObjectToBothSidesOfRelationshipWithKey(rental, RentalsKey);
	}
}

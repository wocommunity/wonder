/*
 * Rental.java [JavaBusinessLogic Project] © Copyright 2005 Apple Computer, Inc. All rights reserved. IMPORTANT: This Apple software is supplied to you by Apple Computer, Inc. ("Apple") in consideration of your agreement to the following terms, and your use, installation, modification or
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

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation.NSValidation;

public class Rental extends _Rental {

    private static final long serialVersionUID = 1L;

    public static final String  IsOutKey                = "isOut";

    public static final String  IsOverdueKey            = "isOverdue";

    private static final String	_CheckOutLengthKeyPath	= "unit.video.rentalTerms.checkOutLength";

	public Rental() {
		super();
	}

	@Override
	public void awakeFromInsertion(EOEditingContext editingContext) {
		super.awakeFromInsertion(editingContext);
		if (dateOut() == null) {
			setDateOut(new NSTimestamp());
		}
	}

	@Override
	public void validateForSave() throws NSValidation.ValidationException {
		Customer customer = customer();
		if (customer != null) {
			customer.validateForSave();
		}
		super.validateForSave();
	}

	public NSTimestamp dateDue() {
		NSTimestamp dateOut = dateOut();
		if (dateOut != null) {
			Number checkOutLength = (Number) (valueForKeyPath(_CheckOutLengthKeyPath));
			if (checkOutLength != null) {
				int days = checkOutLength.intValue();
				return new NSTimestamp(dateOut.getTime() + days * (1000 * 60 * 60 * 24));
			}
			return dateOut;
		}
		return null;
	}

	public boolean isOut() {
		return (dateReturned() == null);
	}

	public boolean isReturned() {
		return (!(isOut()));
	}

	public boolean isOverdue() {
		return (isReturned()) ? false : (dateDue().before(new NSTimestamp()));
	}

	public String isOverdueString() {
		return (isOverdue()) ? "Yes" : "No";
	}

	public void feePaid() {
		if (dateReturned() != null) {
			NSArray<Fee> fees = fees();
			if (fees != null) {
				int count = fees.count();
				for (int i = 0; i < count; i++) {
					if (!(((Fee) (fees.objectAtIndex(i))).isPaid())) {
						return;
					}
				}
			}

			// At this point some code could be inserted to handle the situation where all fees are paid (for example the rental could delete itself). Here we ignore the situation so that we collect the full history of the customer's transactions.
		}
	}

	public void returnVideo() {
		setDateReturned(new NSTimestamp());
		if (!(isOverdue())) {
			feePaid();
		} else {
			EOEditingContext editingContext = editingContext();
			// generate late fee and insert it into editing context
			Fee fee = new Fee(dateDue(), editingContext);
			editingContext.insertObject(fee);
			// manipulate relationship after inserting object
			addObjectToBothSidesOfRelationshipWithKey(fee, FEES_KEY);
		}
	}
}

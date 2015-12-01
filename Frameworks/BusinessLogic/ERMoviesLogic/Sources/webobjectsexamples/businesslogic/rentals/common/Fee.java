/*
 * Fee.java [JavaBusinessLogic Project] © Copyright 2005 Apple Computer, Inc. All rights reserved. IMPORTANT: This Apple software is supplied to you by Apple Computer, Inc. ("Apple") in consideration of your agreement to the following terms, and your use, installation, modification or redistribution
 * of this Apple software constitutes acceptance of these terms. If you do not agree with these terms, please do not use, install, modify or redistribute this Apple software. In consideration of your agreement to abide by the following terms, and subject to these terms, Apple grants you a personal,
 * non-exclusive license, under Apple's copyrights in this original Apple software (the "Apple Software"), to use, reproduce, modify and redistribute the Apple Software, with or without modifications, in source and/or binary forms; provided that if you redistribute the Apple Software in its entirety
 * and without modifications, you must retain this notice and the following text and disclaimers in all such redistributions of the Apple Software. Neither the name, trademarks, service marks or logos of Apple Computer, Inc. may be used to endorse or promote products derived from the Apple Software
 * without specific prior written permission from Apple. Except as expressly stated in this notice, no other rights or licenses, express or implied, are granted by Apple herein, including but not limited to any patent rights that may be infringed by your derivative works or by other works in which
 * the Apple Software may be incorporated. The Apple Software is provided by Apple on an "AS IS" basis. APPLE MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, REGARDING THE APPLE
 * SOFTWARE OR ITS USE AND OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS. IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE, REPRODUCTION, MODIFICATION AND/OR DISTRIBUTION OF THE APPLE SOFTWARE, HOWEVER CAUSED AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING NEGLIGENCE), STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package webobjectsexamples.businesslogic.rentals.common;

import java.math.BigDecimal;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation.NSValidation;

public class Fee extends _Fee {
	
    private static final long serialVersionUID = 1L;

    private static final String _CostKeyPath        = "rental.unit.video.rentalTerms.cost";
    
    public static final int		LateFeeAmountPerDay	= 3;

	public Fee() {
		super();
	}

	// constructor for late fees
	public Fee(NSTimestamp date, EOEditingContext editingContext) {
		this();
		setFeeType(FeeType.lateFeeType(editingContext));
		// calculate time since date to current date in milliseconds
		long time = ((new NSTimestamp()).getTime()) - date.getTime();
		int elapsedDays = (int) (time / (1000 * 60 * 60 * 24));
		setAmount(new BigDecimal((elapsedDays + 1) * LateFeeAmountPerDay));
	}

	@Override
	public void init(EOEditingContext editingContext) {
		super.init(editingContext);
		if (amount() == null) {
			setAmount((BigDecimal) (valueForKeyPath(_CostKeyPath)));
		}
		if (feeType() == null) {
			setFeeType(FeeType.defaultFeeType(editingContext));
		}
	}

	@Override
	public void validateForDelete() throws NSValidation.ValidationException {
		if (!(isPaid())) {
			throw new NSValidation.ValidationException("You can't remove an unpaid fee!");
		}
		super.validateForDelete();
	}

	public void setRental(Rental value) {
	    super.setRental(value);
		if (amount() == null) {
			setAmount((BigDecimal) (valueForKeyPath(_CostKeyPath)));
		}
	}

	public boolean isPaid() {
		return (datePaid() != null);
	}

	public void pay() {
		setDatePaid(new NSTimestamp());
		// notify rental that fee is payed
		Rental rental = rental();
		if (rental != null) {
			rental.feePaid();
		}
	}
}

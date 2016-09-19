/*
 * Talent.java [JavaBusinessLogic Project] © Copyright 2005 Apple Computer, Inc. All rights reserved. IMPORTANT: This Apple software is supplied to you by Apple Computer, Inc. ("Apple") in consideration of your agreement to the following terms, and your use, installation, modification or
 * redistribution of this Apple software constitutes acceptance of these terms. If you do not agree with these terms, please do not use, install, modify or redistribute this Apple software. In consideration of your agreement to abide by the following terms, and subject to these terms, Apple grants
 * you a personal, non-exclusive license, under Apple's copyrights in this original Apple software (the "Apple Software"), to use, reproduce, modify and redistribute the Apple Software, with or without modifications, in source and/or binary forms; provided that if you redistribute the Apple Software
 * in its entirety and without modifications, you must retain this notice and the following text and disclaimers in all such redistributions of the Apple Software. Neither the name, trademarks, service marks or logos of Apple Computer, Inc. may be used to endorse or promote products derived from the
 * Apple Software without specific prior written permission from Apple. Except as expressly stated in this notice, no other rights or licenses, express or implied, are granted by Apple herein, including but not limited to any patent rights that may be infringed by your derivative works or by other
 * works in which the Apple Software may be incorporated. The Apple Software is provided by Apple on an "AS IS" basis. APPLE MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE,
 * REGARDING THE APPLE SOFTWARE OR ITS USE AND OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS. IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE, REPRODUCTION, MODIFICATION AND/OR DISTRIBUTION OF THE APPLE SOFTWARE, HOWEVER CAUSED AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING NEGLIGENCE), STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package webobjectsexamples.businesslogic.movies.common;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;

public class Talent extends _Talent {
	private static final long	serialVersionUID	= -5942077710497738108L;

	public static final String	FirstNameKey		= "firstName";

	public static final String	LastNameKey			= "lastName";

	public static final String	MoviesDirectedKey	= "moviesDirected";

	public static final String	PhotoKey			= "photo";

	public static final String	RolesKey			= "roles";

	public Talent() {
		super();
	}

	@Override
	public String firstName() {
		return (String) (storedValueForKey(FirstNameKey));
	}

	@Override
	public String lastName() {
		return (String) (storedValueForKey(LastNameKey));
	}

	@Override
	public NSArray roles() {
		return (NSArray) (storedValueForKey(RolesKey));
	}

	public String fullName() {
		StringBuilder buffer = new StringBuilder();
		buffer.append(firstName());
		buffer.append(' ');
		buffer.append(lastName());
		return buffer.toString();
	}

	public NSArray moviesStarredIn() {
		NSMutableArray<Object> moviesStarredIn = new NSMutableArray<>();
		NSArray movies = (NSArray) (roles().valueForKey("movie"));
		if (movies != null) {
			int count = movies.count();
			for (int i = 0; i < count; i++) {
				Object movie = movies.objectAtIndex(i);
				if ((movie != NSKeyValueCoding.NullValue) && (!(moviesStarredIn.containsObject(movie)))) {
					moviesStarredIn.addObject(movie);
				}
			}
		}
		return moviesStarredIn;
	}
	
	// used by ERDivaLookExample
	@SuppressWarnings("unchecked")
	public boolean isDirector() {
		NSArray moviesDirected = (NSArray) valueForKey("moviesDirected");
		return (moviesDirected != null && moviesDirected.count() > 0);
	}

}

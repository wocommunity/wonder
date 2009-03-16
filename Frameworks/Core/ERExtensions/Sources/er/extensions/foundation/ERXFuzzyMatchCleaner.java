/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr 
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.foundation;


/**
 * Interface used in conjunction with the fuzzy matching
 * code found in {@link ERXUtilities}. The notion of a cleaner
 * provides a plugin way to prepare given strings for fuzzy matching
 * for instance one type of cleaner might remove the ending strings
 * 'Inc.' when matching against company names.
 */
// FIXME: Should move all of fuzzy matching stuff in here and then add a static inner inerface
public interface ERXFuzzyMatchCleaner {

    /**
     * Method used to clean a string before matching
     * it in a fuzzy manner. Cleaners can perform any
     * cleaning they want on a string before it is matched.
     * @param s string to be cleaned
     * @return cleaned string.
     */
    public String cleanStringForFuzzyMatching(String s);
}

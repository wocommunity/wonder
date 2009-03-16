/*
 * Simple Java Dict Client (RFC2229)
 *
 * $Id$
 *
 * Copyright Metaparadigm Pte. Ltd. 2004.
 * Michael Clark <michael@metaparadigm.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public (LGPL)
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details: http://www.gnu.org/
 *
 */

package com.metaparadigm.dict;

public class Strategy
{
    private String strategy;
    private String description;

    public String getStrategy() { return strategy; }
    public String getDescription() { return description; }

    public Strategy(String strategy, String description)
    {
	this.strategy = strategy;
	this.description = description;
    }

    public String toString()
    {
	return "strategy: " + strategy + " \"" + description + "\"";
    }

}

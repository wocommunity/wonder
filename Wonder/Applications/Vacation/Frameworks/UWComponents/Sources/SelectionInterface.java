//
//  SelectionInterface.java
//  UWComponents
//
//  Created by Ash on Thu May 23 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

package com.uw.shared;

public interface SelectionInterface {
    public int selected = 0;
    public int selected();
    public void setSelected(int newSelectionValue);
}
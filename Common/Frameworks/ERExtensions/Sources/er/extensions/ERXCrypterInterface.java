//
//  ERXCrypterInterface.java
//  ERExtensions
//
//  Created by Bruno Posokhow on Tue Jan 14 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//
package er.extensions;

public interface ERXCrypterInterface {

    public String encrypt(String clearText);
    public String decrypt(String cryptedText);

}

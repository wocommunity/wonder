//
// ERDQueryPageInterface.java
// Project ERDirectToWeb
//
// Created by bposokho on Fri Oct 18 2002
//
package er.directtoweb;

import com.webobjects.directtoweb.*;

public interface ERDQueryPageInterface extends QueryPageInterface{

    public void setCancelDelegate(NextPageDelegate cancelDelegate);
    
}

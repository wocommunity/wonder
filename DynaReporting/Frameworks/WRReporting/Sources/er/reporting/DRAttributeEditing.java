package er.reporting;

import er.grouping.*;
import java.lang.*;
import java.util.*;
import java.io.*;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;

/* DREditing.h created by Administrator on Mon 30-Nov-1998 */
public interface DRAttributeEditing  {

    public void moveSubAttributeUp(DRAttribute subAtt, boolean up);

    public void deleteSubAttribute(DRAttribute subAtt);

    public void toggleGroupInList(DRAttribute att);

}
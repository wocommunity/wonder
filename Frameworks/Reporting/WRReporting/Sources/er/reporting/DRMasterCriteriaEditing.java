package er.reporting;

import er.grouping.*;
import java.lang.*;
import java.util.*;
import java.io.*;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;

public interface DRMasterCriteriaEditing  {

    public void replaceMCWith(DRMasterCriteria oldMC, NSArray smcList);

}
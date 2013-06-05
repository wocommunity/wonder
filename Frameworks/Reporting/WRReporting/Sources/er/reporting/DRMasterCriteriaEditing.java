package er.reporting;

import com.webobjects.foundation.NSArray;

import er.grouping.DRMasterCriteria;

public interface DRMasterCriteriaEditing  {

    public void replaceMCWith(DRMasterCriteria oldMC, NSArray smcList);

}
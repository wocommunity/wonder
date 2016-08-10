package er.reporting;

import er.grouping.DRAttribute;

public interface DRAttributeEditing  {

    public void moveSubAttributeUp(DRAttribute subAtt, boolean up);

    public void deleteSubAttribute(DRAttribute subAtt);

    public void toggleGroupInList(DRAttribute att);

}
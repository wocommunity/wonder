package er.prototypes;

/**
HACKALERT
simple class that ensures correct framework reference with PBX
*/

public class ERPPrincipalClass {

    static {
        //this is not log.info... because maybe this static
        //part would get loaded before some other initializations
        System.out.println("ERPrototypes loaded");
    }
}

Wonderized version of the OpenBasePlugIn (also includes the specialized OpenBase PK generation).
This was given to me on 5/21/2014. On 5/28/2014 received permission from Scott Keith to include
in Project wonder.

Tim Worman

These are the commit logs for everything that was done prior to adding to the Wonder repository.

commit 9ab5549682be3ef1bc442200d87f6dab87d42cb7
Author: T Worman <worman@gseis.ucla.edu>
Date:   Wed May 28 09:04:14 2014 -0700

    Added EROpenBasePlugIn, implemented generation gap inheritance from
    _OpenBasePlugIn which is the modified source from openbase/scott keith

commit 16b0759ced3bd3dd3d7675cf927bbeb45dcbe235
Author: T Worman <worman@gseis.ucla.edu>
Date:   Wed May 28 08:49:48 2014 -0700

    move qualifiers from default package to new package
    com.openbase.webobjects.qualifiers

commit dc1d098291d62c8dae70a26447b8658fe2e6858d
Author: T Worman <worman@gseis.ucla.edu>
Date:   Wed May 21 15:44:29 2014 -0700

    OpenBaseSynchronizationFactory now extends
    EOSchemaSynchronizationFactory instead of EOSynchronizationFactory

commit ab3a96bed5cc9f62bab4a14e24fb4009a0dcf9f3
Author: T Worman <worman@gseis.ucla.edu>
Date:   Wed May 21 12:48:08 2014 -0700

    couple more inserations of EOSchemaGenerationOptions where 'options' are
    passed in - match the super implementation

commit eed37b1a85e765db70af3444635221b968aaf838
Author: T Worman <worman@gseis.ucla.edu>
Date:   Wed May 21 12:43:30 2014 -0700

    OpenBaseSynchronizationFactory.statementsToDropPrimaryKeyConstraintsOnEntityGroups
    - altered _nameInObjectStoreForEntityGroupWithChangeDictionary to pass
    it a table name which must be a change in the API.

commit eeb2b28d7ff55b85fb4825d3645334f97736ffd7
Author: T Worman <worman@gseis.ucla.edu>
Date:   Wed May 21 12:37:27 2014 -0700

    DOESN'T BUILD! altered methods accepting NSDictionary of changes/options
    to instead expect
    EOSchemaSynchronizationModelChanges/EOSchemaGenerationOptions objects
    instead.

commit 693b78d7048cee4006bdfdd6c29a706e50f94864
Author: T Worman <worman@gseis.ucla.edu>
Date:   Wed May 21 11:43:23 2014 -0700

    updated
    OpenBasePlugin.OpenBaseSynchronizationFactory.objectStoreChangesFromAttributeToAttribute
    to reflect newer underlying API which uses
    EOSchemaSynchronizationColumnChanges

commit 906afda877c04ee1716dfc9691335fc6af402889
Author: T Worman <worman@gseis.ucla.edu>
Date:   Wed May 21 10:46:19 2014 -0700

    minor syntax change in OpenBasePKPlugin.newPrimaryKeys to fix build
    error

commit 4c4cbe7d47f70402ae7b12d5b9464c072a61a21c
Author: T Worman <worman@gseis.ucla.edu>
Date:   Wed May 21 10:44:34 2014 -0700

    DOESN'T BUILD! - added all required frameworks to the build path

commit 0c817b4700e3ba294e986c5e3ddd2bac7defcf31
Author: T Worman <worman@gseis.ucla.edu>
Date:   Wed May 21 10:39:56 2014 -0700

    import to eclipse which deleted stuff in dist

commit 1e32f35a55415600eeaa705029a8ff02f03aa0dc
Author: T Worman <worman@gseis.ucla.edu>
Date:   Wed May 21 10:37:31 2014 -0700

    initial commit of OpenBaseWOPKPlugIn

//
//  RMModelGroupEditor.h
//  RuleModeler
//
//  Created by Dave Lopper on 8/14/06.
//  Copyright 2006 __MyCompanyName__. All rights reserved.
//

#import "RMModelEditor.h"


@interface RMModelGroupEditor : RMModelEditor {
    IBOutlet NSDrawer           *modelListDrawer;
    IBOutlet NSTableView        *modelTableView;
    IBOutlet NSArrayController  *modelController;
}

- (IBAction)addModels:(id)sender;
- (IBAction)showModel:(id)sender;
- (IBAction)removeModels:(id)sender;
- (IBAction)showRuleModel:(id)sender;

@end

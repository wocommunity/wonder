//
//  PBXTextViewPoser.h
//  CMMJavaMenu
//
//  Created by Anjo Krank on Mon Mar 25 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <AppKit/AppKit.h>

#define PATCH_SELECTOR @selector(AS_handleMenu:)

@interface PBXTextView:NSTextView
{
    float _highlightStartY;
    float _highlightHeight;
    char _observingHighlightColorChangedNotification;
    NSColor *_highlightBaseColor;
    unsigned int _modifierFlagsAtLastSingleMouseDown;
    float _pageGuideWidth;
    NSColor *_pageGuideOutOfBoundsColor;
    void *__reserved[3];
}
@end

@interface PBXTextViewPoser : PBXTextView {

}

@end

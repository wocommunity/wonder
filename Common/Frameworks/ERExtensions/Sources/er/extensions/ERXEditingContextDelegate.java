/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

/**
 * This delegate does nothing. It is used to
 * make the environment happy in that some
 * parts of the system require that a delegate
 * be set on the editing context. In those cases
 * use an instance of this delegate. All of the
 * other delegates subclass this delegate. The main
 * delegate that is used is {@link ERXDefaultEditingContextDelegate}.
 */
public class ERXEditingContextDelegate extends Object  {
}


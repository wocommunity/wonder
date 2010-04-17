/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.delegates;

/**
 * Used in conjunction with ERDBranchDelegateInterface.
 * Templates that want to be able to use branch delegates
 * need to implement this interface so that the delegate
 * can know which branch was choosen.
 */
public interface ERDBranchInterface {
    
    /**
     * Name of the branch choosen by the
     * user.
     * @return choosen branch name.
     */
    public String branchName();
}

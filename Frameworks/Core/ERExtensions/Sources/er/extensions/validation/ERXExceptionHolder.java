/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.validation;

/**
 * The ExceptionHolder interface should be implemented by components
 * that will collect validation exceptions. In the DirectToWeb world
 * templates collect all of the validation exceptions that occur within
 * a given request-response loop. However at times a way is needed for
 * a nested component to wipe out all of the validation exceptions that
 * have been collected.
 */
public interface ERXExceptionHolder {

    /**
     * Clears all of the collected validation exceptions.
     */
    public void clearValidationFailed();
}

/**
 * Created by IntelliJ IDEA.
 * User: dnoakes
 * Date: Dec 15, 2002
 * Time: 3:30:59 PM
 * To change this template use Options | File Templates.
 */
package com.drew.lang;

import java.io.IOException;
import java.io.OutputStream;

public class NullOutputStream extends OutputStream
{
    public NullOutputStream()
    {
        super();
    }

    @Override
    public void write(int b) throws IOException
    {
        // do nothing
    }
}

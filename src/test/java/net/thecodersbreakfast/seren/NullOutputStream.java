package net.thecodersbreakfast.seren;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author olivier
 */
public class NullOutputStream extends OutputStream {
    public void write(int b) throws IOException {
        // nothing
    }
}

package org.itest.impl.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class IoUtils {
    public static byte[] readBytes(InputStream in, byte[] buffer) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int read;
        while (0 <= (read = in.read(buffer))) {
            out.write(buffer, 0, read);
        }
        return out.toByteArray();
    }
}

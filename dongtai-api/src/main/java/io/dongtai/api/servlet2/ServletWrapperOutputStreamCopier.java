package io.dongtai.api.servlet2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

/**
 * <p>
 *
 * @author zhaoyb1990
 */
public class ServletWrapperOutputStreamCopier extends ServletOutputStream {

    private final OutputStream out;
    private final ByteArrayOutputStream copier;

    ServletWrapperOutputStreamCopier(OutputStream out) {
        this.out = out;
        this.copier = new ByteArrayOutputStream();
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
        copier.write(b);
    }

    byte[] getCopy() {
        return copier.toByteArray();
    }


    /**
     * This method can be used to determine if data can be written without blocking.
     *
     * @return <code>true</code> if a write to this <code>ServletOutputStream</code>
     * will succeed, otherwise returns <code>false</code>.
     * @since Servlet 3.1
     */
    @Override
    public boolean isReady() {
        return false;
    }


    /**
     * Instructs the <code>ServletOutputStream</code> to invoke the provided
     * {@link WriteListener} when it is possible to write
     *
     *
     * @param writeListener the {@link WriteListener} that should be notified
     *  when it's possible to write
     *
     * @exception IllegalStateException if one of the following conditions is true
     * <ul>
     * <li>the associated request is neither upgraded nor the async started
     * <li>setWriteListener is called more than once within the scope of the same request.
     * </ul>
     *
     * @throws NullPointerException if writeListener is null
     *
     * @since Servlet 3.1
     */
    @Override
    public void setWriteListener(WriteListener writeListener) {

    }
}

package nl.kabisa.spring.boot.starter.service.logging;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.util.StreamUtils;
import org.springframework.web.util.WebUtils;

import java.io.*;

public class CachedContentRequestWrapper extends HttpServletRequestWrapper {

    private final byte[] cachedContent;

    public CachedContentRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        InputStream requestInputStream = request.getInputStream();
        this.cachedContent = StreamUtils.copyToByteArray(requestInputStream);
    }

    public byte[] getContentAsByteArray() {
        return cachedContent;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return new CachedContentServletInputStream(this.cachedContent);
    }

    @Override
    public String getCharacterEncoding() {
        String enc = super.getCharacterEncoding();
        return (enc != null ? enc : WebUtils.DEFAULT_CHARACTER_ENCODING);
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream(), getCharacterEncoding()));
    }

    private static class CachedContentServletInputStream extends ServletInputStream {

        private final InputStream cachedBodyInputStream;

        public CachedContentServletInputStream(byte[] cachedBody) {
            this.cachedBodyInputStream = new ByteArrayInputStream(cachedBody);
        }

        @Override
        public boolean isFinished() {
            try {
                return cachedBodyInputStream.available() == 0;
            } catch (IOException e) {
                //Swallow
            }
            return false;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int read() throws IOException {
            return cachedBodyInputStream.read();
        }
    }
}

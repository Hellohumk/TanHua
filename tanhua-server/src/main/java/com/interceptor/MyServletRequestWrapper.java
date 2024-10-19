package com.interceptor;

import org.apache.commons.io.IOUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * servlet提供的包装的reequest类
 *
 * HttpServletRequestWrapper 是 HttpServletRequest的实现
 *
 */

public class MyServletRequestWrapper extends HttpServletRequestWrapper {

    private final byte[] body;

    /**
     * Construct a wrapper for the specified request.
     * 入口
     * @param request The request to be wrapped
     */
    public MyServletRequestWrapper(HttpServletRequest request) throws
            IOException {
        super(request);
        body = IOUtils.toByteArray(super.getInputStream());
    }


    /**
     * 获得一个Reader对象（别告诉我你不知道它是IO这个包的），通过它获取数据
     *
     * 明白了，这个类只要跟InputStream有关的读取方法都必须重写
     * 只读取一次的问题出在哪，就在哪解决
     *
     * @return
     * @throws IOException
     */
    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new
                InputStreamReader(getInputStream()));
    }

    /**
     * 见下面，我们创建了一个ServletInputStream的子类
     *
     * 这里相当于，只要在这个封装类（HttpServletRequest的）中请求InputStream，
     * 就必须用我们这个加强版（子类）的RequestBodyCachingInputStream
     *
     * @return
     * @throws IOException
     */
    @Override
    public ServletInputStream getInputStream() throws IOException {
        return new RequestBodyCachingInputStream(body);
    }

    /**
     * ServletInputStream 继承于 InputStream。是专用于Servlet的InputStream（方法封装）
     */
    private class RequestBodyCachingInputStream extends ServletInputStream
    {

        //存储body信息
        private byte[] body;

        //写指针
        private int lastIndexRetrieved = -1;
        private ReadListener listener;
        public RequestBodyCachingInputStream(byte[] body) {
            this.body = body;
        }
        @Override
        public int read() throws IOException {
            if (isFinished()) {
                return -1;
            }
            int i = body[lastIndexRetrieved + 1];
            lastIndexRetrieved++;
            if (isFinished() && listener != null) {
                try {
                    listener.onAllDataRead();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return i;
        }

        @Override
        public boolean isFinished() {
            return lastIndexRetrieved == body.length - 1;
        }
        @Override
        public boolean isReady() {
// This implementation will never block
// We also never need to call the readListener from this
//            method, as this method will never return false
            return isFinished();
        }
        @Override
        public void setReadListener(ReadListener listener) {
            if (listener == null) {
                throw new IllegalArgumentException("listener cann not be null");
            }
            if (this.listener != null) {
                throw new IllegalArgumentException("listener has been set");
            }
            this.listener = listener;
            if (!isFinished()) {
                try {
                    listener.onAllDataRead();
                } catch (IOException e) {
                    listener.onError(e);
                }
            } else {
                try {
                    listener.onAllDataRead();
                } catch (IOException e) {
                    listener.onError(e);
                }
            }
        }
        @Override
        public int available() throws IOException {
            return body.length - lastIndexRetrieved - 1;
        }
        @Override
        public void close() throws IOException {
            lastIndexRetrieved = body.length - 1;
            body = null;
        }
    }
}





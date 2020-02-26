package com.joon.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpConfig;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.HttpResponse;
import org.tio.http.common.RequestLine;
import org.tio.http.common.handler.HttpRequestHandler;

public class HttpHandle implements HttpRequestHandler {

    public static final HttpHandle me = new HttpHandle();
    private static Logger logger = LoggerFactory.getLogger(HttpHandle.class);


    @Override
    public HttpResponse handler(HttpRequest httpRequest) throws Exception {
        logger.info("   joon  -- HttpHandle  -- http请求");
        if (httpRequest.getRequestLine().getPath().equals("/check")){
            logger.info("   joon  -- HttpHandle  -- 心跳检查");
            HttpResponse httpResponse = new HttpResponse(httpRequest);
            return httpResponse;
        }
        return null;
    }

    @Override
    public HttpResponse resp404(HttpRequest httpRequest, RequestLine requestLine) {
        return null;
    }

    @Override
    public HttpResponse resp500(HttpRequest httpRequest, RequestLine requestLine, Throwable throwable) {
        return null;
    }

    @Override
    public HttpConfig getHttpConfig(HttpRequest httpRequest) {
        return null;
    }

    @Override
    public void clearStaticResCache() {

    }

}

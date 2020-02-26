package com.joon.websocket;

import com.alibaba.fastjson.JSON;
import com.joon.Main;
import com.joon.WebsocketStarter;
import com.joon.config.WebSocketServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.HttpResponse;
import org.tio.websocket.common.WsRequest;
import org.tio.websocket.common.WsResponse;
import org.tio.websocket.common.WsSessionContext;
import org.tio.websocket.server.handler.IWsMsgHandler;

import java.util.ArrayList;
import java.util.List;

@Component
public class WebSocketHandlerServer implements IWsMsgHandler {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketHandlerServer.class);
    public static final WebSocketHandlerServer me = new WebSocketHandlerServer();


    /**
     * 握手时走这个方法，业务可以在这里获取cookie，request参数等
     */
    @Override
    public HttpResponse handshake(HttpRequest request, HttpResponse httpResponse, ChannelContext channelContext) throws Exception {
        String clientip = request.getClientIp();
        logger.info("Path : {} 收到来自{}的ws握手包\r\n{}", request.getRequestLine().getPath(), clientip, request.toString());
        if (WebsocketStarter.subscription.isUnsubscribed()){
            Main.context.getBean(WebsocketStarter.class).web3jGetData();
        }

        if (request.getRequestLine().getPath().equals("/web3j/v1.0/websocket")) {
            logger.info(" joon  -- 加入到组：{}",WebSocketServerConfig.BLOCK_MESSAGE_NAME);
            Tio.bindGroup(channelContext, WebSocketServerConfig.BLOCK_MESSAGE_NAME);
            return httpResponse;
        }
        return null;
    }

    /**
     * 握手之后
     *
     * @param httpRequest
     * @param httpResponse
     * @param channelContext
     * @throws Exception
     * @author joon.H
     */
    @Override
    public void onAfterHandshaked(HttpRequest httpRequest, HttpResponse httpResponse, ChannelContext channelContext) throws Exception {
        logger.info(" joon -- WebSocketHandlerServer - for --  握手之后");
    }

    /**
     * 字节消息（binaryType = arraybuffer）过来后会走这个方法
     */
    @Override
    public Object onBytes(WsRequest wsRequest, byte[] bytes, ChannelContext channelContext) throws Exception {
        logger.info(" joon -- WebSocketHandlerServer - for --  字节消息");
        return null;
    }

    /**
     * 当客户端发close flag时，会走这个方法
     */
    @Override
    public Object onClose(WsRequest wsRequest, byte[] bytes, ChannelContext channelContext) throws Exception {
        logger.info(" joon -- WebSocketHandlerServer - for --  断开连接");
        return null;
    }

    /*
     * 字符消息（binaryType = blob）过来后会走这个方法
     */
    @Override
    public Object onText(WsRequest wsRequest, String text, ChannelContext channelContext) throws Exception {
        logger.info(" joon -- WebSocketHandlerServer - for --  字符消息 ： {}", text);
        return null;
    }


}

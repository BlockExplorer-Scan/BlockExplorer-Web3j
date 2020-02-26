/**
 * 
 */
package com.joon.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.intf.Packet;
import org.tio.websocket.server.WsServerAioListener;

public class WebSocketServerAioListener extends WsServerAioListener {
	private static Logger log = LoggerFactory.getLogger(WebSocketServerAioListener.class);

	public static final WebSocketServerAioListener me = new WebSocketServerAioListener();

	private WebSocketServerAioListener() {

	}

	@Override
	public void onAfterConnected(ChannelContext channelContext, boolean isConnected, boolean isReconnect) throws Exception {
		super.onAfterConnected(channelContext, isConnected, isReconnect);
	}

	@Override
	public void onAfterSent(ChannelContext channelContext, Packet packet, boolean isSentSuccess) throws Exception {
		super.onAfterSent(channelContext, packet, isSentSuccess);
	}

	@Override
	public void onBeforeClose(ChannelContext channelContext, Throwable throwable, String remark, boolean isRemove) throws Exception {
		super.onBeforeClose(channelContext, throwable, remark, isRemove);

		/*WsSessionContext wsSessionContext = (WsSessionContext) channelContext.getAttribute();

		if (wsSessionContext.isHandshaked()) {
			
			int count = Tio.getAllChannelContexts(channelContext.groupContext).getObj().size();

			String msg = channelContext.getClientNode().toString() + " 离开了，现在共有【" + count + "】人在线";
			//用tio-websocket，服务器发送到客户端的Packet都是WsResponse
			WsResponse wsResponse = WsResponse.fromText(msg, ShowcaseServerConfig.CHARSET);
			//群发
//			Tio.sendToGroup(channelContext.groupContext, Const.GROUP_ID, wsResponse);
		}*/
	}

	@Override
	public void onAfterDecoded(ChannelContext channelContext, Packet packet, int packetSize) throws Exception {
		super.onAfterDecoded(channelContext, packet, packetSize);
	}

	@Override
	public void onAfterReceivedBytes(ChannelContext channelContext, int receivedBytes) throws Exception {
		super.onAfterReceivedBytes(channelContext, receivedBytes);
	}

	@Override
	public void onAfterHandled(ChannelContext channelContext, Packet packet, long cost) throws Exception {
		super.onAfterHandled(channelContext, packet, cost);
	}

}

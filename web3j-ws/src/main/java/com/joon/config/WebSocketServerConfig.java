/**
 * 
 */
package com.joon.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.tio.utils.time.Time;

@Component
public  class WebSocketServerConfig {

	@Value("#{config['web3j-address']}")
	public String WEB3J_ADDRESS="http://localhost:8545";

	/**
	 * 协议名字(可以随便取，主要用于开发人员辨识)
	 */
	public static final String PROTOCOL_NAME = "web3j-ws";

	public static final String BLOCK_MESSAGE_NAME = "block_message";

	public static final String CHARSET = "utf-8";
	/**
	 * 监听的ip
	 */
	public static final String SERVER_IP = null;//null表示监听所有，并不指定ip

	/**
	 * 监听端口
	 *
	 */
	@Value("#{config['server-port']}")
	public int SERVER_PORT = 8080;

	/**
	 * 监听HTTP端口
	 *
	 */
	@Value("#{config['http-port']}")
	public int HTTP_PORT = 9090;


	/**
	 * 心跳超时时间，单位：毫秒
	 */
	public static final int HEARTBEAT_TIMEOUT = 1000 * 60 * 30;

	/**
	 * ip数据监控统计，时间段
	 * @author tanyaowu
	 *
	 */
	public static interface IpStatDuration {
		public static final Long DURATION_1 = Time.MINUTE_1 * 5;
		public static final Long[] IPSTAT_DURATIONS = new Long[] { DURATION_1 };
	}

}

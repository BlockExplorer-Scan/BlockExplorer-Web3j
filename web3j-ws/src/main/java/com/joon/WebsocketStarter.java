package com.joon;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonObject;
import com.joon.config.WebSocketServerConfig;
import com.joon.listener.HttpHandle;
import com.joon.listener.WebSocketIpStatListener;
import com.joon.listener.WebSocketServerAioListener;
import com.joon.websocket.WebSocketHandlerServer;
import com.sun.net.ssl.HttpsURLConnection;
import org.apache.http.HttpServerConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.http.common.HttpConfig;
import org.tio.http.server.HttpServerStarter;
import org.tio.server.ServerGroupContext;
import org.tio.utils.lock.SetWithLock;
import org.tio.websocket.common.WsResponse;
import org.tio.websocket.server.WsServerStarter;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.http.HttpService;
import rx.Subscription;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
public class WebsocketStarter {

    private static final Logger logger = LoggerFactory.getLogger(WebsocketStarter.class);

    private ServerGroupContext serverGroupContext;

    @Autowired
    WebSocketServerConfig webSocketServerConfig;

    public  static Subscription subscription;

    /**
     * @throws IOException
     * @author tanyaowu
     */
    public void start() throws Exception {

        WsServerStarter wsServerStarter = new WsServerStarter(webSocketServerConfig.SERVER_PORT, WebSocketHandlerServer.me);
        HttpServerStarter httpServerStarter = new HttpServerStarter
                (new HttpConfig(webSocketServerConfig.HTTP_PORT, 3600L, null, null), HttpHandle.me);

        serverGroupContext = wsServerStarter.getServerGroupContext();
        serverGroupContext.setName(WebSocketServerConfig.PROTOCOL_NAME);
        serverGroupContext.setServerAioListener(WebSocketServerAioListener.me);

        //设置ip统计时间段
        serverGroupContext.ipStats.addDurations(WebSocketServerConfig.IpStatDuration.IPSTAT_DURATIONS);
        //设置ip监控
        serverGroupContext.setIpStatListener(WebSocketIpStatListener.me);
        //设置心跳超时时间
        serverGroupContext.setHeartbeatTimeout(WebSocketServerConfig.HEARTBEAT_TIMEOUT);
        //如果你希望通过wss来访问，就加上下面的代码吧，不过首先你得有SSL证书（证书必须和域名相匹配，否则可能访问不了ssl）
//		String keyStoreFile = "classpath:config/ssl/keystore.jks";
//		String trustStoreFile = "classpath:config/ssl/keystore.jks";
//		String keyStorePwd = "214323428310224";
//		serverGroupContext.useSsl(keyStoreFile, trustStoreFile, keyStorePwd);
        wsServerStarter.start();
        httpServerStarter.start();
        web3jGetData();

    }

    public void web3jGetData() {
        Web3j web3 = Web3j.build(new HttpService(webSocketServerConfig.WEB3J_ADDRESS));  // defaults to http://localhost:8545/


        try {


            //获取挖矿信息
            subscription = web3.blockObservable(true).subscribe(block -> {
                if (serverGroupContext.groups.getGroupmap().get(WebSocketServerConfig.BLOCK_MESSAGE_NAME) != null) {
                    //获取所有交易
                    List<EthBlock.TransactionResult> transactionResultList = block.getBlock().getTransactions();
                    //遍历交易
                    for (EthBlock.TransactionResult tr : transactionResultList) {
                        try {
                            Transaction transaction = (Transaction) tr.get();
                            EthGetTransactionReceipt ethGetTransactionReceipt =
                                    web3.ethGetTransactionReceipt(transaction.getHash()).sendAsync().get();

                            if(!ethGetTransactionReceipt.getResult().getStatus().equals("0x0")){
                                //如果 TO 为空，内部交易
                                if (transaction.getTo() == null || "".equals(transaction.getTo())) {
                                    logger.info("   joon blockObservable -- 创建内部交易合约地址");
                                    String contractAddress = ethGetTransactionReceipt.getResult().getContractAddress();
                                    transaction.setTo(contractAddress);
                                }
                            }/*else {  // else 失败的交易
                                transactionResultList.remove(tr);
                            }*/

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }


                    WsResponse wsResponse = WsResponse.fromText(JSONObject.toJSONString(block), WebSocketServerConfig.CHARSET);
                    logger.info(" 生成新块 number:{} -- hash : {} -- parentHash : {}  -- 交易总数量 ：{}",
                            block.getBlock().getNumber(), block.getBlock().getHash(),
                            block.getBlock().getParentHash(), block.getBlock().getTransactions().size());
                    Tio.sendToGroup(serverGroupContext, WebSocketServerConfig.BLOCK_MESSAGE_NAME, wsResponse);
                }
            });

        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("   joon  blockObservable (true) Exception : {}", ex.getMessage());
            if (subscription.isUnsubscribed() == true) {
                logger.error("   joon  blockObservable (true)  subscription.isUnsubscribed() == true 监听已断开！");
                web3jGetData();
            }
        }
    }

    /**
     * @return the serverGroupContext
     */
    public ServerGroupContext getServerGroupContext() {
        return serverGroupContext;
    }


}

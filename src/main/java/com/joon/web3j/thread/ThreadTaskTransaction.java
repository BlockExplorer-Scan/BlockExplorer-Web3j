package com.joon.web3j.thread;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.joon.web3j.Main;
import com.joon.web3j.model.CallTracer;
import com.joon.web3j.model.CallTracerMain;
import com.joon.web3j.util.CommonUtils;
import com.joon.web3j.util.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.response.*;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @program web3j-quorum
 * @author: joon.h
 * @create: 2018/11/23 20:53
 */
public class ThreadTaskTransaction implements Runnable, Serializable {

    private static Logger logger = LoggerFactory.getLogger(ThreadTaskTransaction.class);


    private static final JmsTemplate jmsTemplate = Main.context.getBean(JmsTemplate.class);

    private Transaction tx;

    public ThreadTaskTransaction(Transaction tx) {
        this.tx = tx;
    }

    @Override
    public void run() {

        try {
            EthGetTransactionReceipt ethGetTransactionReceipt = Main.web3j.ethGetTransactionReceipt(tx.getHash()).sendAsync().get();

            JSONObject jsonObjectTx = JSONObject.parseObject(JSONObject.toJSONString(tx));
            jsonObjectTx.put("gasUsed", ethGetTransactionReceipt.getResult().getGasUsed());
            EthBlock ethBlock =Main.web3j.ethGetBlockByNumber(new DefaultBlockParameterNumber(tx.getBlockNumber()),false).send();
            EthBlock.Block block = ethBlock.getBlock();
            jsonObjectTx.put("timestamp", block.getTimestamp().longValue()*1000+"");

            if (!ethGetTransactionReceipt.getResult().getStatus().equals("0x0")) {

                EthGetCode ethGetCode = Main.web3j.ethGetCode(
                        tx.getTo(), DefaultBlockParameterName.LATEST).send();

                if (tx.getValueRaw().equals("0x0")) {

                    logger.info(" 内部交易成功 -- 区块高度 : {} -- 内部交易hash ：{} -- 内部交易from ：{} " +
                                " -- 内部交易to ：{}  -- GasUsed : {} -- start",
                                tx.getBlockNumber(), tx.getHash(), tx.getFrom(),
                                tx.getTo(), jsonObjectTx.get("gasUsed"));

                    if (tx.getTo() == null || "".equals(tx.getTo())) {

                        logger.info("合约创建 -- 区块高度 : {} -- 内部交易hash ：{}  -- 内部交易from ：{} " +
                                    "-- 内部交易to ：{}  -- GasUsed : {} -- start",
                                    tx.getBlockNumber(), tx.getHash(), tx.getFrom(),
                                    tx.getTo(), jsonObjectTx.get("gasUsed"));

                        //验证是否是创建合约
                        String contractAddress = ethGetTransactionReceipt.getResult().getContractAddress();
                        jsonObjectTx.put("to", contractAddress);

                    }
                    //内部交易 获取erc20 token转账
                    internalTransactionHandle(ethGetTransactionReceipt, jsonObjectTx);

                    //获取TraceTransaction 主币内部转账
                    traceTransactionHandel(block);

                    logger.info("内部交易成功 -- 区块高度 : {} -- 内部交易hash ：{}  -- 内部交易from ：{} " +
                                "-- 内部交易to ：{}  --  GasUsed : {} -- end",
                                tx.getBlockNumber(), tx.getHash(), tx.getFrom(),
                                tx.getTo(), jsonObjectTx.get("gasUsed"));

                } else {
                    //处理外部交易
                    externalTransactionHandle(jsonObjectTx);
                    if (ethGetCode.getCode()!="0x"){
                        //获取TraceTransaction 主币内部转账
                        traceTransactionHandel(block);
                    }
                }

            }else {

                logger.info("失败交易 发送到消息队列 " +
                                " -- 区块高度 : {}  交易索引:{} -- 交易hash : {}" +
                                "-- 区块hash : {}  -- 发送人：{} -- 接收人：{} " +
                                "--交易金额: {} --  GasUsed : {} start",
                        tx.getBlockNumber(), tx.getTransactionIndex(), tx.getHash(),
                        tx.getBlockHash(), tx.getFrom(), tx.getTo(),
                        tx.getValue(), jsonObjectTx.get("gasUsed"));

                jmsTemplate.send(Constant.TRANSACION_MESSAGE_FAIL, new MessageCreator() {
                    public Message createMessage(Session session) throws JMSException {
                        return session.createTextMessage(JSONObject.toJSONString(jsonObjectTx));
                    }
                });

                logger.info("失败交易 发送到消息队列 " +
                                " -- 区块高度 : {}  交易索引:{} -- 交易hash : {}" +
                                "-- 区块hash : {}  -- 发送人：{} -- 接收人：{} " +
                                "--交易金额: {} --  GasUsed : {} end",
                        tx.getBlockNumber(), tx.getTransactionIndex(), tx.getHash(),
                        tx.getBlockHash(), tx.getFrom(), tx.getTo(),
                        tx.getValue(), jsonObjectTx.get("gasUsed"));

            }
        } catch (Exception ex) {
            logger.error(" joon  ThreadTaskTransaction  交易异常 -- 区块高度 : {} -- 内部交易hash ：{}  " +
                         " -- 内部交易from ：{} -- 内部交易to ：{}  -- Exception : {}",
                         tx.getBlockNumber(), tx.getHash(),
                         tx.getFrom(), tx.getTo(), ex.getMessage());
            ex.printStackTrace();
            run();
        }

    }

    /**
     * 内部交易处理
     *  @param ethGetTransactionReceipt
     * @param jsonObjectTx
     */
    private void internalTransactionHandle(EthGetTransactionReceipt ethGetTransactionReceipt, JSONObject jsonObjectTx) {
        try {
            jmsTemplate.send(Constant.INTERNAL_TRANSACION_MESSAGE, new MessageCreator() {
                public Message createMessage(Session session) throws JMSException {
                    return session.createTextMessage(JSONObject.toJSONString(jsonObjectTx));
                }
            });

            //获取erc20 token转账
            List<Log> list = ethGetTransactionReceipt.getResult().getLogs();
            for (Log log : list) {
                jmsTemplate.send(Constant.ETH_GET_TRANSACION_RECEIPT_LOG, new MessageCreator() {
                    public Message createMessage(Session session) throws JMSException {
                        JSONObject logJson = JSONObject.parseObject(JSONObject.toJSONString(log));
                        logJson.put("timestamp", jsonObjectTx.get("timestamp"));
                        return session.createTextMessage(JSONObject.toJSONString(logJson));
                    }
                });
            }
            logger.info("内部交易成功 -- erc20 token -- 区块高度 : {} -- 内部交易hash ：{}  -- 内部交易from ：{} -- 内部交易to ：{}  发送到消息队列",
                    tx.getBlockNumber(), tx.getHash(), tx.getFrom(), tx.getTo());
        } catch (Exception ex) {
            logger.error("   joon  ThreadTaskTransaction -- erc20 token internalTransactionHandle " +
                            "-- 区块高度 : {} -- 内部交易hash ：{}  -- 内部交易from ：{} -- 内部交易to ：{} - Exception :{}",
                    tx.getBlockNumber(), tx.getHash(), tx.getFrom(), tx.getTo(), ex.getMessage());
            ex.printStackTrace();
//            internalTransactionHandle(ethGetTransactionReceipt, jsonObjectTx);
        }


    }

    /**
     * 处理外部交易并发送到消息队列
     *
     * @param jsonObjectTx
     */
    private void externalTransactionHandle(JSONObject jsonObjectTx) {
        try {
            logger.info("外部交易成功 发送到消息队列 " +
                            " -- 区块高度 : {}  交易索引:{} -- 交易hash : {}" +
                            "-- 区块hash : {}  -- 发送人：{} -- 接收人：{} " +
                            "--交易金额: {} --  GasUsed : {} start",
                    tx.getBlockNumber(), tx.getTransactionIndex(), tx.getHash(),
                    tx.getBlockHash(), tx.getFrom(), tx.getTo(),
                    tx.getValue(), jsonObjectTx.get("gasUsed"));
            jmsTemplate.send(Constant.TRANSACION_MESSAGE, new MessageCreator() {
                public Message createMessage(Session session) throws JMSException {
                    return session.createTextMessage(JSONObject.toJSONString(jsonObjectTx));
                }
            });
            logger.info("外部交易成功 发送到消息队列 " +
                            " -- 区块高度 : {}  交易索引:{} -- 交易hash : {}" +
                            "-- 区块hash : {}  -- 发送人：{} -- 接收人：{} " +
                            "--交易金额: {} --  GasUsed : {} end",
                    tx.getBlockNumber(), tx.getTransactionIndex(), tx.getHash(),
                    tx.getBlockHash(), tx.getFrom(), tx.getTo(),
                    tx.getValue(), jsonObjectTx.get("gasUsed"));
        } catch (Exception ex) {
            logger.error("   joon  ThreadTaskTransaction -- 外部交易 externalTransactionHandle" +
                            "-- 区块高度 : {}  交易索引:{} -- 交易hash : {} -- 区块hash : {}  -- 发送人：{} " +
                            "-- 接收人：{} --交易金额: {} --  GasUsed : {}   - Exception :{}",
                    tx.getBlockNumber(), tx.getTransactionIndex(), tx.getHash(),
                    tx.getBlockHash(), tx.getFrom(), tx.getTo(),
                    tx.getValue(), jsonObjectTx.get("gasUsed"), ex.getMessage());
            ex.printStackTrace();
//            externalTransactionHandle(jsonObjectTx);
        }
    }

    /**
     * 通过debug_traceTransaction 获取主币内部转账
     * @param block
     */
    private void traceTransactionHandel(EthBlock.Block block) {
        try {
            Map<String, String> headers = new HashMap<String, String>(1);
            headers.put("Content-Type", "application/json");
            JsonRpcHttpClient client = null;

            client = new JsonRpcHttpClient(new URL(Main.web3j_address), headers);

            JSONArray jsonArray = JSONArray.parseArray("[\"" + tx.getHash() + "\",{\"tracer\":\"callTracerMain\"}]");
            CallTracerMain callTracerMain = client.invoke(Constant.DEBUG_TRACE_TRANSACTION, jsonArray, CallTracerMain.class);
            //获取交易收据（ethGetTransactionReceipt）
            // 判断calls下面的value是否等于0x0 如果不等于则为 主币内部转账·
            getCallsByValueNeq0x0(callTracerMain,block);
            logger.info("内部交易成功 -- 主币内部转账 traceTransactionHandel " +
                            "-- 区块高度 : {} -- 内部交易hash ：{}  -- 内部交易from ：{} -- 内部交易to ：{} -- callTracerMain :{}  发送到消息队列",
                    tx.getBlockNumber(), tx.getHash(), tx.getFrom(), tx.getTo(), callTracerMain.getCalls().size());
        } catch (Throwable throwable) {
            logger.error("   joon  ThreadTaskTransaction -- traceTransactionHandel" +
                            " -- 区块高度 : {} -- 区块hash : {} -- 交易hash : {} - Throwable :{}",
                    tx.getBlockNumber(), tx.getBlockHash(), tx.getHash(), throwable.getMessage());
            throwable.printStackTrace();

            jmsTemplate.send(Constant.CALL_TRACER_ERROR, new MessageCreator() {
                public Message createMessage(Session session) throws JMSException {
                    return session.createTextMessage(JSONObject.toJSONString(tx));
                }
            });

        /*   String exInfo = " 交易信息 ：" + JSONObject.toJSONString(tx) + "\n 异常信息 ：" + CommonUtils.getEmessage(throwable);
            jmsTemplate.send(Constant.CALL_TRACER_ERROR_EMAIL, new MessageCreator() {
                public Message createMessage(Session session) throws JMSException {
                    return session.createTextMessage(exInfo);
                }
            });*/
        }
    }

    /**
     * 递归calls下面的calls获取主币内部转账
     *
     * @param callTracerMain
     * @param block
     * @return
     */
    private void getCallsByValueNeq0x0(CallTracerMain callTracerMain, EthBlock.Block block) throws Exception {

        List<CallTracer> callTracers = callTracerMain.getCalls();

        for (int i = 0; i < callTracers.size(); i++) {
            if (callTracers.get(i).getValue() != null && !callTracers.get(i).getValue().equals("0x0")) {
                logger.info(" joon  -- 主币内部转账 区块高度 : {} -- 内部交易hash ：{}  traceTransaction ：{}",
                        tx.getBlockNumber(), tx.getHash(), JSONObject.toJSONString(callTracers.get(i)));

                JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(callTracers.get(i)));
                jsonObject.put("transactionHash", tx.getHash());
                jsonObject.put("blockNumber", tx.getBlockNumber());
                jsonObject.put("timestamp",block.getTimestamp().longValue()*1000+"");
                jmsTemplate.send(Constant.CALL_TRACER, new MessageCreator() {
                    public Message createMessage(Session session) throws JMSException {
                        return session.createTextMessage(JSONObject.toJSONString(jsonObject));
                    }
                });
            }
        }
    }
}

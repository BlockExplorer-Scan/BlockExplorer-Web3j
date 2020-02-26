package com.joon.web3j.thread;

import com.alibaba.fastjson.JSONObject;
import com.joon.web3j.Main;
import com.joon.web3j.util.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.web3j.protocol.core.methods.response.EthBlock;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.io.Serializable;

public class ThreadTaskBlock implements Runnable, Serializable {

    private static Logger logger = LoggerFactory.getLogger(ThreadTaskBlock.class);


    private  static final JmsTemplate jmsTemplate = Main.context.getBean(JmsTemplate.class);

    private EthBlock block;


    public ThreadTaskBlock(EthBlock block) {
        this.block = block;
    }

    @Override
    public void run() {

        logger.info("挖矿成功 发送到消息队列 number:{} -- hash : {} -- parentHash : {} -- start",
                block.getBlock().getNumber(),block.getBlock().getHash(),block.getBlock().getParentHash());
        try {

            jmsTemplate.send(Constant.BLOCK_MESSAGE, new MessageCreator() {
                public Message createMessage(Session session) throws JMSException {
                    return session.createTextMessage(JSONObject.toJSONString(block));
                }
            });

        }catch (Exception ex){
            logger.error("挖矿异常 发送到消息队列 number:{} -- hash : {} -- parentHash : {}  -- end",
                    block.getBlock().getNumber(),block.getBlock().getHash(),block.getBlock().getParentHash());
            ex.printStackTrace();
        }
        logger.info("挖矿成功 发送到消息队列 number:{} -- hash : {} -- parentHash : {}  -- end",
                block.getBlock().getNumber(),block.getBlock().getHash(),block.getBlock().getParentHash());
    }
}

package com.joon.web3j;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.joon.web3j.model.CallTracer;
import com.joon.web3j.model.CallTracerMain;
import com.joon.web3j.model.TraceTransaction;
import com.joon.web3j.thread.ThreadTaskBlock;
import com.joon.web3j.thread.ThreadTaskTransaction;
import com.joon.web3j.util.CommonUtils;
import com.joon.web3j.util.Constant;
import com.joon.web3j.util.TokenClient;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jms.core.MessageCreator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.DigestUtils;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Collection;
import rx.Observable;
import rx.Subscription;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @program active-mq
 * @author: joon.h
 * @create: 2018/11/21 16:31
 */
public class Main {

    private static Logger logger = LoggerFactory.getLogger(Main.class);

    public static ApplicationContext context = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");

    public static Web3j web3j;

    public static String web3j_address;

    public static long blockNumber_start;
    public static long blockNumber_end;



    public static void main(String[] args) {

        try {

            PropertiesFactoryBean propertiesFactoryBean = context.getBean(PropertiesFactoryBean.class);
            Properties properties = propertiesFactoryBean.getObject();
            web3j_address = (String) properties.get("web3j-address");
            blockNumber_start = Long.parseLong((String) properties.get("blockNumber-start"));
            blockNumber_end = Long.parseLong((String) properties.get("blockNumber-end"));

            ThreadPoolTaskExecutor threadPool = context.getBean(ThreadPoolTaskExecutor.class);


            web3j = Web3j.build(new HttpService(web3j_address));  // defaults to http://localhost:8545/


            // 指定获取某个区块区间的数据
//            replayTransactionsObservable(threadPool);


            //从 blockNumber_start 区块获取到最新的块，并且实时监听后续区块
//            catchUpToLatest(threadPool);

            logger.info(" 当前最新区块 ： {}",web3j.ethBlockNumber().send().getBlockNumber());


        } catch (Exception e) {
            logger.error("   joon  -- main  -- Exception {}", e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 指定获取某个区块区间的数据
     */
    private static void replayTransactionsObservable(ThreadPoolTaskExecutor threadPool) {


        web3j.replayBlocksObservable
                (new DefaultBlockParameterNumber(blockNumber_start),
                        blockNumber_end == -1
                                ? DefaultBlockParameterName.LATEST
                                : new DefaultBlockParameterNumber(blockNumber_end),
                        false)
                .subscribe(block -> {
                    try {
                        threadPool.execute(new ThreadTaskBlock(block));
                    } catch (Exception ex) {
                        logger.error("   joon  -- replayBlocksObservable  -- Exception {}", ex.getMessage());
                        ex.printStackTrace();
                        try {
                            Thread.sleep(3000);
                            threadPool.execute(new ThreadTaskBlock(block));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });

        web3j.replayTransactionsObservable(new DefaultBlockParameterNumber(blockNumber_start),
                blockNumber_end == -1
                        ? DefaultBlockParameterName.LATEST
                        : new DefaultBlockParameterNumber(blockNumber_end)).subscribe(tx -> {
            try {
                threadPool.execute(new ThreadTaskTransaction(tx));
            } catch (Exception ex) {
                logger.error("   joon  -- replayTransactionsObservable  -- Exception {}", ex.getMessage());
                ex.printStackTrace();
                try {
                    Thread.sleep(5000);
                    threadPool.execute(new ThreadTaskTransaction(tx));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


        });
    }


    /**
     * 从 blockNumber_start 区块获取到最新的块，并且实时监听后续区块
     *
     * @param threadPool
     */
    private static void catchUpToLatest(ThreadPoolTaskExecutor threadPool) {
        try {
            Subscription subscription = web3j.catchUpToLatestAndSubscribeToNewBlocksObservable(
                    new DefaultBlockParameterNumber(blockNumber_start), false)
                    .subscribe(block -> {

                        try {
                            threadPool.execute(new ThreadTaskBlock(block));
                        } catch (Exception ex) {
                            logger.error("   joon  -- catchUpToLatestAndSubscribeToNewBlocksObservable  -- Exception {}", ex.getMessage());
                            ex.printStackTrace();
                        }

                    });

            Subscription transaction = web3j.catchUpToLatestAndSubscribeToNewTransactionsObservable(
                    new DefaultBlockParameterNumber(blockNumber_start))
                    .subscribe(tx -> {

                        try {
                            threadPool.execute(new ThreadTaskTransaction(tx));
                        } catch (Exception ex) {
                            logger.error("   joon  -- catchUpToLatestAndSubscribeToNewTransactionsObservable  -- Exception {}", ex.getMessage());
                            ex.printStackTrace();
                        }

                    });
        }catch (Exception ex){
            ex.printStackTrace();
        }


    }


}

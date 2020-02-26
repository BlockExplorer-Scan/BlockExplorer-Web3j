package com.joon.web3j;

import com.joon.web3j.thread.ThreadTaskBlock;
import com.joon.web3j.thread.ThreadTaskTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.http.HttpService;
import rx.Subscription;

import java.util.*;

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
            replayBlocksAndTransactionsObservable(threadPool);

            //从 blockNumber_start 区块获取到最新的块，并且实时监听后续区块 21150927
//            catchUpToLatest(threadPool);

//            Subscription subscription = web3j.catchUpToLatestAndSubscribeToNewBlocksObservable(
//                    new DefaultBlockParameterNumber(blockNumber_start), false)
//                    .subscribe(block -> {
//
//                        try {
//                            logger.info("当前区块--->"+JSONObject.toJSONString(block));
////                            threadPool.execute(new ThreadTaskBlock(block));
//                        } catch (Exception ex) {
//                            logger.error("   joon  -- catchUpToLatestAndSubscribeToNewBlocksObservable  -- Exception {}", ex.getMessage());
//                            ex.printStackTrace();
//                        }
//
//                    });

//            logger.info(" 当前最新区块 ： {}",web3j.ethBlockNumber().send().getBlockNumber());


        } catch (Exception e) {
            logger.error("   joon  -- main  -- Exception {}", e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 指定获取某个区块区间的数据
     */
    private static void replayBlocksAndTransactionsObservable(ThreadPoolTaskExecutor threadPool) {


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

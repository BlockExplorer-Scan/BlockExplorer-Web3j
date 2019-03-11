package com.joon.web3j;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.joon.web3j.model.CallTracerMain;
import com.joon.web3j.model.TraceTransaction;
import com.joon.web3j.thread.ThreadTaskBlock;
import com.joon.web3j.thread.ThreadTaskTransaction;
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
import org.web3j.protocol.core.methods.response.EthAccounts;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Collection;
import rx.Subscription;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
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

    public static void main(String[] args) {

        try {

            PropertiesFactoryBean propertiesFactoryBean = context.getBean(PropertiesFactoryBean.class);
            Properties properties = propertiesFactoryBean.getObject();
            web3j_address = (String) properties.get("web3j-address");

            ThreadPoolTaskExecutor threadPool = context.getBean(ThreadPoolTaskExecutor.class);


            web3j = Web3j.build(new HttpService(web3j_address));  // defaults to http://localhost:8545/

            //获取挖矿信息
           /* Subscription subscription = web3j.blockObservable(true).subscribe(block -> {
                try {
                    logger.info(" block ---> {}",JSONObject.toJSONString(block));
                    if(block.getBlock().getTransactions().size()>0){
                        EthBlock.TransactionResult transactionResult = block.getBlock().getTransactions().get(0);


                        logger.info(" tx -- >"+JSONObject.toJSONString(transactionResult.get()));
                    }

                    threadPool.execute(new ThreadTaskBlock(block));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    logger.info("   joon  -- blockObservable  -- Exception {}", ex.getMessage());
                }
            });*/

  /*
            Subscription transaction = web3j.transactionObservable().subscribe(tx -> {
                try {
                    threadPool.execute(new ThreadTaskTransaction(tx));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    logger.info("   joon  -- transactionObservable  -- Exception {}", ex.getMessage());
                }
            });*/


            //   指定获取某个区块区间的数据
            Subscription subscription =
                    web3j.replayBlocksObservable
                            (new DefaultBlockParameterNumber(10000),
                                    DefaultBlockParameterName.LATEST, true)
                            .subscribe(block -> {
                                try {
                                    logger.info(" block ---> {}", JSONObject.toJSONString(block));
                                    threadPool.execute(new ThreadTaskBlock(block));
                                    if (block.getBlock().getTransactions().size() > 0) {

                                        for (EthBlock.TransactionResult transactionResult
                                                : block.getBlock().getTransactions()) {

                                            org.web3j.protocol.core.methods.response.Transaction transaction
                                                    = (org.web3j.protocol.core.methods.response.Transaction) transactionResult.get();
                                            logger.info(" transaction ---> {}", JSONObject.toJSONString(transaction));
                                            threadPool.execute(new ThreadTaskTransaction(transaction));
                                        }
                                    }
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    logger.info("   joon  -- blockObservable  -- Exception {}", ex.getMessage());
                                }
                            });

            /* web3j.replayTransactionsObservable(new DefaultBlockParameterNumber(7547390),
                     new DefaultBlockParameterNumber(7547390)).subscribe(tx -> {
                 try {
                     logger.info(" tx ---> {}",JSONObject.toJSONString(tx));
                     threadPool.execute(new ThreadTaskTransaction(tx));
                 } catch (Exception ex) {
                     ex.printStackTrace();
                     logger.info("   joon  -- transactionObservable  -- Exception {}", ex.getMessage());
                 }
             });*/

//            traceTransactionHandel();
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }


    public static boolean isNumeric0(String str) {
        for (int i = str.length(); --i >= 0; ) {
            int chr = str.charAt(i);
            if (chr < 48 || chr > 57)
                return false;
        }
        return true;
    }

    private static void traceTransactionHandel() {

        try {
            String txHash = "0x129cc5dea2768a61f7d8f7029045c00f025f7456a75ea3e52f5ad4c7f3596bb0";
//            String txHash = "0x729792a2f89fcfd8991447f0e4f2e75d513a30f03d409a0da3c8e439178c4173";
            Map<String, String> headers = new HashMap<String, String>(1);
            headers.put("Content-Type", "application/json");
            JsonRpcHttpClient client = null;

            client = new JsonRpcHttpClient(new URL("http://n10.ledx.xyz"), headers);
            JSONArray jsonArray = JSONArray.parseArray("[\"" + txHash + "\",{\"tracer\":\"callTracerMain\",\"timeout\":\"5m\"}]");
            CallTracerMain callTracerMain = client.invoke(Constant.DEBUG_TRACE_TRANSACTION, jsonArray, CallTracerMain.class);
            logger.info("callTracerMain : {}", JSONObject.toJSONString(callTracerMain));

        } catch (Exception ex) {
            ex.printStackTrace();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }


    /**
     * 查询代币余额
     */
    public static BigInteger getTokenBalance(Web3j web3j, String fromAddress, String contractAddress) {

        BigInteger balanceValue = BigInteger.ZERO;

        try {
            Function function = new Function("balanceOf",
                    Arrays.<Type>asList(new Address(fromAddress)),
                    Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
                    }));
            String data = FunctionEncoder.encode(function);
            Transaction transaction = Transaction.createEthCallTransaction(fromAddress, contractAddress, data);

            EthCall ethCall = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).send();
            List<Type> results = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
            balanceValue = (BigInteger) results.get(0).getValue();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return balanceValue;
    }

    public static BigInteger getTokenTotalSupply(Web3j web3j, String contractAddress) {

        BigInteger totalSupply = BigInteger.ZERO;
        try {
            Function function = new Function("totalSupply",
                    Arrays.<Type>asList(),
                    Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
                    }));

            String data = FunctionEncoder.encode(function);
            org.web3j.protocol.core.methods.request.Transaction transaction =
                    Transaction.createEthCallTransaction(contractAddress, contractAddress, data);

            EthCall ethCall = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).sendAsync().get();
            List<Type> results = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
            totalSupply = (BigInteger) results.get(0).getValue();

            return totalSupply;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return totalSupply;
    }

    /**
     * 查询代币余额
     */
    public static BigInteger getTokenBalancea(Web3j web3j, String address, String contractAddress) {

        BigInteger balanceValue = BigInteger.ZERO;

        try {

            Function function = new Function("balanceOf",
                    Arrays.<Type>asList(new Address(address)),
                    Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
                    }));

            String data = FunctionEncoder.encode(function);
            Transaction transaction = Transaction.createEthCallTransaction(address, contractAddress, data);
            EthCall ethCall = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).send();
            List<Type> results = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
            balanceValue = (BigInteger) results.get(0).getValue();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return balanceValue;
    }
}

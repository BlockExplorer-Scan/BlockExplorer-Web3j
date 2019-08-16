package com.joon.web3j.util;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

/**
 * @program web3j-quorum
 * @author: joon.h
 * @create: 2019/03/27 15:14
 */
public class CommonUtils{


   /* public static String getEmessage(Exception e){
        //StringWriter输出异常信息
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        e.printStackTrace(pw);
        pw.flush();
        sw.flush();
        return sw.toString();
    }*/

    public static String getEmessage(Throwable throwable){
        //StringWriter输出异常信息
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        pw.flush();
        sw.flush();
        return sw.toString();
    }

    /**
     * 查询代币余额
     */
    public static BigInteger getTokenBalance(Web3j web3j, String address, String contractAddress) {

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

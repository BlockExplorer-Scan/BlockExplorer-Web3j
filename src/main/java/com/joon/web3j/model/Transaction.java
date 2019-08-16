package com.joon.web3j.model;

/**
 * @program web3j-quorum
 * @author: joon.h
 * @create: 2019/03/11 15:04
 */
public class Transaction extends org.web3j.protocol.core.methods.response.Transaction {


    private  long timestamp;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

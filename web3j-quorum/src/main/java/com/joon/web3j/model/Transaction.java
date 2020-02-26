package com.joon.web3j.model;


public class Transaction extends org.web3j.protocol.core.methods.response.Transaction {


    private  long timestamp;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

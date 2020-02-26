package com.joon.web3j.model;

import java.io.Serializable;
import java.util.List;

public class CallTracer implements Serializable {

    private String type;
    private String from;
    private String to;
    private String input;
    private String gasIn;
    private String gasCost;
    private String outOff;
    private String outLen;
    private String value;
    private String error;


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getGasIn() {
        return gasIn;
    }

    public void setGasIn(String gasIn) {
        this.gasIn = gasIn;
    }

    public String getGasCost() {
        return gasCost;
    }

    public void setGasCost(String gasCost) {
        this.gasCost = gasCost;
    }

    public String getOutOff() {
        return outOff;
    }

    public void setOutOff(String outOff) {
        this.outOff = outOff;
    }

    public String getOutLen() {
        return outLen;
    }

    public void setOutLen(String outLen) {
        this.outLen = outLen;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "CallTracerMain{" +
                "type='" + type + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", input='" + input + '\'' +
                ", gasIn='" + gasIn + '\'' +
                ", gasCost='" + gasCost + '\'' +
                ", outOff='" + outOff + '\'' +
                ", outLen='" + outLen + '\'' +
                ", value='" + value + '\'' +
                ", error='" + error + '\'' +
                '}';
    }
}

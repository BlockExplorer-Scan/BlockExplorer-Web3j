package com.joon.web3j.model;

import java.io.Serializable;
import java.util.List;

/**
 * @program web3j-quorum
 * @author: joon.h
 * @create: 2018/12/27 17:22
 */
public class TraceTransaction implements Serializable {

    private String type;
    private String from;
    private String to;
    private String value;
    private String gas;
    private String gasUsed;
    private String input;
    private String output;
    private String time;
    private String error;
    private List<TraceTransaction> calls;


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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getGas() {
        return gas;
    }

    public void setGas(String gas) {
        this.gas = gas;
    }

    public String getGasUsed() {
        return gasUsed;
    }

    public void setGasUsed(String gasUsed) {
        this.gasUsed = gasUsed;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public List<TraceTransaction> getCalls() {
        return calls;
    }

    public void setCalls(List<TraceTransaction> calls) {
        this.calls = calls;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "TraceTransaction{" +
                "type='" + type + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", value='" + value + '\'' +
                ", gas='" + gas + '\'' +
                ", gasUsed='" + gasUsed + '\'' +
                ", input='" + input + '\'' +
                ", output='" + output + '\'' +
                ", time='" + time + '\'' +
                ", error='" + error + '\'' +
                ", calls=" + calls +
                '}';
    }
}

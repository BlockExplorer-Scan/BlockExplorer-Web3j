package com.joon.web3j.model;

import java.io.Serializable;
import java.util.List;

public class CallTracerMain implements Serializable {

   private List<CallTracer> calls;


    public List<CallTracer> getCalls() {
        return calls;
    }

    public void setCalls(List<CallTracer> calls) {
        this.calls = calls;
    }

    @Override
    public String toString() {
        return "CallTracerMain{" +
                "calls=" + calls +
                '}';
    }
}

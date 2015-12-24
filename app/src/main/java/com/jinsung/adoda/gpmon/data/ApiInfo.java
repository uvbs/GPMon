package com.jinsung.adoda.gpmon.data;

import java.io.Serializable;

/**
 * Created by adodaorbit on 2015-12-24.
 */
public class ApiInfo implements Serializable {

    private String name;
    private int requestCount;

    public ApiInfo(String name, int requestCount) {
        this.name = name;
        this.requestCount = requestCount;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRequestCount() {
        return this.requestCount;
    }

    public void setRequestCount(int requestCount) {
        this.requestCount = requestCount;
    }
}

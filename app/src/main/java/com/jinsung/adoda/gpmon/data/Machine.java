package com.jinsung.adoda.gpmon.data;

import java.io.Serializable;

/**
 * Created by adodaorbit on 2015-12-24.
 */
public class Machine implements Serializable {
    String name;

    public Machine(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

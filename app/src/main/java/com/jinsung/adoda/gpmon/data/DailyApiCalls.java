package com.jinsung.adoda.gpmon.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by adodaorbit on 2015-12-24.
 */
public class DailyApiCalls implements Serializable {

    private HashMap<Integer, ArrayList<ApiInfo>> mApiList;

    public DailyApiCalls() {
        mApiList = new HashMap<Integer, ArrayList<ApiInfo>>();
    }

    public void addItem(int hour, ArrayList<ApiInfo> infos) {
        mApiList.put(hour, infos);
    }

    public int getTotalCount(String apiName) {
        int sum = 0;

        for (int i = 0; i < 24; i++) {
            ArrayList<ApiInfo> list = mApiList.get(i);
            if (null == list)
                continue;

            for (int j = 0; j < list.size(); j++) {
                ApiInfo info = list.get(j);
                String name = info.getName();
                if (name.equals(apiName)) {
                    sum += info.getRequestCount();
                }
            }
        }

        return sum;
    }

    public int getNumOfApi() {
        int count = 0;

        ArrayList<ApiInfo> list = mApiList.get(13);
        if (list.size() > 0) {
            count = list.size();
        }

        return count;
    }

    public String getApiName(int index) {
        String apiName = "";

        ArrayList<ApiInfo> list = mApiList.get(13);
        if (list.size() > 0) {
            ApiInfo info = list.get(index);
            apiName += info.getName();
        }

        return apiName;
    }
}

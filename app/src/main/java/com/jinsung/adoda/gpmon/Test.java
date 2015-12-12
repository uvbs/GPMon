package com.jinsung.adoda.gpmon;

import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import static android.widget.Toast.*;

/**
 * Created by adoda on 2015-12-12.
 */
public class Test {
    private String name = null;
    private int age = 0;

    public Test(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public Test(JSONObject obj) throws JSONException {
        this.name = obj.getString("name");
        this.age = obj.getInt("age");
    }

    public String getName() {
        return this.name;
    }

    public int getAge() {
        return this.age;
    }

    public String toString() {
        return "name: " + this.name + ", age: " + this.age + "\n";
    }
}

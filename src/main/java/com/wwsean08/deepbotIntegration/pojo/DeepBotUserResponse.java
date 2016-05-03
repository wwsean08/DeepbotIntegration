package com.wwsean08.deepbotIntegration.pojo;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

/**
 * Created by sean on 4/30/16.
 */
public class DeepBotUserResponse
{
    @SerializedName("fuction")
    String function;
    @SerializedName("param")
    String param;
    @SerializedName("msg")
    User[] users;

    public String getFunction()
    {
        return function;
    }

    public String getParam()
    {
        return param;
    }

    public User[] getUsers()
    {
        return users;
    }

    @Override
    public String toString()
    {
        return "DeepBotUserResponse{" +
                "function='" + function + '\'' +
                ", param='" + param + '\'' +
                ", users=" + Arrays.toString(users) +
                '}';
    }
}

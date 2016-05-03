package com.wwsean08.deepbotIntegration.pojo;

import com.google.gson.annotations.SerializedName;

/**
 * Created by sean on 4/30/16.
 */
public class DeepbotBaseReponse
{
    @SerializedName("fuction")
    String function;
    @SerializedName("param")
    String param;
    @SerializedName("msg")
    String message;

    public String getFunction()
    {
        return function;
    }

    public String getParam()
    {
        return param;
    }

    public String getMessage()
    {
        return message;
    }

    @Override
    public String toString()
    {
        return "DeepbotBaseReponse{" +
                "function='" + function + '\'' +
                ", param='" + param + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}

package com.wwsean08.deepbotIntegration.pojo;

import com.google.gson.annotations.SerializedName;

/**
 * Created by sean on 4/30/16.
 */
public class User
{
    @SerializedName("user")
    private String user;
    @SerializedName("join_date")
    private String joinDate;
    @SerializedName("last_seen")
    private String lastSeen;
    @SerializedName("vip_expiry")
    private String vipExpiration;
    //Set from a separate call that the rest of these aren't filled out with
    @SerializedName("rank")
    private String rank = null;

    @SerializedName("points")
    private float points;
    @SerializedName("watch_time")
    private float watchTime;
    @SerializedName("vip")
    private int vip;
    @SerializedName("mod")
    private int mod;

    public String getUser()
    {
        return user;
    }

    public String getJoinDate()
    {
        return joinDate;
    }

    public String getLastSeen()
    {
        return lastSeen;
    }

    public String getVipExpiration()
    {
        return vipExpiration;
    }

    public String getRank()
    {
        return rank;
    }

    public void setRank(String rank)
    {
        this.rank = rank;
    }

    public float getPoints()
    {
        return points;
    }

    public float getWatchTime()
    {
        return watchTime;
    }

    public int getVip()
    {
        return vip;
    }

    public int getMod()
    {
        return mod;
    }

    @Override
    public String toString()
    {
        return "User{" +
                "user='" + user + '\'' +
                ", joinDate='" + joinDate + '\'' +
                ", lastSeen='" + lastSeen + '\'' +
                ", vipExpiration='" + vipExpiration + '\'' +
                ", rank='" + rank + '\'' +
                ", points=" + points +
                ", watchTime=" + watchTime +
                ", vip=" + vip +
                ", mod=" + mod +
                '}';
    }
}

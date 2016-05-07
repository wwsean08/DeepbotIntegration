package com.wwsean08.deepbotIntegration;

import com.google.gson.Gson;
import com.wwsean08.deepbotIntegration.pojo.User;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by sean on 4/30/16.
 */
public class Main
{
    public static void main(String[] args) throws Exception
    {
        long startTime = System.currentTimeMillis();
        int totalUsers = -1;
        String destUri = "ws://10.0.0.10:3337";
        URI echoURI = new URI(destUri);

        WebSocketClient userCountClient = new WebSocketClient();
        GetUserCountSocket getUserCountSocket = new GetUserCountSocket();
        try
        {
            userCountClient.start();
            ClientUpgradeRequest request = new ClientUpgradeRequest();
            userCountClient.connect(getUserCountSocket, echoURI, request);
            getUserCountSocket.awaitClose(4, TimeUnit.SECONDS);
            totalUsers = getUserCountSocket.getUserCount();
            userCountClient.stop();
            if(totalUsers != -1)
            {
                System.out.println("There are " + totalUsers + " according to deepbot.");
            }
            else
            {
                System.err.println("An error occurred and we didn't get a user count from deepbot.");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        WebSocketClient userClient = new WebSocketClient();
        GetUsersSocket getUsersSocket = new GetUsersSocket(totalUsers);
        try
        {
            userClient.start();
            ClientUpgradeRequest request = new ClientUpgradeRequest();
            userClient.connect(getUsersSocket, echoURI, request);
            getUsersSocket.awaitClose(10, TimeUnit.SECONDS);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        List<User> userList = getUsersSocket.getUserList();
        WebSocketClient rankClient = new WebSocketClient();
        rankClient.start();
        for (User user : userList)
        {
            GetRankSocket rgetRankSocketnkSocket = new GetRankSocket(user);
            ClientUpgradeRequest request = new ClientUpgradeRequest();
            rankClient.connect(rgetRankSocketnkSocket, echoURI, request);
            rgetRankSocketnkSocket.awaitClose(5, TimeUnit.SECONDS);
        }
        Gson gson = new Gson();
        String body = gson.toJson(userList);
        userClient.stop();
        rankClient.stop();

        System.out.println("Sending data to database");

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost post = new HttpPost("http://10.0.0.130:8080/");
        HttpEntity entity = new ByteArrayEntity(body.getBytes("UTF-8"));
        post.setEntity(entity);
        HttpResponse response = httpClient.execute(post);
        if (response.getStatusLine().getStatusCode() >= 200 && response.getStatusLine().getStatusCode() < 300)
        {
            System.out.println("Database updated (probably)");
            System.out.println(response.getStatusLine().toString() + " - " + IOUtils.toString(response.getEntity().getContent()));
        }
        else
        {
            System.err.println(response.getStatusLine().toString());
        }
        System.out.println((System.currentTimeMillis() - startTime)/1000 + " seconds");
    }
}

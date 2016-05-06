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

import java.io.StringBufferInputStream;
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
        String destUri = "ws://10.0.0.10:3337";
        WebSocketClient client = new WebSocketClient();
        GetUsersSocket socket = new GetUsersSocket();
        URI echoURI = new URI(destUri);

        try
        {
            client.start();
            ClientUpgradeRequest request = new ClientUpgradeRequest();
            client.connect(socket, echoURI, request);
            socket.awaitClose(10, TimeUnit.SECONDS);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        List<User> userList = socket.getUserList();
        WebSocketClient rankClient = new WebSocketClient();
        rankClient.start();
        for (User user : userList)
        {
            GetRankSocket rankSocket = new GetRankSocket(user);
            ClientUpgradeRequest request = new ClientUpgradeRequest();
            rankClient.connect(rankSocket, echoURI, request);
            rankSocket.awaitClose(5, TimeUnit.SECONDS);
        }
        Gson gson = new Gson();
        String body = gson.toJson(userList);
        client.stop();
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

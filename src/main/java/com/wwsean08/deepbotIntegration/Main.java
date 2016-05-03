package com.wwsean08.deepbotIntegration;

import com.wwsean08.deepbotIntegration.pojo.User;
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
        for (User user : userList)
        {
            System.out.println(user.toString());
        }
        client.stop();
        rankClient.stop();
        System.exit(0);
    }
}

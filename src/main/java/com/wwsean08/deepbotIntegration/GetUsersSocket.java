package com.wwsean08.deepbotIntegration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.wwsean08.deepbotIntegration.pojo.DeepBotUserResponse;
import com.wwsean08.deepbotIntegration.pojo.DeepbotBaseReponse;
import com.wwsean08.deepbotIntegration.pojo.User;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

/**
 * Basic Echo Client Socket
 */
@WebSocket(maxTextMessageSize = 64 * 1024)
public class GetUsersSocket
{

    private final CountDownLatch closeLatch;

    private boolean endLoop = false;

    private final List<User> userList;

    @SuppressWarnings("unused")
    private Session session;

    public GetUsersSocket()
    {
        this.closeLatch = new CountDownLatch(1);
        this.userList = new ArrayList<>();
    }

    public boolean awaitClose(int duration, TimeUnit unit) throws InterruptedException
    {
        return this.closeLatch.await(duration, unit);
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason)
    {
        System.out.printf("Connection closed: %d - %s%n", statusCode, reason);
        this.session = null;
        this.closeLatch.countDown();
    }

    @OnWebSocketConnect
    public void onConnect(Session session)
    {
        System.out.printf("Got connect: %s%n", session);
        this.session = session;
        try
        {
            Future<Void> fut;
            fut = session.getRemote().sendStringByFuture("api|register|7C8A5OPLHDAaISRCVeMRcAPRFPXLBAJULRfCJ");
            fut.get(2, TimeUnit.SECONDS);

            int i = 0;
            do
            {
                fut = session.getRemote().sendStringByFuture("api|get_users|" + i * 100 + "|100");
                fut.get(1, TimeUnit.SECONDS);
                i++;
            }
            while (!endLoop );

        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
    }

    @OnWebSocketMessage
    public void onMessage(String msg)
    {
        Gson gson = new Gson();
        try
        {
            DeepbotBaseReponse deepbotResponse = gson.fromJson(msg, DeepbotBaseReponse.class);
            if (deepbotResponse.getMessage().toLowerCase().contains("empty"))
            {
                System.out.println(msg);
                //end the loop, doesn't really work though because of the asynchronous nature
                //TODO: Make this actually end the loop
                endLoop = true;
            }
        }
        catch (JsonSyntaxException e)
        {
            //This is a user object and not a registration object.
            User[] users = gson.fromJson(msg, DeepBotUserResponse.class).getUsers();
            for (User user : users)
            {
                userList.add(user);
            }
        }
    }

    @OnWebSocketError
    public void onError(Throwable error)
    {
        error.printStackTrace();
    }

    public List<User> getUserList()
    {
        return this.userList;
    }
}
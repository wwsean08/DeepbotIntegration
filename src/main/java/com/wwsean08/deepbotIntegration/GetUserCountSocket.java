package com.wwsean08.deepbotIntegration;

import com.google.gson.Gson;
import com.wwsean08.deepbotIntegration.pojo.DeepbotBaseReponse;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by sean on 5/6/16.
 */
@WebSocket(maxTextMessageSize = 64 * 1024)
public class GetUserCountSocket
{
    private final CountDownLatch closeLatch;

    private int totalUsers = -1;

    @SuppressWarnings("unused")
    private Session session;

    public GetUserCountSocket()
    {
        this.closeLatch = new CountDownLatch(1);
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
            fut.get(1, TimeUnit.SECONDS);

            fut = session.getRemote().sendStringByFuture("api|get_users_count");
            fut.get(1, TimeUnit.SECONDS);
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
        DeepbotBaseReponse deepbotResponse = gson.fromJson(msg, DeepbotBaseReponse.class);
        if (!deepbotResponse.getMessage().equals("success"))
        {
            try
            {
                totalUsers = Integer.parseInt(deepbotResponse.getMessage());
                session.close();
                closeLatch.countDown();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    @OnWebSocketError
    public void onError(Throwable error)
    {
        error.printStackTrace();
    }

    public int getUserCount()
    {
        return totalUsers;
    }
}

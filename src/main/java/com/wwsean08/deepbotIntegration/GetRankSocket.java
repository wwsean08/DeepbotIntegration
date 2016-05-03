package com.wwsean08.deepbotIntegration;

import com.google.gson.Gson;
import com.wwsean08.deepbotIntegration.pojo.DeepbotBaseReponse;
import com.wwsean08.deepbotIntegration.pojo.User;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by sean on 5/1/16.
 */
@WebSocket(maxTextMessageSize = 64 * 1024)
public class GetRankSocket
{
    private final CountDownLatch closeLatch;

    private Session session;
    private User user;

    public GetRankSocket(User user)
    {
        this.closeLatch = new CountDownLatch(1);
        this.user = user;
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

            fut = session.getRemote().sendStringByFuture("api|get_rank|" + user.getUser());
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
        user.setRank(deepbotResponse.getMessage());
        session.close();
        this.closeLatch.countDown();
    }

    @OnWebSocketError
    public void onError(Throwable error)
    {
        error.printStackTrace();
    }
}

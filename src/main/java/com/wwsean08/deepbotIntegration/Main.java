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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Created by sean on 4/30/16.
 */
public class Main
{
    public static void main(String[] args) throws Exception
    {
        long startTime = System.currentTimeMillis();

        if (args.length < 1)
        {
            System.err.println("Please include an argument pointing to the properties file");
            System.exit(-2);
        }
        Properties props = getProps(args[0]);
        String apiKey = props.getProperty("api_key");

        int totalUsers = -1;
        String destUri = "ws://" + props.getProperty("deepbot_address") + ":3337";
        URI echoURI = new URI(destUri);

        WebSocketClient userCountClient = new WebSocketClient();
        GetUserCountSocket getUserCountSocket = new GetUserCountSocket(apiKey);
        try
        {
            userCountClient.start();
            ClientUpgradeRequest request = new ClientUpgradeRequest();
            userCountClient.connect(getUserCountSocket, echoURI, request);
            getUserCountSocket.awaitClose(4, TimeUnit.SECONDS);
            totalUsers = getUserCountSocket.getUserCount();
            userCountClient.stop();
            if (totalUsers != -1)
            {
                System.out.println("There are " + totalUsers + " according to deepbot.");
            }
            else
            {
                System.err.println("An error occurred and we didn't get a user count from deepbot.  Aborting.");
                System.exit(-1);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        WebSocketClient userClient = new WebSocketClient();
        GetUsersSocket getUsersSocket = new GetUsersSocket(totalUsers, apiKey);
        try
        {
            userClient.start();
            ClientUpgradeRequest request = new ClientUpgradeRequest();
            userClient.connect(getUsersSocket, echoURI, request);
            int maxTime = Integer.parseInt(props.getProperty("get_all_users_time_max"));
            getUsersSocket.awaitClose(maxTime, TimeUnit.SECONDS);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        List<User> userList = getUsersSocket.getUserList();
        WebSocketClient rankClient = new WebSocketClient();
        if (props.getProperty("get_ranks").equalsIgnoreCase("true"))
        {
            rankClient.start();
            for (User user : userList)
            {
                GetRankSocket getRankSocket = new GetRankSocket(user, apiKey);
                ClientUpgradeRequest request = new ClientUpgradeRequest();
                rankClient.connect(getRankSocket, echoURI, request);
                getRankSocket.awaitClose(5, TimeUnit.SECONDS);
            }
        }
        Gson gson = new Gson();
        String body = gson.toJson(userList);
        userClient.stop();
        rankClient.stop();
        if(props.getProperty("no_op").equalsIgnoreCase("true"))
        {
            System.out.println("Printing out user json with " + userList.size() + " records");
            System.out.println(gson.toJson(userList));
        }
        else
        {
            System.out.println("Sending data to database");

            HttpClient httpClient = new DefaultHttpClient();
            HttpPost post = new HttpPost(props.getProperty("storage_endpoint"));
            HttpEntity entity = new ByteArrayEntity(body.getBytes("UTF-8"));
            post.setEntity(entity);
            HttpResponse response = httpClient.execute(post);
            if (response.getStatusLine().getStatusCode() >= 200 && response.getStatusLine().getStatusCode() < 300)
            {
                System.out.println("Database updated (probably)");
                System.out.println(
                        response.getStatusLine().toString() + " - " +
                                IOUtils.toString(response.getEntity().getContent()));
            }
            else
            {
                System.err.println(response.getStatusLine().toString());
            }
        }
        System.out.println((System.currentTimeMillis() - startTime) / 1000 + " seconds");

    }

    /**
     * Load properties from a file at the given path
     *
     * @param path
     * @return
     */
    public static Properties getProps(String path)
    {
        Properties properties = new Properties();
        File file = new File(path);
        if (!file.exists())
        {
            System.err.println("Unable to find file " + path + " aborting.");
            System.exit(-1);
        }
        InputStream IS = null;
        try
        {
            IS = new FileInputStream(file);
            properties.load(IS);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            properties = null;
        }
        finally
        {
            IOUtils.closeQuietly(IS);
        }
        return properties;
    }
}

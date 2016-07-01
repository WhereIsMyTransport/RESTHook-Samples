package com.whereismytransport.resthook.client;

import com.whereismytransport.resthook.client.auth.ClientCredentials;
import spark.Request;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static spark.Spark.*;

/**
 * This API allows us to easily view logs and webhook bodies received by this client and
 * allows users to create new RESTHooks. This would generally be done at startup. But this
 * sample was created for demonstrative and testing purposes.
 */
public class RestHookTestApi {

    private List<String> logs;
    private List<String> messages;
    private RestHookRepository restHookRepository;
    private Map<String,RestHook> hooks;

    private int port;
    private String baseUrl;

    public RestHookTestApi(int port, String baseUrl, RestHookRepository restHookRepository, List<String> logs, List<String> messages){
        this.logs=logs;
        this.messages=messages;
        this.restHookRepository=restHookRepository;
        List<RestHook> restHooks = restHookRepository.getRestHooks();
        hooks= IntStream.range(0,restHooks.size()).boxed().collect(Collectors.toMap(i->restHooks.get(i).index,i->restHooks.get(i)));

        this.port=port;
        this.baseUrl=baseUrl;
    }

    public void start(){
        //Set the operating port
        port(port);

        // default get
        get("/", (req, res) -> "Test Webhook client api running.");

        // get logs
        get("/logs", (req, res) -> {
            res.status(200);
            return listToMultilineString(logs);
        });


        // get received webhook bodies
        get("/received_hooks", (req, res) -> {
            res.status(200);
            return listToMultilineString(messages);
        });

        post("/hooks/:id", (req, res) -> {
            logs.add(listToMultilineString(req.headers().stream().map(x->x).collect(Collectors.toList())));
            String id=req.params(":id");
            return hooks.get(id).handleHookMessage(req,res,messages,logs);
        });

        //Instruct client to create a RESThook at the address given as a UTF-8 encoded string in the body.
        post("/webhook", (req,res) -> {
            WebhookSparkRequestBody body=JsonDeserializer.convert(req.body(), WebhookSparkRequestBody.class, logs);
            processHook(new RestHookRetrofitRequest("Test Hook"),body,res);
            return res.body();
        });

        post("/channelwebhook", (req,res) -> {
            ChannelWebhookSparkRequestBody body=JsonDeserializer.convert(req.body(), ChannelWebhookSparkRequestBody.class, logs);
            logs.add("Character Limit: "+body.characterLimit);
            processHook(new ChannelRestHookRetrofitRequest("Test Hook", body.characterLimit),body, res);
            return res.body();
        });
    }

    private spark.Response processHook(RestHookRetrofitRequest webhookBody,WebhookSparkRequestBody sparkRequestBody, spark.Response res){
        try {
            URL target = new URL(sparkRequestBody.targetUrl);
            String host=target.getProtocol()+"://"+target.getAuthority()+"/";
            String path=target.getFile().substring(1);
            RestHook hook=null;

            for (String hookKey:hooks.keySet()) {
                RestHook restHook = hooks.get(hookKey);
                if(restHook.serverUrl.equals(host) && restHook.serverRelativeUrl.equals(path)){
                    hook=restHook;
                }
            }

            if(hook==null){
                hook= new RestHook(host,path,baseUrl);
                hooks.put(hook.index, hook);
            }

            hook.createHook(webhookBody,new ClientCredentials(sparkRequestBody.clientId,sparkRequestBody.clientSecret,sparkRequestBody.identityServerUrl,sparkRequestBody.scopes),logs,res);
        }
        catch(Exception e){
            for (StackTraceElement stackElement:e.getStackTrace()) {
                logs.add(stackElement.toString());
            }
            res.status(500);
            res.body("Couldn't create WebHook");
        }
        return res;
    }

    private static String listToMultilineString(List<String> list){
        String result="";
        for (String item: list) {
            result+=item+"\n";
        }
        return result;
    }
}

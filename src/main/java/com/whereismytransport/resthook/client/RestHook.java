package com.whereismytransport.resthook.client;

import com.whereismytransport.resthook.client.auth.Token;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import spark.Request;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RestHook {

    public String index;
    public String secret;

    public RestHook(String index) {
        this.index = index;
    }

    public RestHook(String index, String hmacSecret) {
        this.index = index;
        this.secret = hmacSecret;
    }


    public spark.Response handleHookMessage(Request req, spark.Response res, List<String> messages, List<String> logs, Map<String, RestHook> hooks, RestHookRepository repository, String handshakeKey) {
        if (req.headers().stream().anyMatch(x -> x.toLowerCase().equals("x-hook-signature"))) {
            String body = req.body();
            String xHookSignature = req.headers("x-hook-signature");
            try {
                if (HmacUtilities.validBody(this, body, xHookSignature)) {
                    messages.add(body);
                    res.status(200); //OK
                } else {
                    res.status(403); //Access denied
                    String responseMessage = "Access denied: X-Hook-Signature does not match the secret.";
                    logs.add(responseMessage);
                    res.body(responseMessage);
                }
            } catch (Exception e) {
                for (StackTraceElement stackElement : e.getStackTrace()) {
                    logs.add(stackElement.toString());
                }
                String responseMessage = "Exception occurred encoding hash: " + e.getStackTrace().toString();
                res.status(500); //Internal server error
                return res;
            }
        } else  if (req.headers().stream().anyMatch(x -> x.toLowerCase().equals("x-hook-secret"))) {
            res.status(200);
            String handshake = req.headers("x-hook-handshake");
            if (!handshakeKey.equals(handshake)) {
                res.status(403); //Access denied
                String responseMessage = "Access denied: x-hook-handshake does not match handshake key.";
                logs.add(responseMessage);
                res.body(responseMessage);
            }
            secret = req.headers("x-hook-secret");
            res.header("x-hook-secret", secret);
            repository.addOrReplaceRestHook(this);
            hooks.put(index, this);
            return res;
        }
        else {
            res.status(403);
            String responseMessage = "Access denied: X-Hook-Signature or X-Hook-Secret is not present in headers.";
            logs.add(responseMessage);
            res.body(responseMessage);
        }
        return res;
    }

}


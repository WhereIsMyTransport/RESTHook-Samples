package com.whereismytransport.resthook.client;

import java.util.List;
import java.util.Map;
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
    private Map<String, RestHook> hooks;
    private String handshakeKey;
    private int port;


    public RestHookTestApi(int port, String baseUrl, RestHookRepository restHookRepository, List<String> logs, List<String> messages, String handshakeKey) {
        this.logs = logs;
        this.handshakeKey = handshakeKey;
        this.messages = messages;
        this.restHookRepository = restHookRepository;
        List<RestHook> restHooks = restHookRepository.getRestHooks();
        hooks = IntStream.range(0, restHooks.size()).boxed().collect(Collectors.toMap(i -> restHooks.get(i).index, i -> restHooks.get(i)));
        this.port = port;
    }

    public void start() {
        //Set the operating port
        port(port);

        // default get
        get("/", (req, res) -> "Test Webhook client api running.");

        // get logs
        get("/logs", (req, res) -> {
            res.status(200);
            return listToMultiLineString(logs);
        });

        // get received webhook bodies
        get("/received_hooks", (req, res) -> {
            res.status(200);
            return listToMultiLineString(messages);
        });

        post("/hooks/:id", (req, res) -> {
            logs.add(listToMultiLineString(req.headers().stream().map(x -> x).collect(Collectors.toList())));
            String id = req.params(":id");
            RestHook hook;
            if (hooks.containsKey(id)) {
                hook = hooks.get(id);
            } else {
                hook = new RestHook(id);
            }
            return hook.handleHookMessage(req, res, messages, logs, hooks, restHookRepository, handshakeKey);
        });
    }

    private static String listToMultiLineString(List<String> list) {
        String result = "[";
        for (String item : list) {
            result += "\"" + item + "\"";
            result += ",";
        }
        return result.substring(0, result.length() > 0 ? result.length() - 1 : 0) + "]";
    }
}

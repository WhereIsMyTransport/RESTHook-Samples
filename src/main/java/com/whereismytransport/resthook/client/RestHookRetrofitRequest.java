package com.whereismytransport.resthook.client;


import java.util.UUID;

/**
 * This class is the body which is required to be present in all RESTHook creation
 * requests
 * */

public class RestHookRetrofitRequest {

    public String callbackUrl;
    public String description;
    public String handshakeKey;

    /**
     * @param description A human readable description of what this hook is for (e.g. "Post to Twitter")
     */
    public RestHookRetrofitRequest(String description) {
        this.description = description;
        this.handshakeKey= UUID.randomUUID().toString();
    }
}

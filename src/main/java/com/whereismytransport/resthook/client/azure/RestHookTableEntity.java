package com.whereismytransport.resthook.client.azure;

import com.microsoft.azure.storage.table.TableServiceEntity;
import java.net.URLEncoder;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

/**
 * Created by Nick Cuthbert on 25/04/2016.
 */
public class RestHookTableEntity extends TableServiceEntity {

    public static final String restHookPartitionKey ="ClientSecrets";
    public String secret;
    public String index;

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public static String getRestHookPartitionKey() {
        return restHookPartitionKey;
    }



    public RestHookTableEntity(){}

    public RestHookTableEntity(String index, String secret){
        this.secret = secret;
        this.partitionKey=RestHookTableEntity.restHookPartitionKey;
        this.index=index;
        try {
            this.rowKey= URLEncoder.encode(index,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }



    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

}

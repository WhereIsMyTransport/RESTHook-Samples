package com.whereismytransport.resthook.client.azure;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.ResultContinuation;
import com.microsoft.azure.storage.ResultSegment;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.CloudTableClient;
import com.microsoft.azure.storage.table.TableOperation;
import com.microsoft.azure.storage.table.TableQuery;
import com.whereismytransport.resthook.client.RestHook;
import com.whereismytransport.resthook.client.RestHookRepository;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nick Cuthbert on 25/04/2016.
 */
public class AzureRestHookRepository implements RestHookRepository {

    protected CloudTableClient tableClient;

    private final static String tableName = "JavaTestClientTable";
    private final String storageConnectionString;
    private List<String> logs;
    private final String clientBaseUrl;

    public AzureRestHookRepository(String storageConnectionString, String clientBaseUrl,List<String> logs){
        this.storageConnectionString=storageConnectionString;
        this.clientBaseUrl=clientBaseUrl;
        this.logs=logs;
    }
    private CloudTable getTable() throws URISyntaxException, StorageException {

        CloudTable tableReference = tableClient.getTableReference(tableName);
        tableReference .createIfNotExists();
        return tableReference;
    }
    public boolean initialize(List<String> logs, List<String> restHookBodies){
        try {
            CloudStorageAccount account = CloudStorageAccount.parse(storageConnectionString);
            tableClient=account.createCloudTableClient();
            getTable();
                return true;
        } catch (StorageException | InvalidKeyException | URISyntaxException e) {
            for (StackTraceElement stackElement:e.getStackTrace()) {
                logs.add(stackElement.toString());
            }
            return false;
        }
    }

    @Override
    public boolean addOrReplaceRestHook(RestHook hook) {
        try {
            RestHookTableEntity entity = new RestHookTableEntity(hook.index,hook.secret);
            getTable().execute(TableOperation.insertOrReplace(entity));
            return true;
        } catch (StorageException|URISyntaxException  e) {
            for (StackTraceElement stackElement:e.getStackTrace()) {
                logs.add(stackElement.toString());
            }
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<RestHook> getRestHooks() {
        String partitionFilter= TableQuery.generateFilterCondition("PartitionKey", TableQuery.QueryComparisons.EQUAL,RestHookTableEntity.restHookPartitionKey);
        TableQuery<RestHookTableEntity> query=TableQuery.from(RestHookTableEntity.class).where(partitionFilter);

        ArrayList<RestHook> results=new ArrayList<>();
        try {
            for(RestHookTableEntity result:getTable().execute(query)){
                    results.add(new RestHook(result.index,result.secret));
            }
        } catch (URISyntaxException | StorageException e) {
            for (StackTraceElement stackElement:e.getStackTrace()) {
                logs.add(stackElement.toString());
            }
            e.printStackTrace();
        }
        return results;
    }
}

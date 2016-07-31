import com.whereismytransport.resthook.client.RestHookTestApi;
import com.whereismytransport.resthook.client.RestHookRepository;
import com.whereismytransport.resthook.client.azure.AzureRestHookRepository;
import spark.servlet.SparkApplication;

import java.util.ArrayList;

public class Startup implements SparkApplication{

    private static RestHookRepository repository;
    private static RestHookTestApi restHookTestApi;
    private static ArrayList<String> logs= new ArrayList<>();
    private static ArrayList<String> messages=new ArrayList<> ();
    public static void main(String [] args){
        repository=new AzureRestHookRepository(RoleEnvironment.azureStorageConnectionString,RoleEnvironment.url,logs);
        repository.initialize(logs,messages);
        restHookTestApi = new RestHookTestApi(RoleEnvironment.port, RoleEnvironment.url,repository, logs,messages,RoleEnvironment.handshakeSecret);
        restHookTestApi.start();
    }



    // Method automatically called by Java Host (e.g. Jetty or Tomcat)
    @Override
    public void init() {

        int port=RoleEnvironment.port;

        if(System.getenv().containsKey("HTTP_PLATFORM_PORT")){
            port = Integer.parseInt(System.getenv("HTTP_PLATFORM_PORT"));
        }else{
            port = RoleEnvironment.port;
        }
        String url=RoleEnvironment.url;
        
        repository=new AzureRestHookRepository(RoleEnvironment.azureStorageConnectionString,url,logs);
        repository.initialize(logs,messages);
        restHookTestApi = new RestHookTestApi(port, url,repository,logs,messages,RoleEnvironment.handshakeSecret);
        restHookTestApi.start();
    }

}

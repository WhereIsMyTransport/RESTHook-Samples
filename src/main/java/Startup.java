import com.whereismytransport.resthook.client.RESTHookTestApi;
import com.whereismytransport.resthook.client.RestHookRepository;
import com.whereismytransport.resthook.client.azure.AzureRestHookRepository;
import spark.servlet.SparkApplication;

import java.util.ArrayList;

public class Startup implements SparkApplication{
    private static RestHookRepository repository;
    private static RESTHookTestApi restHookTestApi;
    private static ArrayList<String> logs= new ArrayList<>();
    private static ArrayList<String> messages=new ArrayList<> ();
    public static void main(String [] args){
        String url="http://localhost:4567/";
        int port=4567;
        String connectionString="UseDevelopmentStorage=true";
        if(args.length==3){
            port=Integer.parseInt(args[0]);
            url=args[1];
            connectionString=args[2];
        }
        repository=new AzureRestHookRepository(RoleEnvironment.azureStorageConnectionString);
        repository.initialize(logs,messages);
        restHookTestApi = new RESTHookTestApi(Integer.parseInt(args[0]), "http://localhost:4567/",repository, RoleEnvironment.clientId,RoleEnvironment.clientSecret);
        restHookTestApi.start();
    }



    // Method automatically called by Java Host (e.g. Jetty or Tomcat)
    @Override
    public void init() {
        repository=new AzureRestHookRepository(RoleEnvironment.azureStorageConnectionString);
        repository.initialize(logs,messages);
        int port=0;

        if(System.getenv().containsKey("HTTP_PLATFORM_PORT")){
            port = Integer.parseInt(System.getenv("HTTP_PLATFORM_PORT"));
        }else{
            port = Integer.parseInt(RoleEnvironment.port);
        }
        String url=RoleEnvironment.url;
        if(System.getenv().containsKey("WEBSITE_SITE_NAME")){
            url= System.getenv("WEBSITE_SITE_NAME");
        }

        restHookTestApi = new RESTHookTestApi(port, url,repository,RoleEnvironment.clientId,RoleEnvironment.clientSecret);
        restHookTestApi.start();
    }

}

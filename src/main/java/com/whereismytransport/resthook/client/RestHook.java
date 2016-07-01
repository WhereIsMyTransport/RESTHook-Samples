package com.whereismytransport.resthook.client;

import com.whereismytransport.resthook.client.auth.ClientCredentials;
import com.whereismytransport.resthook.client.auth.Token;
import com.whereismytransport.resthook.client.auth.TokenService;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import spark.Request;

import java.io.InputStreamReader;
import java.util.List;
import java.util.UUID;

/**
 * Created by Nick Cuthbert on 25/04/2016.
 */
public class RestHook {
    private TokenService tokenService = TokenService.retrofit.create(TokenService.class);
    private CaptainHookApiService restHookService = CaptainHookApiService.retrofit.create(CaptainHookApiService.class);
    private String clientUrl;
    public String serverUrl;
    public String serverRelativeUrl;
    public String index;

    public String secret;

    public RestHook(String serverUrl,
                    String serverRelativeUrl,
                    String clientUrl) {

        this.serverUrl = serverUrl;
        this.clientUrl=clientUrl;
        this.serverRelativeUrl = serverRelativeUrl;
        this.index=UUID.randomUUID().toString();
    }

    public RestHook(String serverUrl,
                    String serverRelativeUrl,
                    String secret,
                    String index,
                    String clientUrl)
    {
        this.serverUrl=serverUrl;
        this.serverRelativeUrl=serverRelativeUrl;
        this.secret=secret;
        this.index=index;
        this.clientUrl=clientUrl;
    }

    public spark.Response handleHookMessage(Request req, spark.Response res, List<String> messages,List<String> logs) {
        if(req.headers().stream().anyMatch(x->x.toLowerCase().equals("x-hook-secret"))){
            res.status(200);
            secret=req.headers("x-hook-secret");
            res.header("x-hook-secret",secret);
            return res;
        }
        else if(req.headers().stream().anyMatch(x->x.toLowerCase().equals("x-hook-signature"))){
            String body = req.body();
            messages.add(body);
            String xHookSignature = req.headers("x-hook-signature");
            try {
                if (HmacUtilities.validBody(this, body, xHookSignature)) {
                    res.status(200); //OK
                } else {
                    res.status(403); //Access denied
                    String responseMessage = "Access denied: X-Hook-Signature does not match the secret.";
                    logs.add(responseMessage);
                    res.body(responseMessage);
                }
            } catch (Exception e) {
                for (StackTraceElement stackElement:e.getStackTrace()) {
                    logs.add(stackElement.toString());
                }
                String responseMessage = "Exception occurred encoding hash: " + e.getStackTrace().toString();
                res.status(500); //Internal server error
                return res;
            }
        } else {
            res.status(403);
            String responseMessage = "Access denied: X-Hook-Signature or X-Hook-Secret is not present in headers.";
            logs.add(responseMessage);
            res.body(responseMessage);
        }
        return res;
    }

    public spark.Response createHook(RestHookRetrofitRequest request, ClientCredentials clientCredentials, List<String> logs, spark.Response sparkResponse) {
        try {
                String relativeCallbackUrl = "hooks/" +index ;
                request.callbackUrl=clientUrl + relativeCallbackUrl;

                // Get token to connect to CaptainHook
                Call<Token> getTokenCall = tokenService.createToken(clientCredentials.identityUrl, clientCredentials.getMap());

                Response<Token> tokenResponse = getTokenCall.execute();

                if (tokenResponse.isSuccessful()) {
                    Token token = tokenResponse.body();
                    logs.add("Input url: " +serverUrl+serverRelativeUrl);
                    logs.add("Token: " +token.access_token);
                    Call<ResponseBody> createHookCall;
                    if (request instanceof ChannelRestHookRetrofitRequest){
                        createHookCall = restHookService.createRestHook(serverUrl+serverRelativeUrl,
                                (ChannelRestHookRetrofitRequest)request, "Bearer " + token.access_token);
                    }else{
                        createHookCall = restHookService.createRestHook(serverUrl+serverRelativeUrl,
                                request, "Bearer " + token.access_token);
                    }

                    Response<ResponseBody> createHookCallResponse = createHookCall.execute();
                    if(createHookCallResponse.isSuccessful()) {
                        sparkResponse.status(200);
                        sparkResponse.body(createHookCallResponse.body().string());
                        logs.add("Successfully created web hook");
                    }else {
                        String body=createHookCallResponse.errorBody().string();
                        sparkResponse.status(createHookCallResponse.code());
                        sparkResponse.body(body);
                        logs.add("Something went wrong calling web hook setup. Response code: " + createHookCallResponse.code()+
                                ", Message"+createHookCallResponse.message()+", Body"+createHookCallResponse.body()+", Error Body: "+body
                        );

                    }
                }else {
                    sparkResponse.status(401);
                    sparkResponse.body("Couldn't get token: "+tokenResponse.errorBody());
                    logs.add("Couldn't get token. Response Code:" +tokenResponse.code()+", Message: "+tokenResponse.message());
                }
            }catch (Exception e) {
                e.getStackTrace();
                String response=e.getMessage();
                for (StackTraceElement stackElement:e.getStackTrace()) {
                    logs.add(stackElement.toString());
                    response+=stackElement.toString()+"\n";

                }
                sparkResponse.status(500);
                sparkResponse.body(response);
            }
            return sparkResponse;
        }
    }


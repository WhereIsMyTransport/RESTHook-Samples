/**
 * This sample was created for demonstrative purposes only.
 *   
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
 
package com.whereismytransport.Webhook.client;

import com.whereismytransport.Webhook.client.auth.Token;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import spark.Request;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Webhook {

    public String index;
    public String secret;

    public Webhook(String index) {
        this.index = index;
    }

    public Webhook(String index, String hmacSecret) {        
        this.index = index;
        this.secret = hmacSecret;
    }


    public spark.Response handleHookMessage(Request req, spark.Response res, Map<String, Webhook> hooks, WebhookRepository repository, String handshakeKey) {
        try {
            if (req.headers().stream().anyMatch(x -> x.toLowerCase().equals("x-hook-signature"))) {
                String body = req.body();                
                String xHookSignature = req.headers("x-hook-signature");
                try {
                    //Validate body against secret to verify its authenticity
                    if (HmacUtilities.validBody(this, body, xHookSignature)) {
                        messages.add(body);
                        res.status(200); //OK
                    } else {
                        res.status(403); //Access denied
                        String responseMessage = "Access denied: X-Hook-Signature does not match the secret.";                        
                        res.body(responseMessage);
                    }
                } catch (Exception e) {
                    String responseMessage = "Exception occurred encoding hash: " + e.getStackTrace().toString();
                    res.status(500); //Internal server error
                    return res;
                }
            } else if (req.headers().stream().anyMatch(x -> x.toLowerCase().equals("x-hook-secret"))) {
                res.status(200);
                String handshake = req.headers("x-hook-handshake");
                if (!handshakeKey.equals(handshake)) {
                    res.status(403); //Access denied
                    String responseMessage = "Access denied: x-hook-handshake does not match handshake key.";                    
                    res.body(responseMessage);
                }
                //Secret must be stored so as to later allow HMAC validation
                secret = req.headers("x-hook-secret");
                res.header("x-hook-secret", secret);                                
                repository.addOrReplaceWebhook(this);
                hooks.put(index, this);
                return res;
            } else {
                res.status(403);
                String responseMessage = "Access denied: X-Hook-Signature or X-Hook-Secret is not present in headers.";            
                res.body(responseMessage);
            }

            return res;
        } catch (Exception e) {
            //Something bad has happened            
            throw e;
        }
    }

}


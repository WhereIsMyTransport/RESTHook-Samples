/**
 * This sample was created for demonstrative purposes only.
 *   
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.whereismytransport.Webhook.client;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static spark.Spark.*;


public class WebhookTestApi {

    private WebhookRepository webhookRepository;
    private Map<String, Webhook> hooks;
    private String handshakeKey;
    private int port;


    public WebhookTestApi(int port, String baseUrl, WebhookRepository webhookRepository, String handshakeKey) {        
        this.handshakeKey = handshakeKey;        
        this.webhookRepository = webhookRepository;
        List<Webhook> webhooks = WebhookRepository.getWebhooks();
        //Map to hash map
        hooks = IntStream.range(0, webhooks.size()).boxed().collect(Collectors.toMap(i -> webhooks.get(i).index, i -> webhooks.get(i)));         
        this.port = port;
    }

    public void start() {
        //Set the operating port
        port(port);

        // default get
        get("/", (req, res) -> "Test Webhook client api running.");

        // Handle webhook here
        post("/hooks/:id", (req, res) -> {
            String id = req.params(":id");
            Webhook hook;
            if (hooks.containsKey(id)) {
                hook = hooks.get(id);
            } else {
                hook = new Webhook(id);
            }
            return hook.handleHookMessage(req, res, hooks, webhookRepository, handshakeKey);
        });
    } 
}

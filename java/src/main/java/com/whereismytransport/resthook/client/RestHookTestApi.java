/**
 * This sample was created for demonstrative purposes only.
 *   
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.whereismytransport.resthook.client;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static spark.Spark.*;


public class RestHookTestApi {

    private RestHookRepository restHookRepository;
    private Map<String, RestHook> hooks;
    private String handshakeKey;
    private int port;


    public RestHookTestApi(int port, String baseUrl, RestHookRepository restHookRepository, String handshakeKey) {        
        this.handshakeKey = handshakeKey;        
        this.restHookRepository = restHookRepository;
        List<RestHook> restHooks = restHookRepository.getRestHooks();
        //Map to hash map
        hooks = IntStream.range(0, restHooks.size()).boxed().collect(Collectors.toMap(i -> restHooks.get(i).index, i -> restHooks.get(i)));         
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
            RestHook hook;
            if (hooks.containsKey(id)) {
                hook = hooks.get(id);
            } else {
                hook = new RestHook(id);
            }
            return hook.handleHookMessage(req, res, hooks, restHookRepository, handshakeKey);
        });
    } 
}

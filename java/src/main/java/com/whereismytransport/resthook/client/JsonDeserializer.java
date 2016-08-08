/**
 * This sample was created for demonstrative purposes only.
 *   
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.whereismytransport.resthook.client;

import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ncthbrt on 2016/05/13.
 */
public class JsonDeserializer {

    private static final Gson GSON = new Gson();

    public static <T> T convert(String jsonBody, Class<T> type, List<String> logs) {    
        try {
            return GSON.fromJson(jsonBody, type);
        }
        catch (Exception e) {
            logs.add(e.getMessage());
            for (StackTraceElement el: e.getStackTrace()) {
                logs.add(el.toString());
            }
            return null;
        }
    }


}

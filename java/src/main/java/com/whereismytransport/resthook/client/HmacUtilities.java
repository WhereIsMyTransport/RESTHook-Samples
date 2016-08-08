/**
 * This sample was created for demonstrative purposes only.
 *   
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
 
 package com.whereismytransport.resthook.client;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;

public class HmacUtilities {

    /**
     * @param key the secret which was supplied by the server upon the intial RESTHook handshake
     * @param data a UTF-8 => base64 string encoded representation of the request body
     * @return
     * @throws Exception
     */
    private static String encode(byte[] key, byte[] data) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key, sha256_HMAC.getAlgorithm());
        sha256_HMAC.init(secret_key);
        return DatatypeConverter.printBase64Binary(sha256_HMAC.doFinal(data));
    }

    public static boolean validBody(RestHook hook, String body,String xHookSignature){
        try {
            byte[] bodyBytes = body.getBytes("UTF-8");
            String encodedHash=encode(DatatypeConverter.parseBase64Binary(hook.secret), bodyBytes);
            return encodedHash.equals(xHookSignature);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}

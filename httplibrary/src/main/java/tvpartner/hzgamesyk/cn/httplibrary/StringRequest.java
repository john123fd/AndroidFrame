package tvpartner.hzgamesyk.cn.httplibrary;

import org.apache.http.HttpResponse;

import java.io.IOException;
import java.util.Map;

/**
 * Created by zhanghuan on 2016/3/17.
 */
public abstract class StringRequest<T> extends Request<T> {
    private static String msCookie = null;

    public StringRequest(int method, String url, Listener<T> listener) {
        super(method, url, listener);
        if (msCookie != null && msCookie.length() > 0) {
            addHeader("Cookie", msCookie);
        }
    }

    @Override
    protected abstract Response<T> parseNetworkResponse(NetworkResponse response);

    @Override
    public byte[] handleResponse(HttpResponse response, Delivery delivery) throws IOException, ServerError {
        String cookie = HttpUtils.getHeader(response, "Set-Cookie");
        if (cookie != null && cookie.length() > 0) {
            msCookie = cookie;
        }
        return super.handleResponse(response, delivery);
    }

    public static String formatUrl(String requestUri, Map<String, String> requestArg) {
        StringBuilder stringBuilder = new StringBuilder();
        if (requestArg != null && !requestArg.isEmpty()) {
            for (Map.Entry<String, String> entry : requestArg.entrySet()) {
                stringBuilder.append(stringBuilder.length() == 0 ? "?" : "&");
                stringBuilder.append(entry.getKey()).append("=").append(entry.getValue());
            }
        }
        return stringBuilder.insert(0, requestUri).toString();
    }

    public static String formatUrl(String requestUrl, String requestUri, Map<String, String> requestArg) {
        StringBuilder stringBuilder = new StringBuilder();
        if (requestArg != null && !requestArg.isEmpty()) {
            for (Map.Entry<String, String> entry : requestArg.entrySet()) {
                stringBuilder.append(stringBuilder.length() == 0 ? "?" : "&");
                stringBuilder.append(entry.getKey()).append("=").append(entry.getValue());
            }
        }
        return stringBuilder.insert(0, requestUri).insert(0, requestUrl).toString();
    }
}

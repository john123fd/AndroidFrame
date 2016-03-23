package tvpartner.hzgamesyk.cn.httplibrary.request;

import android.os.Handler;
import android.os.Looper;

import tvpartner.hzgamesyk.cn.httplibrary.NetworkResponse;
import tvpartner.hzgamesyk.cn.httplibrary.Request;
import tvpartner.hzgamesyk.cn.httplibrary.Response;
import tvpartner.hzgamesyk.cn.httplibrary.cache.DiskCache;

/**
 * Created by zhanghuan on 2016/3/17.
 */
public class ClearCacheRequest extends Request<Object> {
    private final DiskCache mCache;
    private final Runnable mCallback;

    public ClearCacheRequest(DiskCache cache, Runnable callback) {
        super(Method.GET, null, null);
        mCache = cache;
        mCallback = callback;
    }

    @Override
    public boolean isCanceled() {
        mCache.clearCache();
        if (mCallback != null) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postAtFrontOfQueue(mCallback);
        }
        return true;
    }

    @Override
    public Priority getPriority() {
        return Priority.IMMEDIATE;
    }

    @Override
    protected Response<Object> parseNetworkResponse(NetworkResponse response) {
        return null;
    }
}

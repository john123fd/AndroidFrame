package tvpartner.hzgamesyk.cn.httplibrary;

import android.content.Context;
import android.content.res.AssetManager;
import android.widget.ImageView;

import org.apache.http.protocol.HTTP;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import tvpartner.hzgamesyk.cn.httplibrary.RequestQueue.RequestFilter;
import tvpartner.hzgamesyk.cn.httplibrary.cache.BitmapImageCache;
import tvpartner.hzgamesyk.cn.httplibrary.cache.DiskCache;
import tvpartner.hzgamesyk.cn.httplibrary.image.NetworkImageView;
import tvpartner.hzgamesyk.cn.httplibrary.request.ClearCacheRequest;
import tvpartner.hzgamesyk.cn.httplibrary.request.ImageRequest;
import tvpartner.hzgamesyk.cn.httplibrary.stack.HurlStack;
import tvpartner.hzgamesyk.cn.httplibrary.toolbox.BasicNetwork;
import tvpartner.hzgamesyk.cn.httplibrary.toolbox.FileDownloader;
import tvpartner.hzgamesyk.cn.httplibrary.toolbox.ImageLoader;

public final class AsyncHttp {
    private static final String HTTP_USERAGNET = "Android";
    private static final String HTTP_DISKCACHE = "volley";
    private static final int HTTP_MEMORYCACHE_SIZE = 10 * 1024 * 1024; // 20MB
    private static final int HTTP_DISKCACHE_SIZE = 100 * 1024 * 1024; // 50MB
    private static final int HTTP_MAX_THREADPOOL = 5;
    private static final int HTTP_MAX_DOWNLOADS = 1;
    private static final String RES_ASSETS = "ASSETS:";
    private static final String RES_SDCARD = "SDCARD:";
    private static final String RES_HTTP = "HTTP:";
    private static AsyncHttp msInstance;
    private static Context msContext;
    private static DiskCache mCache;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private FileDownloader mDownloader;

    private AsyncHttp(Context tContext) {
        final AssetManager mAssetManager = tContext.getAssets();
        Network network = new BasicNetwork(new HurlStack(HTTP_USERAGNET, null),
                HTTP.UTF_8);
        mCache = new DiskCache(
                new File(tContext.getCacheDir(), HTTP_DISKCACHE),
                HTTP_DISKCACHE_SIZE);
        mRequestQueue = new RequestQueue(network, HTTP_MAX_THREADPOOL, mCache);
        mImageLoader = new ImageLoader(mRequestQueue, new BitmapImageCache(
                HTTP_MEMORYCACHE_SIZE)) {

            @Override
            public ImageRequest buildRequest(String requestUrl, int maxWidth,
                                             int maxHeight) {
                ImageRequest request;
                if (requestUrl.startsWith(RES_ASSETS)) {
                    request = new ImageRequest(requestUrl.substring(RES_ASSETS
                            .length()), maxWidth, maxHeight) {
                        @Override
                        public NetworkResponse perform() {
                            try {
                                return new NetworkResponse(
                                        toBytes(mAssetManager.open(getUrl())),
                                        HTTP.UTF_8);
                            } catch (IOException e) {
                                return new NetworkResponse(new byte[1],
                                        HTTP.UTF_8);
                            }
                        }
                    };
                } else if (requestUrl.startsWith(RES_SDCARD)) {
                    request = new ImageRequest(requestUrl.substring(RES_SDCARD
                            .length()), maxWidth, maxHeight) {
                        @Override
                        public NetworkResponse perform() {
                            try {
                                return new NetworkResponse(
                                        toBytes(new FileInputStream(getUrl())),
                                        HTTP.UTF_8);
                            } catch (IOException e) {
                                return new NetworkResponse(new byte[1],
                                        HTTP.UTF_8);
                            }
                        }
                    };
                } else {
                    request = new ImageRequest(requestUrl, maxWidth, maxHeight);
                }
                return request;
            }
        };
        mDownloader = new FileDownloader(mRequestQueue, HTTP_MAX_DOWNLOADS);
        mRequestQueue.start();
    }

    public static byte[] toBytes(InputStream inputStream) {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] data = new byte[10000];
        int count = -1;
        try {
            while ((count = inputStream.read(data, 0, 10000)) != -1)
                outStream.write(data, 0, count);
            data = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outStream.toByteArray();
    }

    public static synchronized void setContext(Context tContext) {
        if (msInstance != null) {
            throw new IllegalStateException("Context class was " + msContext
                    + " but is trying to be reset to " + tContext);
        }
        msContext = tContext;
        msInstance = new AsyncHttp(msContext);
    }

    public static synchronized AsyncHttp getInstance() {
        if (msInstance == null) {
            throw new IllegalStateException(
                    "Call setContext(Context) to create AsyncHttp instance first!!!");
        }
        return msInstance;
    }

    public DiskCache getCache() {
        return mCache;
    }

    /**
     * 获取缓存数据
     *
     * @param url
     *            哪条url的缓存
     * @return
     */
    public byte[] getCache(String url) {
        DiskCache.Entry entry = mCache.getEntry(url);
        if (entry != null) {
            return entry.data;
        } else {
            return new byte[0];
        }
    }

    /**
     * 获取缓存数据
     *
     * @param url
     *           哪条url的缓存
     * @param requestArgs
     *           http请求中的参数集(Http的缓存会连同请求参数一起作为一个缓存的key)
     */
    public byte[] getCache(String url, Map<String, String> requestArgs) {
        url = formatUrl(url, requestArgs);
        return getCache(url);
    }

    /**
     * 只有你确定cache是一个String时才可以使用这个方法，否则还是应该使用getCache(String);
     *
     * @param url
     *            url
     * @param requestArgs
     *            http请求中的参数集(KJHttp的缓存会连同请求参数一起作为一个缓存的key)
     */
    public String getStringCache(String url, Map<String, String> requestArgs) {
        url = formatUrl(url, requestArgs);
        return new String(getCache(url));
    }

    /**
     * 只有你确定cache是一个String时才可以使用这个方法，否则还是应该使用getCache(String);
     *
     * @param url
     * @return
     */
    public String getStringCache(String url) {
        return new String(getCache(url));
    }

    public long getCacheSize() {
        return mCache.getSize();
    }

    public void clearCache(Runnable callback) {
        addRequest(new ClearCacheRequest(mCache, callback));
    }

    public void setImageUri(ImageView imageView, String remoteUri) {
        ImageLoader.ImageListener listener = ImageLoader.getImageListener(
                imageView, 0, 0);
        mImageLoader.get(remoteUri, listener, 0, 0);
    }

    public void setImageUri(ImageView imageView, String remoteUri,
                            int defaultImageResId, int errorImageResId) {
        ImageLoader.ImageListener listener = ImageLoader.getImageListener(
                imageView, defaultImageResId, errorImageResId);
        mImageLoader.get(remoteUri, listener, 0, 0);
    }

    public void setImageUri(ImageView imageView, String remoteUri,
                            int defaultImageResId, int errorImageResId, int maxWidth,
                            int maxHeight) {
        ImageLoader.ImageListener listener = ImageLoader.getImageListener(
                imageView, defaultImageResId, errorImageResId);
        mImageLoader.get(remoteUri, listener, maxWidth, maxHeight);
    }

    public void setImageUri(NetworkImageView imageView, String remoteUri) {
        imageView.setImageUrl(remoteUri, mImageLoader);
    }

    public void setImageUri(NetworkImageView imageView, String remoteUri,
                            int defaultImageResId, int errorImageResid) {
        imageView.setDefaultImageResId(defaultImageResId);
        imageView.setErrorImageResId(errorImageResid);
        imageView.setImageUrl(remoteUri, mImageLoader);
    }

    public static String formatUrl(String requestUri,
                                   Map<String, String> requestArgs) {
        StringBuilder stringBuilder = new StringBuilder();
        if (requestArgs != null && !requestArgs.isEmpty()) {
            for (Map.Entry<String, String> entry : requestArgs.entrySet()) {
                stringBuilder.append(stringBuilder.length() == 0 ? "?" : "&");
                stringBuilder.append(entry.getKey()).append("=")
                        .append(entry.getValue());
            }
        }
        return stringBuilder.insert(0, requestUri).toString();
    }

    public static String formatUrl(String requestUrl, String requestUri,
                                   Map<String, String> requestArgs) {
        StringBuilder stringBuilder = new StringBuilder();
        if (requestArgs != null && !requestArgs.isEmpty()) {
            for (Map.Entry<String, String> entry : requestArgs.entrySet()) {
                stringBuilder.append(stringBuilder.length() == 0 ? "?" : "&");
                stringBuilder.append(entry.getKey()).append("=")
                        .append(entry.getValue());
            }
        }
        return stringBuilder.insert(0, requestUri).insert(0, requestUrl)
                .toString();
    }

    public FileDownloader getDownloader() {
        return mDownloader;
    }

    public FileDownloader.DownloadController addDownload(String remoteUrl,
                                                         String localPath, Listener<Void> listener) {
        return mDownloader.add(localPath, remoteUrl, listener);
    }

    public void cancelAll(RequestFilter filter) {
        mRequestQueue.cancelAll(filter);
    }

    public void cancelAll(final Object tag) {
        mRequestQueue.cancelAll(tag);
    }

    public Request<?> addRequest(Request<?> request) {
        return mRequestQueue.add(request);
    }

    public Request<?> addRequest(Request<?> request, TimeUnit timeUnit,
                                 int amountTime) {
        request.setCacheExpireTime(timeUnit, amountTime);
        return mRequestQueue.add(request);
    }

    public Request<?> addRequest(final Object tag, Request<?> request) {
        request.setTag(tag);
        return mRequestQueue.add(request);
    }

    public Request<?> addRequest(final Object tag, Request<?> request,
                                 TimeUnit timeUnit, int amountTime) {
        request.setCacheExpireTime(timeUnit, amountTime);
        request.setTag(tag);
        return mRequestQueue.add(request);
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }
}



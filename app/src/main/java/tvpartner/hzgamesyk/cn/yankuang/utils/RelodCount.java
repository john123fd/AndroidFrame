package tvpartner.hzgamesyk.cn.yankuang.utils;

import android.content.Context;

import java.io.File;

import tvpartner.hzgamesyk.cn.httplibrary.AsyncHttp;
import tvpartner.hzgamesyk.cn.httplibrary.Listener;
import tvpartner.hzgamesyk.cn.httplibrary.NetroidError;
import tvpartner.hzgamesyk.cn.httplibrary.Request;
import tvpartner.hzgamesyk.cn.httplibrary.request.StringRequest;
import tvpartner.hzgamesyk.cn.httplibrary.toolbox.FileDownloader;
import tvpartner.hzgamesyk.cn.yankuang.AppContext;
import tvpartner.hzgamesyk.cn.yankuang.HttpLink;
import tvpartner.hzgamesyk.cn.yankuang.bean.WelcomeLogo;

/**
 * Created by zhanghuan on 2016/3/17.
 */
public class RelodCount {
    private AsyncHttp asyncHttp = AsyncHttp.getInstance();
    private FileDownloader mDownloader;
    private static RelodCount instance;
    private Context context = AppContext.getInstance();

    public static RelodCount getInstance() {
        if (null == instance) {
            instance = new RelodCount();
        }
        return instance;
    }

    private RelodCount() {
    }

    public void relodIcon() {
        final long versions = context.getSharedPreferences("SAVE",
                Context.MODE_PRIVATE).getLong("versions", 1);
        mDownloader = asyncHttp.getDownloader();
        File localFile0 = new File(context.getFilesDir().getAbsolutePath()
                + "/downloads");
        if (!localFile0.exists()) {
            localFile0.mkdirs();
        }
        final String localPath = context.getFilesDir().getAbsolutePath()
                + "/downloads/welcomeicon";
        asyncHttp.addRequest(this, new StringRequest(Request.Method.GET,
                HttpLink.URI_WELCOME_LOGO, new Listener<String>() {

            @Override
            public void onSuccess(String response) {
                WelcomeLogo logo = ApplicationUtil.fromJson(
                        response.toString(), WelcomeLogo.class);
                if (logo != null) {
                    final long newVersions = logo.mTime;
                    if (newVersions > versions) {
                        String iconUrl = logo.url;
                        if (null != iconUrl && !iconUrl.isEmpty()) {
                            mDownloader.add(localPath + newVersions
                                            + ".jpg", iconUrl,
                                    new Listener<Void>() {

                                        @Override
                                        public void onSuccess(
                                                Void response) {
                                            context.getSharedPreferences(
                                                    "SAVE",
                                                    Context.MODE_PRIVATE)
                                                    .edit()
                                                    .putLong("versions",
                                                            newVersions)
                                                    .commit();
                                            File file = new File(
                                                    localPath
                                                            + versions
                                                            + ".jpg");
                                            if (file.exists()) {
                                                file.delete();
                                            }
                                        }

                                    });
                        }
                    }
                }
            }

            @Override
            public void onError(NetroidError error) {
                // TODO Auto-generated method stub
                super.onError(error);
            }
        }));

    }

    public void relodText() {
        asyncHttp.addRequest(this, new StringRequest(Request.Method.GET,
                HttpLink.ENDPOINT_TVPARTNER + HttpLink.URI_SHARE_TEXT,
                new Listener<String>() {

                    @Override
                    public void onSuccess(String response) {
                        if (null != response && !response.equals("\"\"")) {
                            context.getSharedPreferences("SAVE",
                                    Context.MODE_PRIVATE).edit()
                                    .putString("sharetext", response).commit();
                        }
                    }

                    @Override
                    public void onError(NetroidError error) {
                        super.onError(error);

                    }
                }));
    }
}


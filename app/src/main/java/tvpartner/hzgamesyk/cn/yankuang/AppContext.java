package tvpartner.hzgamesyk.cn.yankuang;

import android.accounts.Account;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.File;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

import butterknife.ButterKnife;
import tvpartner.hzgamesyk.cn.httplibrary.AsyncHttp;
import tvpartner.hzgamesyk.cn.yankuang.base.BaseApplication;
import tvpartner.hzgamesyk.cn.yankuang.bean.AuthenticToken;
import tvpartner.hzgamesyk.cn.yankuang.utils.AuthenticTokenKeeper;
import tvpartner.hzgamesyk.cn.yankuang.utils.MD5Util;
import tvpartner.hzgamesyk.cn.yankuang.utils.RelodCount;
import tvpartner.hzgamesyk.cn.yankuang.utils.StringUtils;

/**
 * Created by zhanghuan on 2016/3/17.
 */
public class AppContext extends BaseApplication {

    public static final int PAGE_SIZE = 20;// 默认分页大小

    private static AppContext instance;

    private int loginUid;

    private boolean login;

    private HashMap<String, Object> map = new HashMap<String, Object>();

    public static final String KEY_FRITST_START = "KEY_FRIST_START";

    public static final String KEY_NIGHT_MODE_SWITCH = "night_mode_switch";

    private AsyncHttp asyncHttp;
    private Account account = null;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        init();
    }

    private void init() {
        // 初始化网络请求
        AsyncHttp.setContext(this);

        ButterKnife.setDebug(BuildConfig.DEBUG);
    }

    /**
     * 获得当前app运行的AppContext
     *
     * @return
     */
    public static AppContext getInstance() {
        return instance;
    }

    public static String getDeviceInfo(Context context) {
        try {
            org.json.JSONObject json = new org.json.JSONObject();
            android.telephony.TelephonyManager tm = (android.telephony.TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            String device_id = tm.getDeviceId();
            android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) context
                    .getSystemService(Context.WIFI_SERVICE);
            String mac = wifi.getConnectionInfo().getMacAddress();
            json.put("mac", mac);
            if (TextUtils.isEmpty(device_id)) {
                device_id = mac;
            }
            if (TextUtils.isEmpty(device_id)) {
                device_id = android.provider.Settings.Secure.getString(
                        context.getContentResolver(),
                        android.provider.Settings.Secure.ANDROID_ID);
            }
            json.put("device_id", device_id);
            return json.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Account getAccount() {
        return account;
    }

    public boolean getIslogin() {
        AuthenticToken authenticToken = AuthenticTokenKeeper
                .readAccessToken(getApplicationContext());
        if (authenticToken.getToken().equals("")) {
            return false;
        } else if (authenticToken.isExpired()) {
            return false;
        } else {
            return true;
        }
    }

    public void outLongin() {
        AuthenticTokenKeeper.clear(getApplicationContext());
        AppContext.getInstance().setIsauto(false);
        AppContext.getInstance().setAccount(null);
        Toast.makeText(this, "登陆信息失效，强制退出登陆", Toast.LENGTH_SHORT).show();
    }

    public String creatUID() {
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String deviceid = tm.getDeviceId();
        if (deviceid == null || deviceid.equals("")) {
            deviceid = Settings.Secure
                    .getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        return MD5Util.getMD5String(deviceid + System.currentTimeMillis());
    }

    public String getUID() {
        String uid = getSharedPreferences("SAVE", Context.MODE_PRIVATE)
                .getString("UID", "");
        if (uid.equals("")) {
            uid = creatUID();
            getSharedPreferences("SAVE", Context.MODE_PRIVATE).edit()
                    .putString("UID", uid).commit();
        }
        return uid;
    }

    public String getUsername() {
        return getSharedPreferences("SAVE", Context.MODE_PRIVATE).getString(
                "name", "");
    }

    public String getPassword() {
        return getSharedPreferences("SAVE", Context.MODE_PRIVATE).getString(
                "password", "");
    }

    public Boolean getIsauto() {
        return getSharedPreferences("auto", Context.MODE_PRIVATE).getBoolean(
                "isauto", false);
    }

    public void setUsername(String username) {
        getSharedPreferences("SAVE", Context.MODE_PRIVATE).edit()
                .putString("name", username).commit();
    }

    public void setPassword(String password) {
        getSharedPreferences("SAVE", Context.MODE_PRIVATE).edit()
                .putString("password", password).commit();
    }

    public void setIsauto(Boolean isauto) {
        getSharedPreferences("auto", Context.MODE_PRIVATE).edit()
                .putBoolean("isauto", isauto).commit();
    }

    public String getShareText() {
        RelodCount.getInstance().relodText();
        return getSharedPreferences("SAVE", Context.MODE_PRIVATE).getString(
                "sharetext", getString(R.string.share_text));
    }

    public String getShareTitle() {
        return getSharedPreferences("SAVE", Context.MODE_PRIVATE).getString(
                "title", getString(R.string.share_title));
    }

    public String getShareUrl() {
        return getSharedPreferences("SAVE", Context.MODE_PRIVATE).getString(
                "url", getString(R.string.share_url));
    }

    public Bitmap getWelcomeIcon() {
        long versions = getSharedPreferences("SAVE", Context.MODE_PRIVATE)
                .getLong("versions", 0);
        String path = getFilesDir().getAbsolutePath()
                + "/downloads/welcomeicon" + versions + ".jpg";
        if (!new File(path).exists()) {
            getSharedPreferences("SAVE", Context.MODE_PRIVATE).edit()
                    .putLong("versions", 0).commit();
            RelodCount.getInstance().relodIcon();
            return BitmapFactory.decodeResource(getResources(),
                    R.drawable.yklogo);
        }
        RelodCount.getInstance().relodIcon();
        return BitmapFactory.decodeFile(path);
    }

    public boolean containsProperty(String key) {
        Properties props = getProperties();
        return props.containsKey(key);
    }

    public void setProperties(Properties ps) {
        AppConfig.getAppConfig(this).set(ps);
    }

    public Properties getProperties() {
        return AppConfig.getAppConfig(this).get();
    }

    public void setProperty(String key, String value) {
        AppConfig.getAppConfig(this).set(key, value);
    }

    /**
     * 获取cookie时传AppConfig.CONF_COOKIE
     *
     * @param key
     * @return
     */
    public String getProperty(String key) {
        String res = AppConfig.getAppConfig(this).get(key);
        return res;
    }

    public void removeProperty(String... key) {
        AppConfig.getAppConfig(this).remove(key);
    }

    /**
     * 获取App唯一标识
     *
     * @return
     */
    public String getAppId() {
        String uniqueID = getProperty(AppConfig.CONF_APP_UNIQUEID);
        if (StringUtils.isEmpty(uniqueID)) {
            uniqueID = UUID.randomUUID().toString();
            setProperty(AppConfig.CONF_APP_UNIQUEID, uniqueID);
        }
        return uniqueID;
    }

    /**
     * 获取App安装包信息
     *
     * @return
     */
    public PackageInfo getPackageInfo() {
        PackageInfo info = null;
        try {
            info = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace(System.err);
        }
        if (info == null)
            info = new PackageInfo();
        return info;
    }


    /**
     * 清除保存的缓存
     */
    public void cleanCookie() {
        removeProperty(AppConfig.CONF_COOKIE);
    }

    /**
     * 清除app缓存
     */
    public void clearAppCache() {

    }


    /**
     * 判断当前版本是否兼容目标版本的方法
     *
     * @param VersionCode
     * @return
     */
    public static boolean isMethodsCompat(int VersionCode) {
        int currentVersion = android.os.Build.VERSION.SDK_INT;
        return currentVersion >= VersionCode;
    }


    public static boolean isFristStart() {
        return getPreferences().getBoolean(KEY_FRITST_START, true);
    }

    public static void setFristStart(boolean frist) {
        set(KEY_FRITST_START, frist);
    }

    public void put(String key, Object object) {
        map.put(key, object);
    }

    public Object get(String key) {
        return map.get(key);
    }
}

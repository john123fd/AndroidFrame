package tvpartner.hzgamesyk.cn.yankuang.utils;

import android.content.Context;
import android.content.SharedPreferences;

import tvpartner.hzgamesyk.cn.yankuang.bean.AuthenticToken;

/**
 * Created by zhanghuan on 2016/3/17.
 */
public final class AuthenticTokenKeeper {
    private static final String PREFERENCES_NAME = "tvpartner_authentic_token";
    private static final String KEY_UID = "uid";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_EXPIRETIME = "expire_time";

    public static void writeAccessToken(Context tContext, AuthenticToken authenticToken) {
        if (tContext == null || authenticToken == null) {
            return;
        }

        SharedPreferences pref = tContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
        SharedPreferences.Editor editor = pref.edit();
        editor.putLong(KEY_UID, authenticToken.getUid());
        editor.putString(KEY_TOKEN, authenticToken.getToken());
        editor.putLong(KEY_EXPIRETIME, authenticToken.getExpireTime());
        editor.commit();
    }

    public static AuthenticToken readAccessToken(Context tContext) {
        if (tContext == null) {
            return null;
        }

        AuthenticToken authenticToken = new AuthenticToken();
        SharedPreferences pref = tContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
        authenticToken.setUid(pref.getLong(KEY_UID, 0));
        authenticToken.setToken(pref.getString(KEY_TOKEN, ""));
        authenticToken.setExpireTime(pref.getLong(KEY_EXPIRETIME, 0));
        return authenticToken;
    }

    public static void clear(Context tContext) {
        if (tContext == null) {
            return;
        }

        SharedPreferences pref = tContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear().commit();
    }
}

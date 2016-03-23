package tvpartner.hzgamesyk.cn.yankuang.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;

import com.alibaba.fastjson.JSON;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class ApplicationUtil {
//	private static final Logger msLogger = LoggerFactory.getLogger(ApplicationUtil.class);
//    private static final Gson msGson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    private static final SimpleDateFormat msFormat = new SimpleDateFormat(
            "yyyy-MM-dd", Locale.getDefault());

    private static final SimpleDateFormat msFormat_ms = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    private static final SimpleDateFormat msFormat_hm = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm", Locale.getDefault());

    public static long stringToDate(String time) throws ParseException {
        return msFormat_ms.parse(time).getTime();
    }

    public static String simpleDateFormat(long time) {
        return msFormat.format(new Date(time));
    }

    public static String simpleDateFormatMs(long time) {
        return msFormat_ms.format(new Date(time));
    }
	/**
	 * Safe create directory if not exist.
	 * @param directoryPath
	 */
	public synchronized static void createPath(String directoryPath) {
		try {
			File localFile = new File(directoryPath);
			if (localFile.exists() && !localFile.isDirectory()) {
				localFile.delete();
			}
			if (!localFile.exists()) {
				localFile.mkdirs();
			}
		} catch (Exception e) {
//			msLogger.error(ApplicationUtil.class.getName() + "#createPath(" + directoryPath + ") error occurred!!!", e);
		}
	}
	
	/**
	 * Set file's permission.
	 * @param permission
	 * @param pathname
	 */
	public static void setPermission(String permission, String pathname) {
		try {
			Runtime.getRuntime().exec("chmod 777 " + pathname + " -R").waitFor();
		} catch (IOException | InterruptedException e) {
//			msLogger.error(ApplicationUtil.class.getName() + "#setPermission(" + permission + ", " + pathname + ") error occurred!!!", e);
		}
	}
	
	/**
	 * Get application's display name specified by the package name.
	 * @param tContext
	 * @param packageName
	 * @return
	 */
	public static String getApplicationName(Context tContext, String packageName) {
		try {
			ApplicationInfo info = tContext.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);
			return tContext.getPackageManager().getApplicationLabel(info).toString();
		} catch (NameNotFoundException e) {
//			msLogger.debug(ApplicationUtil.class.getName() + "#getApplicationName(" + packageName + ") error occurred!!!", e);
			return null;
		}
	}

    /**
     * Get application's version code.
     * @param tContext
     * @return
     */
    public static int getApplicationVersionCode(Context tContext) {
        try {
            PackageInfo info = tContext.getPackageManager().getPackageInfo(tContext.getPackageName(), PackageManager.GET_META_DATA);
            return info.versionCode;
        } catch (NameNotFoundException e) {
//            msLogger.debug(ApplicationUtil.class.getName() + "#getApplicationVersionCode() error occurred!!!", e);
        }
        return 0;
    }

    /**
     * Get application's version code.
     * @param tContext
     * @param packageName
     * @return
     */
    public static int getApplicationVersionCode(Context tContext, String packageName) {
        try {
            PackageInfo info = tContext.getPackageManager().getPackageInfo(packageName, PackageManager.GET_META_DATA);
            return info.versionCode;
        } catch (NameNotFoundException e) {
//            msLogger.debug(ApplicationUtil.class.getName() + "#getApplicationVersionCode(" + packageName + ") error occurred!!!", e);
        }
        return 0;
    }

    /**
     * Get application's version name.
     * @param tContext
     * @return
     */
    public static String getApplicationVersionName(Context tContext) {
        try {
            PackageInfo info = tContext.getPackageManager().getPackageInfo(tContext.getPackageName(), PackageManager.GET_META_DATA);
            return info.versionName;
        } catch (NameNotFoundException e) {
//            msLogger.debug(ApplicationUtil.class.getName() + "#getApplicationVersionName() error occurred!!!", e);
        }
        return "";
    }

    /**
     * Get application's version name.
     * @param tContext
     * @param packageName
     * @return
     */
    public static String getApplicationVersionName(Context tContext, String packageName) {
        try {
            PackageInfo info = tContext.getPackageManager().getPackageInfo(packageName, PackageManager.GET_META_DATA);
            return info.versionName;
        } catch (NameNotFoundException e) {
//            msLogger.debug(ApplicationUtil.class.getName() + "#getApplicationVersionName(" + packageName + ") error occurred!!!", e);
        }
        return "";
    }
	
	/**
	 * Silent close a closable object.
	 * @param o
	 */
	public static void silentClose(Closeable o) {
		if (o != null) {
			try {
				o.close();
			} catch (IOException e) {
//				msLogger.debug(ApplicationUtil.class.getName() + "#slientClose() error occurred!!!", e);
			}
		}
	}
	
	/**
	 * Whether the specified package installed.
	 * @param tContext
	 * @param packageName
	 * @return
	 */
	public static boolean isApplicationAvailable(Context tContext, String packageName) {
		try {
			PackageInfo info = tContext.getPackageManager().getPackageInfo(packageName, 0);
			return info != null;
		} catch (NameNotFoundException e) {
			return false;
		}
	}

    /**
     * Launch application by package name
     */
    public static void launchApp(Context tContext, String packageName) {
        try {
            Intent intent = tContext.getPackageManager().getLaunchIntentForPackage(packageName);
            tContext.startActivity(intent);
        } catch (Exception e) {
//            msLogger.debug(ApplicationUtil.class.getName() + "#launchApp() error occurred!!!", e);
        }
    }

    /**
     * Install application
     */
    public static void installApk(Context tContext, File localFile) {
        if (localFile.getParentFile() != null) {
            setPermission("777", localFile.getParentFile().getAbsolutePath());
        }
        setPermission("777", localFile.getAbsolutePath());
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(localFile), "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        tContext.startActivity(intent);
    }

    /**
     * This method deserializes the specified JSON into an object of the specified class.
     */
    public static <T> T fromJson(String json, Class<T> classOfT) {
        return JSON.parseObject(json, classOfT);
//        return msGson.fromJson(json, classOfT);
    }

    /**
     * This method deserializes the specified JSON into an object of the specified type.
     */
    public static <T> T fromJson(String json, Type typeOfT) {
        return JSON.parseObject(json, typeOfT);
//        return msGson.fromJson(json, typeOfT);
    }

    /**
     * This method serializes the specified object to a string
     */
    public static <T> String toJson(T object) {
        return JSON.toJSONString(object);
    }

    /**
     * This method serializes the specified object to a string of the specified type.
     */
//    public static <T> String toJson(T object, Type typeOfT) {
//        return msGson.toJson(object, typeOfT);
//    }
}

package tvpartner.hzgamesyk.cn.yankuang.base;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import tvpartner.hzgamesyk.cn.yankuang.AppContext;
import tvpartner.hzgamesyk.cn.yankuang.R;
import tvpartner.hzgamesyk.cn.yankuang.interf.I_SkipActivity;
import tvpartner.hzgamesyk.cn.yankuang.utils.StringUtils;

/**
 * Created by zhanghuan on 2016/3/22.
 */
public abstract class BaseActivity extends AppCompatActivity implements Callback, I_SkipActivity {

    public static final String TAG = BaseActivity.class.getSimpleName();

    // Durations for certain animations we use:
    private static final int HEADER_HIDE_ANIM_DURATION = 300;

    private Toolbar mActionBarToolbar;
    private TextView mActionBarTitle;

    protected boolean isDestroy;
    protected FragmentActivity mContext;
    protected AppContext mApplication;
    protected Handler mHandler;
    private Toast toast = null;

    // 统一的加载对话框
    protected ProgressDialog mLoadingDialog;

    // 控制ActionBar自动隐藏
    private ObjectAnimator mStatusBarColorAnimator;
    private boolean mActionBarAutoHideEnabled = false;
    private int mActionBarAutoHideSensivity = 0;
    private int mActionBarAutoHideMinY = 0;
    private int mActionBarAutoHideSignal = 0;
    private boolean mActionBarShown = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(initPageLayoutID());
        ButterKnife.bind(this);
        initActionBar();
        init();
        initView();
        initDate();
        initPageViewListener();
        process(savedInstanceState);
    }

    protected void init() {
        mContext = this;
        mApplication = AppContext.getInstance();
        mHandler = new Handler(this);
    }

    /**
     * 返回主布局id
     */
    protected abstract int initPageLayoutID();

    /**
     * 初始化页面控件
     */
    protected void initView() {
//        onTitleChanged(getTitle(), getTitleColor());
    }

    /**
     * 初始化数据
     */
    protected void initDate() {
    }

    /**
     * 页面控件点击事件处理
     */
    protected void initPageViewListener() {
    }

    /**
     * 逻辑处理
     */
    protected void process(Bundle savedInstanceState) {
    }

    protected void initActionBar() {
        if (getActionBarToolbar() == null) {
            return;
        }

        mActionBarToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        mActionBarToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backOnclick();
            }
        });

        int titleRes = getActionBarTitle();
        mActionBarTitle = (TextView)mActionBarToolbar.findViewById(R.id.toolbar_title);
        if (mActionBarToolbar != null && titleRes != 0 && mActionBarTitle != null) {
            mActionBarTitle.setText(titleRes);
        }

        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    protected Toolbar getActionBarToolbar() {
        if (mActionBarToolbar == null) {
            mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
            if (mActionBarToolbar != null) {
                setSupportActionBar(mActionBarToolbar);
            }
        }
        return mActionBarToolbar;
    }

    protected int getActionBarTitle() {
        return R.string.app_name;
    }

    public void setActionBarTitle(int resId) {
        if (resId != 0) {
            setActionBarTitle(getString(resId));
        }
    }

    public void setActionBarTitle(String title) {
        if (StringUtils.isEmpty(title)) {
            title = getString(R.string.app_name);
        }
        if (mActionBarToolbar != null) {
            if (mActionBarTitle != null) {
                mActionBarTitle.setText(title);
            }
        }
    }

    protected void setBackIcon(int resId) {
        if (mActionBarToolbar != null) {
            mActionBarToolbar.setNavigationIcon(resId);
        }
    }

    protected void backOnclick() {
    }

    public void enableActionBarAutoHide(final ListView listView) {
        initActionBarAutoHide();
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            final static int ITEMS_THRESHOLD = 3;
            int lastFvi = 0;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {
                onMainContentScrolled(firstVisibleItem <= ITEMS_THRESHOLD ? 0 : Integer.MAX_VALUE,
                        lastFvi - firstVisibleItem > 0 ? Integer.MIN_VALUE :
                                lastFvi == firstVisibleItem ? 0 : Integer.MAX_VALUE
                );
                lastFvi = firstVisibleItem;
            }
        });
    }

    private void initActionBarAutoHide() {
        mActionBarAutoHideEnabled = true;
        mActionBarAutoHideMinY =
                getResources().getDimensionPixelSize(R.dimen.action_bar_auto_hide_min_y);
        mActionBarAutoHideSensivity =
                getResources().getDimensionPixelSize(R.dimen.action_bar_auto_hide_sensivity);
    }

    private void onMainContentScrolled(int currentY, int deltaY) {
        if (deltaY > mActionBarAutoHideSensivity) {
            deltaY = mActionBarAutoHideSensivity;
        } else if (deltaY < -mActionBarAutoHideSensivity) {
            deltaY = -mActionBarAutoHideSensivity;
        }

        if (Math.signum(deltaY) * Math.signum(mActionBarAutoHideSignal) < 0) {
            // deltaY is a motion opposite to the accumulated signal, so reset signal
            mActionBarAutoHideSignal = deltaY;
        } else {
            // add to accumulated signal
            mActionBarAutoHideSignal += deltaY;
        }

        boolean shouldShow = currentY < mActionBarAutoHideMinY ||
                (mActionBarAutoHideSignal <= -mActionBarAutoHideSensivity);
        autoShowOrHideActionBar(shouldShow);
    }

    protected void autoShowOrHideActionBar(boolean show) {
        if (show == mActionBarShown) {
            return;
        }

        mActionBarShown = show;
        onActionBarAutoShowOrHide(show);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    protected void onActionBarAutoShowOrHide(boolean shown) {
        Log.d(TAG, "onActionBarAutoShowOrHide " + shown);
        if (mStatusBarColorAnimator != null) {
            mStatusBarColorAnimator.cancel();
        }

        if (shown) {
            mActionBarToolbar.animate().translationY(0).alpha(1)
                    .setDuration(HEADER_HIDE_ANIM_DURATION)
                    .setInterpolator(new DecelerateInterpolator());
        } else {
            mActionBarToolbar.animate().translationY(-mActionBarToolbar.getBottom()).alpha(0)
                    .setDuration(HEADER_HIDE_ANIM_DURATION)
                    .setInterpolator(new DecelerateInterpolator());
        }
    }

    @Override
    protected void onDestroy() {
        isDestroy = true;
        dismissLoadingDialog();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * 显示加载对话框
     *
     * @param msg          消息
     * @param isCancelable 是否可被用户关闭
     */
    public void showLoadingDialog(String msg, boolean isCancelable) {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            return;
        } else {
            mLoadingDialog = new ProgressDialog(mContext);
            mLoadingDialog.setMessage(msg);
            mLoadingDialog.setIndeterminate(true);
            mLoadingDialog.setCancelable(isCancelable);
            mLoadingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mLoadingDialog.show();
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (!isDestroy) {
            performHandleMessage(msg);
        }
        return true;
    }

    /**
     * 接收处理mHandler的消息
     */
    protected void performHandleMessage(Message msg) {

    }

    /**
     * 关闭加载对话框
     */
    public void dismissLoadingDialog() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
            mLoadingDialog = null;
        }
    }

    /**
     * 启动Activity
     */
    public void launchActivity(Class<? extends Activity> cls) {
        startActivity(new Intent(mContext, cls));
    }

    @Override
    public void skipActivity(Activity aty, Class<?> cls) {
        showActivity(aty, cls);
        aty.finish();
    }

    /**
     * skip to @param(cls)，and call @param(aty's) finish() method
     */
    @Override
    public void skipActivity(Activity aty, Intent it) {
        showActivity(aty, it);
        aty.finish();
    }

    /**
     * skip to @param(cls)，and call @param(aty's) finish() method
     */
    @Override
    public void skipActivity(Activity aty, Class<?> cls, Bundle extras) {
        showActivity(aty, cls, extras);
        aty.finish();
    }

    /**
     * show to @param(cls)，but can't finish activity
     */
    @Override
    public void showActivity(Activity aty, Class<?> cls) {
        Intent intent = new Intent();
        intent.setClass(aty, cls);
        aty.startActivity(intent);
    }

    /**
     * show to @param(cls)，but can't finish activity
     */
    @Override
    public void showActivity(Activity aty, Intent it) {
        aty.startActivity(it);
    }

    /**
     * show to @param(cls)，but can't finish activity
     */
    @Override
    public void showActivity(Activity aty, Class<?> cls, Bundle extras) {
        Intent intent = new Intent();
        intent.putExtras(extras);
        intent.setClass(aty, cls);
        aty.startActivity(intent);
    }

    public void toastShow(Context context,String text,int i) {
        if(toast == null)
        {
            toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        }
        else {
            toast.setText(text);
        }
        toast.show();
    }
}

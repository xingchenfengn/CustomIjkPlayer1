package com.xingzhiqiao.xzqplayer.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.util.List;
import java.util.Locale;

public class AndroidTools {

	private static Context appContext;

	/**
	 * 获取版本号
	 * 
	 * @return 当前应用的版本号 android:versionCode="1"
	 */
	public static int getVersionCode(Context context) {
		int version = 0;
		try {
			PackageManager manager = context.getPackageManager();
			PackageInfo info = manager.getPackageInfo(context.getPackageName(),
					0);
			version = info.versionCode;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return version;
	}

	/**
	 * 获取版本名称
	 * 
	 * @return 当前应用的版本名称 android:versionName="1.0.3"
	 */
	public static String getVersionName(Context context) {
		String version = "";
		try {
			PackageManager manager = context.getPackageManager();
			PackageInfo info = manager.getPackageInfo(context.getPackageName(),
					0);
			version = info.versionName;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return version;
	}

	/**
	 * 判断应用程序是否处于前台
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isForegroudApp(Context context) {
		String packageName = context.getPackageName();
		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);

		List<RunningTaskInfo> tasksInfo = activityManager.getRunningTasks(1);
		if (tasksInfo.size() > 0) {
			// 应用程序位于堆栈的顶层
			String topPkgName = tasksInfo.get(0).topActivity.getPackageName();
			if (packageName.equals(topPkgName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 获取系统语言
	 */
	public static String getLanguage(Context context) {
		Locale locale = context.getResources().getConfiguration().locale;
		if (locale == null) {
			return "zh";
		}
		String language = locale.getLanguage();
		if (TextUtils.isEmpty(language)) {
			return "zh";
		}
		return language;
	}

	/**
	 * 获得屏幕参数：主要是分辨率 width * height
	 */
	public static String getDisplay(Context context) {
		String display = getScreenWidth(context) + "*"
				+ getScreenHeight(context);
		;
		return display;
	}

	/**
	 * Get the screen width
	 * 
	 * @author mapeng_thun
	 * @param context
	 * @return
	 */
	public static int getScreenWidth(Context context) {
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);
		return dm.widthPixels;
	}

	/**
	 * Get the screen height
	 * 
	 * @author mapeng_thun
	 * @param context
	 * @return
	 */
	public static int getScreenHeight(Context context) {
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);
		return dm.heightPixels;
	}

	public static void setApplicationContext(Context context) {
		appContext = context.getApplicationContext();
	}

	public static Context getApplicationContext() {
		if (appContext == null) {
			throw new RuntimeException("ApplicationContext not initialized!");
		}
		return appContext;
	}

	public static String getIMEI(Context context) {
		TelephonyManager mTelephonyMgr = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String imei = mTelephonyMgr.getDeviceId();
		return imei;
	}

	public static String getIMSI(Context context) {
		TelephonyManager mTelephonyMgr = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String imsi = mTelephonyMgr.getSubscriberId();
		return imsi;
	}

	/**
	 * 用户的os的sdk的版本是否大于等于指定的版本
	 * 
	 * @param apiLevel
	 * @return
	 */
	public static boolean isCompatibleApiLevel(int apiLevel) {
		return android.os.Build.VERSION.SDK_INT >= apiLevel;
	}

	/**
	 * 显示键盘
	 */
	public static void showKeyboard(Context context, View view) {
		InputMethodManager imm = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
		imm.showSoftInput(view, 0);
		setEtSelection(view);
	}


	/**
	 * 通过模拟点击事件,使View获取焦点,进而弹出软键盘
	 * 
	 * @param view
	 */
	public static void showKeyboardOtherway(final View view) {
		(new Handler()).postDelayed(new Runnable() {

			public void run() {
				// location
				int[] location = new int[2];
				view.getLocationOnScreen(location);
				float xOffset = location[0] + view.getPaddingLeft();
				// 设置EditText光标位置
				if (view instanceof EditText) {
					EditText et = (EditText) view;
					String str = et.getText().toString();
					// 计算画布上的字符串宽度(所占像素宽度)
					Paint paint = new Paint();
					xOffset += paint.measureText(str);
				}

				view.dispatchTouchEvent(MotionEvent.obtain(
						SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
						MotionEvent.ACTION_DOWN, xOffset, 0, 0));
				view.dispatchTouchEvent(MotionEvent.obtain(
						SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
						MotionEvent.ACTION_UP, xOffset, 0, 0));

			}
		}, 200);

	}

	/**
	 * 隐藏键盘
	 */
	public static void hideKeyboard(Context context, View view) {
		if (view == null) {
			return;
		}
		InputMethodManager imm = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

	}

	public static boolean isKeyboardShowing(Context context) {
		InputMethodManager imm = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		return imm.isAcceptingText();
	}

	/**
	 * 获取屏幕高度
	 */
	public static int getScreenHeight(Activity paramActivity) {
		Display display = paramActivity.getWindowManager().getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		display.getMetrics(metrics);
		return metrics.heightPixels;
	}

	/**
	 * 获取状态栏高度
	 */
	public static int getStatusBarHeight(Activity paramActivity) {
		Rect localRect = new Rect();
		paramActivity.getWindow().getDecorView()
				.getWindowVisibleDisplayFrame(localRect);
		return localRect.top;
	}

	/**
	 * 获取ActionBar的高度
	 */
	public static int getActionBarHeight(Activity paramActivity) {
		// return
		// paramActivity.getResources().getDimensionPixelSize(R.dimen.topbar_height);
		return 0;
	}

	/**
	 * below actionbar, above softkeyboard
	 */
	public static int getAppContentHeight(Activity paramActivity) {
		return getScreenHeight(paramActivity)
				- getStatusBarHeight(paramActivity)
				- getActionBarHeight(paramActivity)
				- getKeyboardHeight(paramActivity);
	}

	/**
	 * 获取窗口高度 below status bar,include actionbar, above softkeyboard
	 */
	public static int getAppHeight(Activity paramActivity) {
		Rect localRect = new Rect();
		paramActivity.getWindow().getDecorView()
				.getWindowVisibleDisplayFrame(localRect);
		return localRect.height();
	}

	/**
	 * 获取键盘高度
	 */
	public static int getKeyboardHeight(Activity paramActivity) {
		int height = getScreenHeight(paramActivity)
				- getStatusBarHeight(paramActivity)
				- getAppHeight(paramActivity);
		// if (height == 0) {
		// height = SharedPrefHelper.getDefaultSoftKeyBoardHeight();
		// }
		// SharedPrefHelper.setDefaultSoftKeyBoardHeight(height);

		return height;
	}

	/**
	 * 键盘是否显示
	 */
	public static boolean isKeyBoardShow(Activity paramActivity) {
		int height = getScreenHeight(paramActivity)
				- getStatusBarHeight(paramActivity)
				- getAppHeight(paramActivity);
		return height != 0;
	}

	/**
	 * 设置光标位置
	 */
	public static void setEtSelection(View view) {
		if (view instanceof EditText) {
			EditText et = (EditText) view;
			String str = et.getText().toString();
			if (!TextUtils.isEmpty(str)) {
				et.setSelection(str.length());
			}
		}
	}

	/**
	 * EditText获取焦点,并弹出软键盘
	 * 
	 */
	public static void requestFocus(Context context, EditText v) {
		v.requestFocus();
		v.setSelection(v.getText().toString().length());

		AndroidTools.showKeyboard(context, v);
	}

	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	public static void keyboard(Activity context) {
		context.getWindow()
				.setSoftInputMode(
						WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
								| WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED);
	}


}

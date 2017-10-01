package vkd.locker.util;

import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.view.View;

public abstract class Util {

	/**
	 * Sets the background {@link Drawable} of a view.<br>
	 * On API level 16+ {@link View#setBackgroundDrawable(Drawable)} is
	 * deprecated, so we use the new method {@link View#setBackground(Drawable)}
	 * 
	 * @param v
	 *            The {@link View} on which to set the background
	 * @param bg
	 *            The background
	 */
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	public static void setBackgroundDrawable(View v, Drawable bg) {
		if (v == null)
			return;
		v.setBackground(bg);
	}

	/**
	 * SWAR Algorithm<br>
	 * 
	 * @param i
	 * @return The number of set bits in a 32bit integer
	 */
	public static int numberOfSetBits(int i) {
		i = i - ((i >> 1) & 0x55555555);
		i = (i & 0x33333333) + ((i >> 2) & 0x33333333);
		return (((i + (i >> 4)) & 0x0F0F0F0F) * 0x01010101) >> 24;
	}

	/**
	 * Get a {@link Bitmap} from a {@link Drawable}
	 * 
	 * @param drawable
	 * @return The {@link Bitmap} representing this {@link Drawable}
	 */
	public static Bitmap drawableToBitmap(Drawable drawable) {
		if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable) drawable).getBitmap();
		}

		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);

		return bitmap;
	}

	/**
	 * Utility method to get an {@link ActivityInfo} for a packageName.
	 * 
	 * @param packageName
	 * @return an {@link ActivityInfo} or null if not found. (or if packageName
	 *         or context are null)
	 */
	public static ApplicationInfo getaApplicationInfo(String packageName,
			Context c) {
		if (packageName == null || c == null)
			return null;
		try {
			return c.getPackageManager().getApplicationInfo(packageName, 0);
		} catch (NameNotFoundException e) {
			return null;
		}
	}

	public static float dpToPx(float dp, Context context) {
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		return dp * (metrics.densityDpi / 160f);
	}

	@RequiresApi(api = Build.VERSION_CODES.KITKAT)
	public static boolean checkUsageStatsPermission(Context context) {
		try {
			PackageManager packageManager = context.getPackageManager();
			ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
			AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
			int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName);
			return (mode == AppOpsManager.MODE_ALLOWED);

		} catch (PackageManager.NameNotFoundException e) {
			return false;
		}
	}

	/**
	 * Checks if password/pin is set or not
	 */
	public static boolean checkIfNotPinPassNotSet(Context context) {
		return new PrefUtils(context).isCurrentPasswordEmpty();
	}
}

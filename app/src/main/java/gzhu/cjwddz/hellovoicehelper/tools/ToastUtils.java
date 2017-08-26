package gzhu.cjwddz.hellovoicehelper.tools;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

/**
 * Created by gh0st on 2017/1/17.
 */

public class ToastUtils {
    private static Toast mToast;

    public static void showToast(Context context, int id) {
        mToast= Toast.makeText(context,id, Toast.LENGTH_SHORT);
        mToast.show();
    }

    public static void showToast(Context context, String string) {
        mToast= Toast.makeText(context, string, Toast.LENGTH_SHORT);
        mToast.show();
    }

    public static void showToastOnUI(final Activity context, final String string) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mToast= Toast.makeText(context, string, Toast.LENGTH_SHORT);
                mToast.show();
            }
        });
    }
}

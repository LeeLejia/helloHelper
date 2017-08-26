package gzhu.cjwddz.hellovoicehelper.connects;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.security.MessageDigest;

import gzhu.cjwddz.hellovoicehelper.BuildConfig;
import gzhu.cjwddz.hellovoicehelper.CallBack;
import gzhu.cjwddz.hellovoicehelper.Config;

/**
 * Created by cjwddz on 2017/5/17.
 */

public class Verify{

    public static Boolean legalUser =false;
    public static JSONObject result;
    public static boolean hasVerify=false;
    private static String SIGNATUR="a12e58526a28d16e5e5da57f995965cdae3e";
    /** 检查更新*/
    public static void Verify(Activity activity) {
        try {
            byte[] buffer = new byte[1024];
            Socket socket = new Socket("www.cjwddz.cn", 10005);
            JSONObject obj = new JSONObject();
            obj.put("protosign", 1258);
            obj.put("msgType", 0);
            obj.put("machine", getIMEI(activity));
            obj.put("code", Config.CODE);
            obj.put("version", "1.0");
            obj.put("application", "jwechat");
            OutputStream out = socket.getOutputStream();
            out.write(obj.toString().getBytes());
            out.flush();
            int count = socket.getInputStream().read(buffer);
            hasVerify = true;
            result = new JSONObject(new String(buffer, 0, count));
            if (result.getInt("msgType") > 0) {
                Intent it=new Intent();
                it.setAction("VERIFY");
                it.putExtra("RESULT",true);
                activity.sendBroadcast(it);
                legalUser = true;
            } else {
                Intent it=new Intent();
                it.setAction("VERIFY");
                it.putExtra("RESULT",false);
                activity.sendBroadcast(it);
                legalUser = false;
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    /** 检查更新*/
    public static void Verify(Activity activity, CallBack callBack) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (activity.checkSelfPermission("android.permission.READ_PHONE_STATE") == PackageManager.PERMISSION_DENIED) {
                activity.requestPermissions(new String[]{"android.permission.READ_PHONE_STATE"}, 1452);
            }
        }
        try {
            byte[] buffer = new byte[1024];
            Socket socket = new Socket("www.cjwddz.cn", 10005);
            JSONObject obj = new JSONObject();
            obj.put("protosign", 1258);
            obj.put("msgType", 0);
            obj.put("machine", getIMEI(activity));
            obj.put("code", Config.CODE);
            obj.put("version", "1.0");
            obj.put("application", "jwechat");
            OutputStream out = socket.getOutputStream();
            out.write(obj.toString().getBytes());
            out.flush();
            int count = socket.getInputStream().read(buffer);
            hasVerify = true;
            result = new JSONObject(new String(buffer, 0, count));
            if (result.getInt("msgType") > 0) {
                Intent it=new Intent();
                it.setAction("VERIFY");
                it.putExtra("RESULT",true);
                activity.sendBroadcast(it);
                legalUser = true;
                if(callBack!=null)
                    callBack.success("验证成功！");
            } else {
                Intent it=new Intent();
                it.setAction("VERIFY");
                it.putExtra("RESULT",false);
                activity.sendBroadcast(it);
                legalUser = false;
                if(callBack!=null)
                    callBack.fail("验证失败！");
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
    public static void CheckSign(Context context){
        //signature:
        System.out.println("SIGN:"+getAppSign(context));
        if(!BuildConfig.DEBUG && !getAppSign(context).equals(SIGNATUR.substring(4))){
            System.out.println("APP DEAL!!");
            System.exit(0);
        }
    }
    /** 获取App签名*/
    public static String getAppSign(Context context) {
        try {
            PackageInfo pis = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            return hexdigest(pis.signatures[0].toByteArray());
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("application not found");
        }
    }
    public static String hexdigest(byte[] paramArrayOfByte) {
        final char[] hexDigits = {48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 97, 98, 99, 100, 101, 102};
        try {
            MessageDigest localMessageDigest = MessageDigest.getInstance("MD5");
            localMessageDigest.update(paramArrayOfByte);
            byte[] arrayOfByte = localMessageDigest.digest();
            char[] arrayOfChar = new char[32];
            for (int i = 0, j = 0; ; i++, j++) {
                if (i >= 16) {
                    return new String(arrayOfChar);
                }
                int k = arrayOfByte[i];
                arrayOfChar[j] = hexDigits[(0xF & k >>> 4)];
                arrayOfChar[++j] = hexDigits[(k & 0xF)];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    public static String getIMEI(Context ctx) {
        String id;
        TelephonyManager mTelephony = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        if (mTelephony.getDeviceId() != null && !mTelephony.getDeviceId().startsWith("00000")) {
            id = mTelephony.getDeviceId();
        } else {
            id = Settings.Secure.getString(ctx.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        return id;
    }
}

package gzhu.cjwddz.hellovoicehelper.services;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Intent;
import android.os.Build;
import android.os.Parcelable;
import android.support.annotation.RequiresApi;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.io.DataOutputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gzhu.cjwddz.hellovoicehelper.Config;
import gzhu.cjwddz.hellovoicehelper.connects.Verify;
import gzhu.cjwddz.hellovoicehelper.tools.ToastUtils;

import static gzhu.cjwddz.hellovoicehelper.services.MyMoniTask.priority;

/**
 * Created by cjwddz on 2017/3/24.
 */
public class HLAccessbilityService extends AccessibilityService {
    long timestamp=0;
    Runtime runtime;
    MyMoniTask myMoniTask;
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            Parcelable data = event.getParcelableData();
            if (data == null || !(data instanceof Notification)) {
                return;
            }
            List<CharSequence> texts = event.getText();
            if (!texts.isEmpty()) {
                String text = String.valueOf(texts.get(0));
                /**判断是否合法用户*/
                if (!Verify.legalUser) {
                    ToastUtils.showToast(this, "未登录，或非法用户！");
                    return;
                }
                if(!Config.WORD.isEmpty() && !text.contains(": "+Config.WORD)){
                    ToastUtils.showToast(this, "口令错误！");
                    return;
                }
                notificationEvent(text, (Notification) data);
            }
        }else if(event.getEventType() ==AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED){
            Config.CURRENT_ACTIVITY=event.getClassName().toString();
            try {
                handleWindowStateChange();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }else if(event.getEventType() ==AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED){
            handleWindowContentChange();
        }
    }
    @Override
    public void onInterrupt() {
        if(myMoniTask !=null){
            myMoniTask.setDelay(-1);
            myMoniTask =null;
        }
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Verify.CheckSign(this);
        myMoniTask =new MyMoniTask(this);
    }

    /**
     * 执行Shell命令
     * @param command 要执行的命令数组
     */
    public void execShell(String command) {
        if (command == null) {
            return;
        }
        DataOutputStream os = null;
        try {
            // 获取root权限
            Process process = runtime.exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.write((command + "\n").getBytes());
            os.flush();
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 打开某个activity
     * @param activityName
     */
    protected void checkActivity(String activityName){
        if(!Config.CURRENT_ACTIVITY.equals(activityName)){
            MyMoniTask.isTurnning=true;
            Config.REQUEST_ACTIVITY=activityName;
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            intent.setClassName("com.yy.huanju",activityName);
            startActivity(intent);
        }else
            Config.REQUEST_ACTIVITY=activityName;
    }
    /**
     * 获取匹配字段
     * @param src
     * @param p
     * @param groupId
     * @return
     */
    protected String getMatcherString(String src,String p,int groupId){
        Pattern pattern=Pattern.compile(p);
        Matcher matcher= pattern.matcher(src);
        if(matcher.find()){
            int i=matcher.groupCount();
            if(i<groupId)
                return null;
            return matcher.group(groupId);
        }
        return null;
    }

    //region methods
    /**
     * 通知栏事件
     */
    protected void notificationEvent(String ticker, Notification nf) {
        String mt;
        if (ticker.matches(Config.C_UNBAN)) {/**解禁*/
            mt = getMatcherString(ticker, Config.C_UNBAN, 2);
            if (mt == null)
                return;
            for(char a :mt.toCharArray()){
                int id = Integer.parseInt(a+"") - 1;
                MyMoniTask.tasks.offer(new MyMoniTask.Task(Config.P_UNBAN,id));
            }
            myMoniTask.handleTask();
        }else if (ticker.matches(Config.C_BAN)) {/**禁麦*/
            mt = getMatcherString(ticker, Config.C_BAN, 2);
            if (mt == null)
                return;
            for(char a :mt.toCharArray()){
                int id = Integer.parseInt(a+"") - 1;
                MyMoniTask.tasks.offer(new MyMoniTask.Task(Config.P_BAN,id));
            }
            myMoniTask.handleTask();
        }else if (ticker.matches(Config.C_UCLOCK)) {/**解封*/
            mt = getMatcherString(ticker, Config.C_UCLOCK, 2);
            if (mt == null)
                return;
            for(char a :mt.toCharArray()){
                int id = Integer.parseInt(a+"") - 1;
                MyMoniTask.tasks.offer(new MyMoniTask.Task(Config.P_UCLOCK,id));
            }
            myMoniTask.handleTask();
        }  else if (ticker.matches(Config.C_CLOCK)) {/**封麦*/
            mt = getMatcherString(ticker, Config.C_CLOCK, 2);
            if (mt == null)
                return;
            for(char a :mt.toCharArray()){
                int id = Integer.parseInt(a+"") - 1;
                MyMoniTask.tasks.offer(new MyMoniTask.Task(Config.P_CLOCK,id));
            }
            myMoniTask.handleTask();
        }  else if (ticker.matches(Config.C_CLEARTIME)) {

        } else if (ticker.matches(Config.C_CLOSE_PUBSCREEN)) {

        } else if (ticker.matches(Config.C_CLOSEROOM)) {

        } else if (ticker.matches(Config.C_DELAY)) {/**设置刷房延时*/
            mt = getMatcherString(ticker, Config.C_DELAY, 2);
            if (mt == null)
                return;
            int delay = Integer.parseInt(mt);
            //priority = 0;
            myMoniTask.setDelay(delay);
        } else if (ticker.matches(Config.C_KICK)) {

        } else if (ticker.matches(Config.C_MANAGER)) {

        } else if (ticker.matches(Config.C_OPEN_PUBSCREEN)) {

        } else if (ticker.matches(Config.C_START)) {/**开始刷房*/
            //priority = 0;
            MyMoniTask.isRefreshing = true;
        }else if (ticker.matches(Config.C_STOP)) {/**暂停刷房*/
            MyMoniTask.isRefreshing = false;
        } else if (ticker.matches(Config.C_MOVEMANAGER)) {
        } else if (ticker.matches(Config.C_SET_ROOM_NAME)) {
        } else if (ticker.matches(Config.C_SET_BUGLE)) {
        } else if (ticker.matches(Config.C_OPENROOM)) {
        }else if (ticker.matches(Config.C_SITTER)) {
        } else if (ticker.matches(Config.C_SETSITUATION)) {
        } else if (ticker.matches(Config.C_SETANDREMOVE)) {

        }
    }


    /**
     * 窗口变换事件处理
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    protected void handleWindowStateChange() throws InterruptedException {
        timestamp=System.currentTimeMillis();
        if(Config.REQUEST_ACTIVITY.equals(Config.CURRENT_ACTIVITY)){
            MyMoniTask.isTurnning=false;
            Config.REQUEST_ACTIVITY="";
        }
        /**刷新任务*/
        //region  刷新
        if(priority==0 && MyMoniTask.isRefreshing){
            if(Config.A_MainActivity.equals(Config.CURRENT_ACTIVITY)){
                //只负责在MainActivity做该操作
                AccessibilityNodeInfo n= getNodeInfoByIdAndIndex("com.yy.huanju:id/item_my_room_avatar",0);
                if(n!=null)
                    AccessibilityHelper.performClick(n);
            }
            return;
        }
        //endregion
        /**操作聊天室界面 禁，解禁，封，解封*/
        if(priority==Config.P_BAN || priority==Config.P_UNBAN || priority==Config.P_CLOCK || priority==Config.P_UCLOCK){
           myMoniTask.handleMic();
        }
    }

    /**
     * 获取指定AccessibilityNodeInfo
     * @param id
     * @param index
     * @return
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public AccessibilityNodeInfo getNodeInfoByIdAndIndex(String id, int index){
        AccessibilityNodeInfo rootNodeInfo = getRootInActiveWindow();
        if(rootNodeInfo==null)
            return null;
        List<AccessibilityNodeInfo> nodeInfos = rootNodeInfo.findAccessibilityNodeInfosByViewId(id);
        if (nodeInfos.size() >index)
            return nodeInfos.get(index);
        else
            return null;
    }
    protected void handleWindowContentChange(){

    }

    public long getIdleTime(){
        return System.currentTimeMillis()-timestamp;
    }
    //endregion
}
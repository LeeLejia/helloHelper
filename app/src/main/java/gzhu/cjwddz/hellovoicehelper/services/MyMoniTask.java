package gzhu.cjwddz.hellovoicehelper.services;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import gzhu.cjwddz.hellovoicehelper.Config;

/**
 * Created by cjwddz on 2017/6/8.
 */

public class MyMoniTask {
    public static class Task{
        public int priority;
        public Object taskParam;
        public Task(int priority,Object taskParam){
            this.priority=priority;
            this.taskParam=taskParam;
        }
    }
    String TAG="MyMoniTask.";
    Timer refreshRoomTimer;
    TimerTask refreshRoom;
    HLAccessbilityService context;
    public static Boolean isRefreshing =false;
    public static Boolean isTurnning=false;
    /**任务队列ID*/
    public static LinkedList<Task> tasks=new LinkedList<Task>(){};
    //任务优先级 刷新最低0，禁麦1,封麦2
    public static int priority=0;
    public static Object taskParam;
    public MyMoniTask(final Context context){
        this.context=(HLAccessbilityService)context;
        //当界面超过4秒没有变化时候，检查任务
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
               long tmp= ((HLAccessbilityService) context).getIdleTime();
                if(tmp>7000){
                    ((HLAccessbilityService) context).timestamp=System.currentTimeMillis();
                    Log.i(TAG, "等待4秒没有反应，检查任务中。");
                    if(priority==Config.P_BAN || priority==Config.P_UNBAN || priority==Config.P_CLOCK || priority==Config.P_UCLOCK){
                        handleMic();
                    }
                }
            }
        },1000,1000);
    }

    /**
     * 设置时钟延时，用于刷新任务
     * @param delay 当delay小于等于0的时候不对时钟进行设置，而是不会运行代码
     */
    public void setDelay(int delay){
        if(delay<=0){
            isRefreshing =false;
        } else{
            isRefreshing =true;
            if(refreshRoomTimer!=null){
                refreshRoomTimer.cancel();
                refreshRoomTimer=null;
            }
            refreshRoomTimer=new Timer(true);
            refreshRoom=new TimerTask() {
                @Override
                public void run() {
                    if(!isRefreshing ||(isTurnning && priority<=0) )
                        return;
                    //只负责确保当前是MainActivity
                    checkActivity(Config.A_MainActivity,context);
                    checkActivity(Config.A_CHATROOM,context);
                }
            };
            refreshRoomTimer.schedule(refreshRoom,delay,delay);
        }
    }

    /**
     * 设置运行状况
     * @param isRunning
     */
    public void setRunning(boolean isRunning){
        this.isRefreshing =isRunning;
    }
    /**
     * 打开某个activity
     * @param activityName
     */
    private void checkActivity(String activityName,Context context){
        if(!Config.CURRENT_ACTIVITY.equals(activityName)){
            isTurnning=true;
            Config.REQUEST_ACTIVITY=activityName;
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            intent.setClassName("com.yy.huanju",activityName);
            try{context.startActivity(intent);}catch (ActivityNotFoundException e){}
        }else
            Config.REQUEST_ACTIVITY="";
    }

    /**
     * 获取栈顶Activity
     * @param context
     * @return
     */
    private String getCurrentActivityName(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);

        ComponentName componentInfo = taskInfo.get(0).topActivity;
        return componentInfo.getClassName();
    }

    /**
     * 处理任务。处理完任务便调用这个
     */
    public void handleTask(){
        //任务处理完毕
        if(tasks.size()<=0){
            priority=0;
            return;
        }
        MyMoniTask.Task task=tasks.poll();
        priority=task.priority;
        MyMoniTask.taskParam=task.taskParam;
        switch (priority){
            //禁，解禁，封，解封
            case Config.P_BAN:
            case Config.P_UNBAN:
            case Config.P_CLOCK:
            case Config.P_UCLOCK:
                chooseMic();
                break;
        }
    }

    /**
     * 禁麦
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    protected void chooseMic() {
        Config.REQUEST_ACTIVITY = Config.A_CHATROOM;
        if (!Config.CURRENT_ACTIVITY.equals(Config.A_CHATROOM)) {
            checkActivity(Config.A_CHATROOM,context);
        } else {//如果在聊天框了，就点击座位
            AccessibilityNodeInfo nodeInfo=context.getNodeInfoByIdAndIndex("com.yy.huanju:id/chatroom_mic_press",(Integer) MyMoniTask.taskParam);
            if(nodeInfo!=null)
                AccessibilityHelper.performClick(nodeInfo);
            else{
                handleMic();
            }
        }
    }
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void handleMic(){
        if(Config.REQUEST_ACTIVITY.equals(Config.CURRENT_ACTIVITY)){//是否聊天界面
            MyMoniTask.isTurnning=false;
            Config.REQUEST_ACTIVITY="";
            AccessibilityNodeInfo nodeInfo= context.getNodeInfoByIdAndIndex("com.yy.huanju:id/chatroom_mic_press",(Integer) MyMoniTask.taskParam);
            if(nodeInfo!=null)
                AccessibilityHelper.performClick(nodeInfo);
        }else if(Config.A_CHATROOM.equals(Config.CURRENT_ACTIVITY)){
            AccessibilityNodeInfo n= context.getNodeInfoByIdAndIndex("com.yy.huanju:id/topbar_right_child_layout",0);
            if(n!=null)
                AccessibilityHelper.performClick(n);
        }else if("com.yy.huanju.widget.dialog.c".equals(Config.CURRENT_ACTIVITY)){
            AccessibilityNodeInfo n= context.getNodeInfoByIdAndIndex("com.yy.huanju:id/topbar_right_child_layout",0);
            if(n!=null)
                AccessibilityHelper.performClick(n);
            else{
                n= context.getNodeInfoByIdAndIndex("com.yy.huanju:id/txt_menu_item_content",0);
                if(n!=null)
                    if(AccessibilityHelper.performClick(n)){
                        try {
                            Thread.sleep(400);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        n = context.getNodeInfoByIdAndIndex("android:id/button1", 0);
                        if (n != null)
                            AccessibilityHelper.performClick(n);
                    }
            }
        }else{//已经确认过是目标界面了，操作类型相符，相当于第二个步骤
            AccessibilityNodeInfo rootNodeInfo = context.getRootInActiveWindow();
            if(rootNodeInfo==null)
                return;
            List<AccessibilityNodeInfo> nodeInfos = rootNodeInfo.findAccessibilityNodeInfosByViewId("com.yy.huanju:id/txt_menu_item_content");
            if(nodeInfos.size()!=0){
                String text;
                //获取返回键
                AccessibilityNodeInfo back=context.getNodeInfoByIdAndIndex("com.yy.huanju:id/btn_cancel",0);
                if(back==null)
                    return;
                if(priority==Config.P_CLOCK || priority==Config.P_UCLOCK){
                    text=nodeInfos.get(1).getText().toString();
                    if(text.contains("解封")){
                        if(!(priority==Config.P_UCLOCK && AccessibilityHelper.performClick(nodeInfos.get(1))))
                            AccessibilityHelper.performClick(back);
                    }else if(text.contains("封闭")){
                        if(!(priority==Config.P_CLOCK && AccessibilityHelper.performClick(nodeInfos.get(1))))
                            AccessibilityHelper.performClick(back);
                    }else
                        AccessibilityHelper.performClick(back);
                }else{
                    if (nodeInfos.size() < 3)
                        return;
                    text = nodeInfos.get(2).getText().toString();
                    if (text.contains("解除禁麦")) {
                        if (!(priority == Config.P_UNBAN && AccessibilityHelper.performClick(nodeInfos.get(2))))
                            AccessibilityHelper.performClick(back);
                    } else if (text.contains("禁麦")) {
                        if (!(priority ==  Config.P_BAN && AccessibilityHelper.performClick(nodeInfos.get(2))))
                            AccessibilityHelper.performClick(back);
                    } else
                        AccessibilityHelper.performClick(back);
                }
                handleTask();
            }
        }
    }
}

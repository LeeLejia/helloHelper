package gzhu.cjwddz.hellovoicehelper;

import java.io.Serializable;

/**
 * Created by cjwddz on 2017/5/16.
 */

public class Config implements Serializable {
    /**
     * 配置项改变回调
     */
    public interface ConfigChange {
        //自动采集服务是否打开
        int ACCESSIBILITY = 1;

        void ConfigChangeListener(int preferce);
    }

    /**
     * 邀请码
     */
    public static String CODE = "";
    public static Boolean IS_VERIFY = false;
    public static Boolean IS_ServiceOn = false;
    /**
     * 命令
     */
    public static String WORD="";
    public static final String C_START = "^(.+?)开始$";
    public static final String C_STOP = "^(.+?)暂停$";
    public static final String C_DELAY = "^(.+?)刷房延时([0-9]+?)$";
    public static final String C_CLOCK = "^(.+?)封([1-8]+?)$";
    public static final String C_UCLOCK = "^(.+?)解封([1-8]+?)$";
    public static final String C_BAN = "^(.+?)禁([1-8]+?)$";
    public static final String C_UNBAN = "^(.+?)解禁([1-8]+?)$";

    public static final String C_MANAGER = "^(.+?)管(永久|1小时|3小时|24小时)(.+?)$";
    public static final String C_MOVEMANAGER = "^(.+?)移管(.+?)$";
    public static final String C_SET_ROOM_NAME = "^(.+?)房名(.+?)$";
    public static final String C_SET_BUGLE = "^(.+?)喇叭(.+?)$";
    public static final String C_CLOSEROOM = "^(.+?)锁房(.+?)$";
    public static final String C_OPENROOM = "^(.+?)解锁";
    public static final String C_CLEARTIME = "^(.+?)定时清公屏([0-9]*?)$";
    public static final String C_OPEN_PUBSCREEN = "^(.+?)开公屏$";
    public static final String C_CLOSE_PUBSCREEN = "^(.+?)关公屏$";
    public static final String C_KICK = "^(.+?)踢([0-9])$";
    public static final String C_SITTER = "^(.+?)抱([0-9])$";
    public static final String C_SETSITUATION = "^(.+?)抱树([0-9])(.+?)$";
    public static final String C_SETANDREMOVE = "^(.+?)抱踢([0-9])(.+?)$";

    public static final int P_DELAY =0;
    public static final int P_CLOCK = 1;
    public static final int P_UCLOCK =11;
    public static final int P_BAN = 2;
    public static final int P_UNBAN =22;

    public static final int P_MANAGER = 3;
    public static final int P_MOVEMANAGER =33;
    public static final int P_SET_ROOM_NAME =4;
    public static final int P_SET_BUGLE =5;
    public static final int P_CLOSEROOM =6;
    public static final int P_OPENROOM =66;
    public static final int P_CLEARTIME =7;
    public static final int P_OPEN_PUBSCREEN = 88;
    public static final int P_CLOSE_PUBSCREEN = 8;
    public static final int P_KICK = 9;
    public static final int P_SITTER =10;
    public static final int P_SETSITUATION = 12;
    public static final int P_SETANDREMOVE =13;
    /**
     * hello语音界面
     */
    public static String A_MainActivity = "com.yy.huanju.MainActivity";
    public static String A_CHATROOM = "com.yy.huanju.chatroom.ChatroomActivity";
    public static String A_ADD_ADMIN="com.yy.huanju.admin.AddAdminActivity";
    public static String CURRENT_ACTIVITY = "";//当前activity
    public static String REQUEST_ACTIVITY="";//所需要的activity

    /**禁麦*/
    public static Boolean[] isBan=new Boolean[]{false,false,false,false,false,false,false,false};
    /**封麦*/
    public static Boolean[] isSeal=new Boolean[]{false,false,false,false,false,false,false,false};
}

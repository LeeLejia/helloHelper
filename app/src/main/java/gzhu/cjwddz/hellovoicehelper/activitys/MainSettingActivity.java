package gzhu.cjwddz.hellovoicehelper.activitys;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import gzhu.cjwddz.hellovoicehelper.CallBack;
import gzhu.cjwddz.hellovoicehelper.Config;
import gzhu.cjwddz.hellovoicehelper.R;
import gzhu.cjwddz.hellovoicehelper.connects.Verify;
import gzhu.cjwddz.hellovoicehelper.services.AccessibilityHelper;
import gzhu.cjwddz.hellovoicehelper.services.HLAccessbilityService;
import gzhu.cjwddz.hellovoicehelper.tools.ToastUtils;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * Created by cjwddz on 2017/5/15.
 */

public class MainSettingActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener, View.OnClickListener, Preference.OnPreferenceClickListener {
    EditTextPreference edit_Code;
    Preference go_settingService;
    EditTextPreference edit_Word;
    ImageView iv;
    Button bt;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Verify.CheckSign(this);
        setContentView(R.layout.main_activity);
        addPreferencesFromResource(R.xml.setting);
        init();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission("android.permission.READ_PHONE_STATE") == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{"android.permission.READ_PHONE_STATE"}, 1452);
            }
        }else{
            if(!Verify.hasVerify){
                Runnable r= new Runnable(){
                    @Override
                    public void run() {
                        Verify.Verify(MainSettingActivity.this, new CallBack() {
                            @Override
                            public void success(Object o) {
                                ToastUtils.showToastOnUI(MainSettingActivity.this,(String)o);
                                setSummerOnUi("当前邀请码:"+Config.CODE+"[已验证]");
                            }

                            @Override
                            public void update(Object... i) {

                            }

                            @Override
                            public void fail(Object o) {
                                ToastUtils.showToastOnUI(MainSettingActivity.this,(String)o);
                                setSummerOnUi("当前邀请码:"+Config.CODE+"[验证失败]");
                            }
                        });
                    }
                };
                new Thread(r).start();
            }
        }
    }

    /**
     * 初始化界面
     */
    void init(){
        bt= (Button) findViewById(R.id.bt);
        go_settingService =findPreference("SETTING_SERVICE");
        go_settingService.setOnPreferenceClickListener(this);
        edit_Code= (EditTextPreference) findPreference("VERIFFY_CODE");
        edit_Code.setOnPreferenceChangeListener(this);
        edit_Word= (EditTextPreference) findPreference("SET_WORD");
        edit_Word.setOnPreferenceChangeListener(this);
        iv= (ImageView) findViewById(R.id.iv);

        Config.CODE=edit_Code.getText();
        if(Config.CODE==null || Config.CODE.isEmpty()){
            Config.CODE="";
            edit_Code.setSummary(getString(R.string.RequestCode));
        }
        else
            edit_Code.setSummary("当前邀请码:"+Config.CODE+"[未验证]");
        bt.setOnClickListener(this);
        Config.WORD=edit_Word.getText();
        if(Config.WORD==null || Config.WORD.isEmpty()){
            Config.WORD="";
            edit_Word.setSummary(getString(R.string.noword));
        }else
            edit_Word.setSummary(String.format("当前口令[%s]",Config.WORD));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(AccessibilityHelper.isAccessibilitySettingsOn(this, HLAccessbilityService.class)){
            Config.IS_ServiceOn=true;
            go_settingService.setSummary("辅助设置已经打开");
            iv.setImageResource(R.drawable.command);
        }else{
            Config.IS_ServiceOn=false;
            go_settingService.setSummary("请打开辅助设置");
            iv.setImageResource(R.drawable.help);
        }
    }

    /**
     * 配置项改变
     * @param preference
     * @param newValue
     * @return
     */
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == edit_Code) {
            Config.CODE = (String) newValue;
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    Verify.Verify(MainSettingActivity.this, new CallBack() {
                        @Override
                        public void success(Object o) {
                            ToastUtils.showToastOnUI(MainSettingActivity.this,(String)o);
                            setSummerOnUi("当前邀请码:"+Config.CODE+"[已验证]");
                        }
                        @Override
                        public void update(Object... i) {

                        }
                        @Override
                        public void fail(Object o) {
                            ToastUtils.showToastOnUI(MainSettingActivity.this,(String)o);
                            setSummerOnUi("当前邀请码:"+Config.CODE+"[验证失败]");
                        }
                    });
                }
            };
            new Thread(r).start();
        }else if(preference==edit_Word){
            Config.WORD = (String) newValue;
            if(Config.WORD==null || Config.WORD.isEmpty()){
                Config.WORD="";
                edit_Word.setSummary(getString(R.string.noword));
            }else
                edit_Word.setSummary(String.format("当前口令[%s]",Config.WORD));
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        if(v==bt){

        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==1452){
            if(grantResults[0]!=PERMISSION_GRANTED){
                ToastUtils.showToast(this,"权限获取失败！");
                return;
            }
            if(!Verify.hasVerify){
                Runnable r= new Runnable(){
                    @Override
                    public void run() {
                        Verify.Verify(MainSettingActivity.this);
                    }
                };
                new Thread(r).start();
            }
        }
    }
    @Override
    public boolean onPreferenceClick(Preference preference) {
        if(preference==go_settingService) {
            if (Config.IS_ServiceOn) {
                ToastUtils.showToast(this, "辅助设置已经打开！");
            }
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        }
        return false;
    }
    /**
     * ui线程更新Summer
     * @param s
     */
    void setSummerOnUi(final String s){
        Runnable r=new Runnable() {
            @Override
            public void run() {
                edit_Code.setSummary(s);
            }
        };
        runOnUiThread(r);
    }
}
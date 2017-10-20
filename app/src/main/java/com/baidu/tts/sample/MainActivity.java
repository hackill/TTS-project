package com.baidu.tts.sample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.baidu.tts.sample.control.InitConfig;
import com.baidu.tts.sample.control.MySyntherizer;
import com.baidu.tts.sample.control.NonBlockSyntherizer;
import com.baidu.tts.sample.listener.UiMessageListener;
import com.baidu.tts.sample.util.OfflineResource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    protected String appId = "8535996";

    protected String appKey = "MxPpf3nF5QX0pndKKhS7IXcB";

    protected String secretKey = "7226e84664474aa098296da5eb2aa434";

    // TtsMode.MIX; 离在线融合，在线优先； TtsMode.ONLINE 纯在线； 没有纯离线
    protected TtsMode ttsMode = TtsMode.MIX;

    // 离线发音选择，VOICE_FEMALE即为离线女声发音。
    // assets目录下bd_etts_speech_female.data为离线男声模型；bd_etts_speech_female.data为离线女声模型
    protected String offlineVoice = OfflineResource.VOICE_FEMALE;

    protected MySyntherizer synthesizer;

    private EditText mEditText;
    private RadioGroup radioGroup;

    private int speaker = 1;
    //0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initPermission();
        initialTts();

        findViewById(R.id.play).setOnClickListener(this);
        findViewById(R.id.stop).setOnClickListener(this);

        mEditText = (EditText) findViewById(R.id.edit_text);
        radioGroup = (RadioGroup) findViewById(R.id.radio_group);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {

                Log.d(TAG, "onCheckedChanged() called with: group = [" + group + "], checkedId = [" + checkedId + "]");
                switch (checkedId) {
                    case R.id.male:
                        speaker = 1;
                        break;
                    case R.id.male3:
                        speaker = 3;
                        break;
                    case R.id.female:
                        speaker = 0;
                        break;
                    case R.id.child:
                        speaker = 4;
                        break;

                }


            }
        });
    }


    /**
     * 初始化引擎，需要的参数均在InitConfig类里
     * <p>
     * DEMO中提供了3个SpeechSynthesizerListener的实现
     * MessageListener 仅仅用log.i记录日志，在logcat中可以看见
     * UiMessageListener 在MessageListener的基础上，对handler发送消息，实现UI的文字更新
     * FileSaveListener 在UiMessageListener的基础上，使用 onSynthesizeDataArrived回调，获取音频流
     */
    protected void initialTts() {
        // 设置初始化参数
        SpeechSynthesizerListener listener = new UiMessageListener(mainHandler); // 此处可以改为 含有您业务逻辑的SpeechSynthesizerListener的实现类

        Map<String, String> params = getParams();

        // appId appKey secretKey 网站上您申请的应用获取。注意使用离线合成功能的话，需要应用中填写您app的包名。包名在build.gradle中获取。
        InitConfig initConfig = new InitConfig(appId, appKey, secretKey, ttsMode, offlineVoice, params, listener);

        synthesizer = new NonBlockSyntherizer(this, initConfig, mainHandler); // 此处可以改为MySyntherizer 了解调用过程
    }

    /**
     * 合成的参数，可以初始化时填写，也可以在合成前设置。
     *
     * @return
     */
    protected Map<String, String> getParams() {
        Map<String, String> params = new HashMap<String, String>();
        // 以下参数均为选填


        params.put(SpeechSynthesizer.PARAM_SPEAKER, String.valueOf(speaker)); // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
        params.put(SpeechSynthesizer.PARAM_VOLUME, "5"); // 设置合成的音量，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_SPEED, "5");// 设置合成的语速，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_PITCH, "5");// 设置合成的语调，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_HIGH_SPEED_SYNTHESIZE);         // 该参数设置为TtsMode.MIX生效。即纯在线模式不生效。
        // MIX_MODE_DEFAULT 默认 ，wifi状态下使用在线，非wifi离线。在线状态下，请求超时6s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_NETWORK ， 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时6s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE, 2G 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
        return params;
    }


    @Override
    public void onClick(View v) {

        int id = v.getId();
        switch (id) {
            case R.id.stop:
                synthesizer.stop();
                break;
            case R.id.play:
                speak();
                break;
        }
    }


    private void speak() {

        synthesizer.setParams(getParams());

        String text = mEditText.getText().toString();

        synthesizer.speak(text);
    }

    /**
     * android 6.0 以上需要动态申请权限
     */
    private void initPermission() {
        String permissions[] = {
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_SETTINGS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE
        };

        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                //进入到这里代表没有权限.
            }
        }
        String tmpList[] = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // 此处为android 6.0以上动态授权的回调，用户自行实现。
    }

}
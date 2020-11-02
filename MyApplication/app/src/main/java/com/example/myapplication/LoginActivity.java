package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import android.view.View;

import android.widget.Button;
import android.widget.EditText;

import android.widget.Toast;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

public class LoginActivity extends Activity implements View.OnClickListener {

    private Button bt_getphonecore;
    private EditText edit_phone;
    private EditText edit_cord;
    private String phone_number;
    private String cord_number;
    EventHandler eventHandler;
    private boolean coreflag = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        initViews();
        sms_verification();

    }

    private void initViews() {

        Button bt_login = findViewById(R.id.bt_login);
        bt_getphonecore = findViewById(R.id.bt_getphonecore);
        edit_phone = findViewById(R.id.ed_phone); //你的手机号
        edit_cord = findViewById(R.id.ed_code);//你的验证码

        bt_login.setOnClickListener(this);
        bt_getphonecore.setOnClickListener(this);
    }

    protected void onDestroy() {//销毁
        super.onDestroy();
        SMSSDK.unregisterEventHandler(eventHandler);

    }

    protected void onResume() {
        super.onResume();
    }


    //按钮点击事件
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_getphonecore://获取验证码的ID
                if (judPhone()) {//去掉左右空格获取字符串，是正确的手机号
                    SMSSDK.getVerificationCode("86", phone_number);//获取你的手机号的验证码
                    edit_cord.requestFocus();//判断是否获得焦点
                }
                break;
            //  获取后要提交你的验证码以判断是否正确，并登陆成功
            case R.id.bt_login://登陆页面的ID
                if (judCord()) {//判断验证码
                    SMSSDK.submitVerificationCode("86", phone_number, cord_number);//提交手机号和验证码
                    Intent intent =new Intent(this, MainActivity.class);
                    intent.putExtra("phone", phone_number);
                    GlobalData globalData = new GlobalData();
                    globalData.setPhoneNumber(phone_number);
                    startActivity(intent);
                }
                coreflag = false;
                break;
        }
    }

    private boolean judPhone() {//判断手机号是否正确
        //不正确的情况
        if (TextUtils.isEmpty(edit_phone.getText().toString().trim())) {
            Toast.makeText(LoginActivity.this, "请输入您的电话号码", Toast.LENGTH_LONG).show();
            edit_phone.requestFocus();//设置是否获得焦点。若有requestFocus()被调用时，后者优先处理。注意在表单中想设置某一个如EditText获取焦点，光设置这个是不行的，需要将这个EditText前面的focusable都设置为false才行。
            return false;
        } else if (edit_phone.getText().toString().trim().length() != 11) {
            Toast.makeText(LoginActivity.this, "您的电话号码位数不正确", Toast.LENGTH_LONG).show();
            edit_phone.requestFocus();
            return false;
        }

        //正确的情况
        else {
            phone_number = edit_phone.getText().toString().trim();
            String num = "[1][3578]\\d{9}";
            if (phone_number.matches(num)) {
                return true;
            } else {
                Toast.makeText(LoginActivity.this, "请输入正确的手机号码", Toast.LENGTH_LONG).show();
                return false;
            }
        }
    }


    private boolean judCord() {//判断验证码是否正确
        judPhone();//先执行验证手机号码正确与否
        if (TextUtils.isEmpty(edit_cord.getText().toString().trim())) {//验证码
            Toast.makeText(LoginActivity.this, "请输入您的验证码", Toast.LENGTH_LONG).show();
            edit_cord.requestFocus();//聚集焦点
            return false;
        } else if (edit_cord.getText().toString().trim().length() != 6) {
            Toast.makeText(LoginActivity.this, "您的验证码位数不正确", Toast.LENGTH_LONG).show();
            edit_cord.requestFocus();
            return false;
        } else {
            cord_number = edit_cord.getText().toString().trim();
            return true;
        }
    }

    public void sms_verification() {
        eventHandler = new EventHandler() {
            public void afterEvent(int event, int result, Object data) {
                Message msg = new Message();//创建了一个对象
                msg.arg1 = event;
                msg.arg2 = result;
                msg.obj = data;
                handler.sendMessage(msg);
            }
        };
        SMSSDK.registerEventHandler(eventHandler);//注册短信回调（记得销毁，避免泄露内存）*/
    }

    /**
     * 使用Handler来分发Message对象到主线程中，处理事件
     */
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int event = msg.arg1;
            int result = msg.arg2;
            Object data = msg.obj;
            if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {//获取验证码成功
                if (result == SMSSDK.RESULT_COMPLETE) {
                    //回调完成
                    boolean smart = (Boolean) data;
                    if (smart) {
                        Toast.makeText(getApplicationContext(), "该手机号已经注册过，请重新输入", Toast.LENGTH_LONG).show();
                        edit_phone.requestFocus();//焦点
                        return;
                    }
                }
            }
            //回调完成
            if (result == SMSSDK.RESULT_COMPLETE) {
                if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {//提交验证码成功
                    Toast.makeText(getApplicationContext(), "验证码输入正确", Toast.LENGTH_LONG).show();
                }
            } else {//其他出错情况
                if (coreflag) {
                    bt_getphonecore.setVisibility(View.VISIBLE);
                    Toast.makeText(getApplicationContext(), "验证码获取失败请重新获取", Toast.LENGTH_LONG).show();
                    edit_phone.requestFocus();
                } else {
                    Toast.makeText(getApplicationContext(), "验证码输入错误", Toast.LENGTH_LONG).show();
                }
            }
        }
    };
}
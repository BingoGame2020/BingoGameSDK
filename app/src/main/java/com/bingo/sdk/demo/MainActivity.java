package com.bingo.sdk.demo;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.bingo.sdk.BingoSdkCore;
import com.bingo.sdk.activity.BingoNoticeWebActivity;
import com.bingo.sdk.bean.RechargeOptions;
import com.bingo.sdk.callback.BingoExitCallBack;
import com.bingo.sdk.callback.BingoSDKCallBack;
import com.bingo.sdk.callback.PermissionCallback;
import com.bingo.sdk.inner.util.CommonUtil;
import com.bingo.sdk.inner.util.DeviceUtil;
import com.bingo.sdk.inner.util.LogUtil;
import com.bingo.sdk.inner.util.ToastUtil;
import com.bingo.sdk.web.ApiStatusCode;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView tv_msg;
    private String info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] permissions = new String[]{
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };

        BingoSdkCore.getInstance().checkPermissions(this, permissions, new PermissionCallback() {
            @Override
            public void onPermissionGranted() {
                LogUtil.e("onPermissionGranted");
                init();
            }

            @Override
            public void onPermissionDenied() {
                ToastUtil.showShortToast(MainActivity.this, "权限被拒绝");
            }
        });
        setListener();
//        test();
    }


    private void init() {


        BingoSdkCore.getInstance().initSdk(this, null, new BingoSDKCallBack() {
            @Override
            public void onRealNameFinished(int code) {
                LogUtil.e("实名认证结果: " + (code == ApiStatusCode.CODE_SUCCESS));
            }

            @Override
            public void onInitFinished(int code, String msg) {
                LogUtil.e("MainActivity onInitFinished: " + msg);
                if (code != ApiStatusCode.CODE_SUCCESS) {
                    ToastUtil.showShortToast(MainActivity.this, "初始化失败: " + msg);
                }
            }

            @Override
            public void onLoginFinished(int code, String result) {
                LogUtil.e("MainActivity onLoginFinished: " + result);
                if (code != ApiStatusCode.CODE_SUCCESS) {
                    ToastUtil.showShortToast(MainActivity.this, "登录失败: " + result);
                } else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //模拟加载时间
                            BingoSdkCore.getInstance().onGameLoadFinished(MainActivity.this);
                        }
                    }, 5000);
                }
            }

            @Override
            public void onChangeAccount(int code, String result) {
                LogUtil.e("MainActivity onChangeAccount: " + result);
                if (code != ApiStatusCode.CODE_SUCCESS) {
                    ToastUtil.showShortToast(MainActivity.this, "切换账号失败: " + result);
                } else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //模拟加载时间
                            BingoSdkCore.getInstance().onGameLoadFinished(MainActivity.this);
                        }
                    }, 5000);
                }
            }

            @Override
            public void onPayFinished(int code, String result) {
                LogUtil.e("MainActivity onPayFinished: " + result);
                if (code != ApiStatusCode.CODE_SUCCESS) {
                    ToastUtil.showShortToast(MainActivity.this, "支付失败: " + result);
                } else {
                    ToastUtil.showShortToast(MainActivity.this, "支付成功");
                }
            }

            @Override
            public void onLogout() {
                LogUtil.e("MainActivity  onLogout");
            }

        });
    }

    private void setListener() {
        findViewById(R.id.btn_login).setOnClickListener(this);
        findViewById(R.id.btn_pay).setOnClickListener(this);
        findViewById(R.id.btn_test).setOnClickListener(this);
        findViewById(R.id.btn_save).setOnClickListener(this);
        findViewById(R.id.btn_delete).setOnClickListener(this);
        findViewById(R.id.btn_notice).setOnClickListener(this);
        findViewById(R.id.btn_get_code).setOnClickListener(this);
        tv_msg = findViewById(R.id.tv_msg);
        tv_msg.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                String content = tv_msg.getText().toString();
                ClipData data = ClipData.newPlainText("data", CommonUtil.filterNull(content));
                clipboard.setPrimaryClip(data);
                ToastUtil.showShortToast(MainActivity.this, "已复制到剪贴板");
                return true;
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_login) {
            login();
        } else if (id == R.id.btn_pay) {
            pay();
        } else if (id == R.id.btn_test) {
            test();
        } else if (id == R.id.btn_save) {
            save();
        } else if (id == R.id.btn_delete) {
            delete();
        } else if (id == R.id.btn_notice) {
            notice();
        } else if (id == R.id.btn_get_code) {
//            getCode();
            show();
        }

    }

    private void show() {
//        Account fromDb = AccountUtil.getCurrentLoginAccountFromDb(this);
//        LogUtil.e("登录信息: " + fromDb);
//
//        DialogUtil.getInstance().showTopEnterDialog(this, "欢迎", null);
//        DialogUtil.getInstance().showRealNameDialog(this,false,null);
//
//        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=")));
    }

    private void getCode() {

//
        String code = DeviceUtil.getDeviceCode(this);
        LogUtil.e("device code: " + code);
        tv_msg.setText("device code: " + code);
    }

    private void notice() {
        Intent intent = new Intent(this, BingoNoticeWebActivity.class);
        intent.putExtra("url", "https://www.baidu.com");
        startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void delete() {
        String fileName = "test.txt";
        ContentResolver resolver = getContentResolver();
        Uri uri = MediaStore.setIncludePending(MediaStore.Downloads.EXTERNAL_CONTENT_URI);
        String[] proj = new String[]{
                MediaStore.Downloads._ID,
                MediaStore.Downloads.DATA,
                MediaStore.Downloads.DISPLAY_NAME,
        };
        Cursor cursor = resolver.query(uri, proj, proj[2] + " = ? ", new String[]{fileName}, null);
        if (cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(proj[0]));
            String path = cursor.getString(cursor.getColumnIndex(proj[1]));
            String name = cursor.getString(cursor.getColumnIndex(proj[2]));
            LogUtil.e("id:" + id + "\tpath: " + path + "\tname:" + name);

            Uri newUri = uri.buildUpon().appendPath(id + "").build();
            LogUtil.e("要删除的新的uri: " + newUri);
            int delete = resolver.delete(newUri, null, null);
            LogUtil.e("删除结果: " + delete);
        }
        cursor.close();


    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void save() {
        String fileName = "test.txt";
        ContentResolver resolver = getContentResolver();
        Uri uri = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DATE_MODIFIED, System.currentTimeMillis());
        values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
        String path = Environment.DIRECTORY_DOWNLOADS + "/TEST/";
        LogUtil.e("path: " + path);
        values.put(MediaStore.Downloads.RELATIVE_PATH, path);
        values.put(MediaStore.Downloads.OWNER_PACKAGE_NAME, getPackageName());
        values.put(MediaStore.Downloads.IS_PENDING, true);//重要,如果不加这个,insert会返回null
//            values.put(MediaStore.Downloads.MIME_TYPE, MimeType.TXT);//加了这个 会默认加上后缀名
        Uri pending = MediaStore.setIncludePending(uri);
        Uri insertUri = resolver.insert(pending, values);
        LogUtil.e("写入uri: " + insertUri);
        if (insertUri == null) {
            return;
        }
        try {
            OutputStream outputStream = resolver.openOutputStream(insertUri);
            outputStream.write("这些是测试内容".getBytes());
            outputStream.flush();
            outputStream.close();

            LogUtil.e("写入成功");
        } catch (Exception e) {
            LogUtil.e("写入错误");
            e.printStackTrace();
        } finally {
            values.put(MediaStore.Downloads.IS_PENDING, false);//重要,如果不加这个,insert会返回null
            resolver.update(insertUri, values, null, null);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void test() {

    }

    private void readContent(Uri newUri) {
        ContentResolver resolver = getContentResolver();
        try {
            InputStream inputStream = resolver.openInputStream(newUri);
            InputStreamReader reader = new InputStreamReader(inputStream);
            char[] buff = new char[2048];
            StringBuilder builder = new StringBuilder();
            for (; ; ) {
                int size = reader.read(buff, 0, buff.length);
                if (size < 0)
                    break;
                builder.append(buff);
            }

            LogUtil.e("读取到的内容:" + builder.toString().trim());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void pay() {
//        List<Account> accounts = BingoDBManager.getInstance().getAll(this);
//        for (Account account : accounts) {
//            LogUtil.e("查询结果: " + account);
//        }

//
        RechargeOptions opts = new RechargeOptions();
        opts.setAmount(1).setUnitName("元宝").setOrderId(System.currentTimeMillis() + "")
                .setUnitNumber(1000).setRatio(10)
                .setExt("透传参数").setServerId("1").setServerName("1")
                .setRoleName("1").setRoleLevel(1).setRoleId("123456");
        BingoSdkCore.getInstance().pay(this, opts);
    }

    private void login() {
//                Intent intent = new Intent(this, BingoWechatPayWebActivity.class);
//        intent.putExtra("url","https://wx.tenpay.com/cgi-bin/mmpayweb-bin/checkmweb?prepay_id=wx10153028410699038bb879ef149d6c0000&package=3402966678");
//        startActivity(intent);

//        Account account = new Account();
//        account.setUid("123456").setMobile("18888888888").setPassword("password1234");
//        long upsert = BingoDBManager.getInstance().upsert(this, account);
//        LogUtil.e("插入更新结果:" + upsert);
//
//        long time = System.currentTimeMillis();
//        String md5 = EncryptUtil.encodeByMD5(time + "");
//        LogUtil.e("MD5:"+md5);
//        String key = EncryptUtil.filterKey(md5);
//        LogUtil.e("key: "+key);

//
        BingoSdkCore.getInstance().login(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.e("MainActivity onResume()");
        BingoSdkCore.getInstance().onResume(this);

    }

    @Override
    protected void onStop() {
        super.onStop();
        BingoSdkCore.getInstance().onStop(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        BingoSdkCore.getInstance().onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        BingoSdkCore.getInstance().exit(this, new BingoExitCallBack() {
            @Override
            public void onExit(boolean exit) {
                if (exit)
                    finish();

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BingoSdkCore.getInstance().onDestroy(this);
        BingoSdkCore.getInstance().release();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        LogUtil.e("申请权限结果: " + Arrays.toString(grantResults));
        BingoSdkCore.getInstance().onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }
}
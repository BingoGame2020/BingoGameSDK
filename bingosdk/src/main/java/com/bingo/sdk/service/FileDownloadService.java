package com.bingo.sdk.service;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bingo.sdk.inner.bean.DownloadUri;
import com.bingo.sdk.inner.channel.ChannelConfig;
import com.bingo.sdk.inner.util.FileUtil;
import com.bingo.sdk.inner.util.LogUtil;
import com.bingo.sdk.inner.util.MetaUtil;
import com.bingo.sdk.utils.ResourceManager;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class FileDownloadService extends IntentService {
    private final Handler handler;
    private static final int TIME_OUT = 15;
    private static final String TAG = FileDownloadService.class.getSimpleName();
    private int lastPercent = 0;
    private String fileName;

    public FileDownloadService() {
        super(TAG);
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (msg.what == 1) {
                    NotificationObj obj = (NotificationObj) msg.obj;
                    initNotification(msg.arg1, msg.arg2, obj.getUri(), obj.getContentType());
                }
            }
        };
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                String url = extras.getString("url");
                download(url);
            }
        }

    }

    private void download(final String url) {
        Request request = new Request.Builder()
                .url(url).build();


        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.callTimeout(TIME_OUT, TimeUnit.SECONDS)
                .connectTimeout(TIME_OUT, TimeUnit.SECONDS).readTimeout(TIME_OUT, TimeUnit.SECONDS)
                .writeTimeout(TIME_OUT, TimeUnit.SECONDS)
        ;
        OkHttpClient client = builder.build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                LogUtil.e("下载失败: " + e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                LogUtil.e("下载请求成功: " + getApplicationContext());
                ResponseBody body = response.body();
                if (body == null) {
//                    if (listener!=null) {
//                        listener.onFailed(-1, "下载内容为空");
//                    }
                    LogUtil.e("Response body为空");

                } else {
                    long contentLength = body.contentLength();
                    MediaType contentType = body.contentType();
                    String[] split = url.split("/");
                    //只取文件名,如: https://xxx.xx.com/aa.apk, 只截取aa.apk
                    fileName = split[split.length - 1];
                    String type = contentType == null ? "" : contentType.toString();
                    LogUtil.e("下载文件大小: " + contentLength + "\ttype: " + contentType + "\t名字: " + fileName);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        saveFileForQ(fileName, type, contentLength, body.byteStream());
                    } else {
                        saveFilePreQ(fileName, type, contentLength, body.byteStream());
                    }
//                    initNotification("请求数据大小: " + contentLength);
                }

//                updateNotification();
            }
        });
    }

    private void saveFilePreQ(String fileName, String contentType, long length, InputStream inputStream) {
        //10.0以前 保存文件
        DownloadUri downloadUri = FileUtil.getDownloadFilePreQ(fileName);
        if (downloadUri.getFile() == null) {
            LogUtil.e("file  is not exists ");
            downloadError(downloadUri);
            return;
        }
        if (length <= 0) {
            downloadError(downloadUri);
            return;
        }
        if (inputStream == null) {
//            if (listener != null) {
//                listener.onFailed(-1, "input stream is null");
//            }
            LogUtil.e("input stream is null");
            downloadError(downloadUri);
            return;
        }

        initNotification(0, downloadUri.getContentId(), Uri.fromFile(downloadUri.getFile()), contentType);
        LogUtil.e("写入文件 uri: " + downloadUri.getFile());
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(downloadUri.getFile());
            byte[] bytes = new byte[2048];
            int len;
            long wroteSum = 0;//已写入的长度
            while ((len = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
                wroteSum += len;
                int progressPercent = (int) (wroteSum * 1f / length * 100f);//不转成float会一直为0
//                LogUtil.e("完成百分比: " + progressPercent + "\t写入: " + wroteSum + "\t总共: " + length);
                if (lastPercent != progressPercent) {
                    Message message = Message.obtain();
                    message.what = 1;
                    message.arg1 = progressPercent;
                    message.arg2 = downloadUri.getContentId();
                    NotificationObj obj = new NotificationObj();
                    obj.setUri(Uri.fromFile(downloadUri.getFile()));
                    obj.setContentType(contentType);
                    message.obj = obj;
                    handler.sendMessage(message);
                    lastPercent = progressPercent;
                }

            }
            outputStream.flush();

            startInstall(Uri.fromFile(downloadUri.getFile()), contentType);

        } catch (Exception e) {
            e.printStackTrace();
            downloadError(downloadUri);
        } finally {
            try {

                inputStream.close();
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveFileForQ(String fileName, String contentType, long length, InputStream inputStream) {
        DownloadUri uri = FileUtil.getDownloadFileForQ(getApplicationContext(), fileName, contentType);
        if (uri.getUri() == null) {
            LogUtil.e("file uri is null ");
            downloadError(uri);
            return;
        }

        if (length <= 0) {
            downloadError(uri);
            return;
        }
        if (inputStream == null) {
//            if (listener != null) {
//                listener.onFailed(-1, "input stream is null");
//            }
            LogUtil.e("input stream is null");
            downloadError(uri);
            return;
        }

        initNotification(0, uri.getContentId(), uri.getUri(), contentType);


        LogUtil.e("写入文件 uri: " + uri.getUri());
        ContentResolver resolver = getContentResolver();
        OutputStream outputStream = null;
        try {
            outputStream = resolver.openOutputStream(uri.getUri());

            byte[] bytes = new byte[2048];
            int len;
            long wroteSum = 0;//已写入的长度
            while ((len = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
                wroteSum += len;
                int progressPercent = (int) (wroteSum * 1f / length * 100f);//不转成float会一直为0
//                LogUtil.e("完成百分比: " + progressPercent + "\t写入: " + wroteSum + "\t总共: " + length);
                if (lastPercent != progressPercent) {
                    Message message = Message.obtain();
                    message.what = 1;
                    message.arg1 = progressPercent;
                    message.arg2 = uri.getContentId();

                    NotificationObj obj = new NotificationObj();
                    obj.setUri(uri.getUri());
                    obj.setContentType(contentType);
                    message.obj = obj;

                    handler.sendMessage(message);
                    lastPercent = progressPercent;
                }

            }
            outputStream.flush();


            startInstall(uri.getUri(), contentType);

        } catch (Exception e) {
            e.printStackTrace();
            downloadError(uri);
        } finally {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContentValues values = new ContentValues();
                    //无论最终下载是否成功,都要去更新这个值,避免以后出现数据库唯一约束冲突
                    //冲突原因是数据库的data保存文件路径,但是这个字段是有唯一约束的
                    values.put(MediaStore.Downloads.IS_PENDING, false);
                    resolver.update(uri.getUri(), values, null, null);
                }

                inputStream.close();
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void downloadError(DownloadUri uri) {
        initNotificationError(uri.getContentId());
        //删除数据库记录,避免产生垃圾数据
        deleteMediaStoreRecord(uri.getContentId());
    }

    private void deleteMediaStoreRecord(int contentId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = getContentResolver();
            Uri uri = MediaStore.setIncludePending(MediaStore.Downloads.EXTERNAL_CONTENT_URI);
            int delete = resolver.delete(uri, MediaStore.Downloads._ID + " = ? ", new String[]{contentId + ""});
            LogUtil.e("下载失败,删除记录: " + delete);
        }
    }

    private Intent getInstallIntent(Uri uri, String contentType) {
        String realPath = getRealPath(uri);
        LogUtil.i("真实路径: " + realPath);
        try {
            File file = new File(realPath);
            String authority = getApplicationContext().getPackageName() + ".fileProvider";
            Uri fileUri = Uri.fromFile(file);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                fileUri = FileProvider.getUriForFile(getApplicationContext(), authority, file);
            }

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(fileUri, contentType);
            }
            return intent;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void startInstall(Uri uri, String contentType) {
        Intent intent = getInstallIntent(uri, contentType);
        if (intent == null) {
            LogUtil.e("安装intent为空");
            return;
        }
        startActivity(intent);

    }

    private String getRealPath(Uri uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = getContentResolver();
            String[] projects = {MediaStore.Downloads.RELATIVE_PATH, MediaStore.Downloads.DISPLAY_NAME, MediaStore.Downloads.DATA};
            Cursor cursor = resolver.query(uri, projects, null, null, null);
            String realPath = null;
            if (cursor.moveToFirst()) {
//                String dirName = cursor.getString(cursor.getColumnIndex(projects[0]));
//                String fileName = cursor.getString(cursor.getColumnIndex(projects[1]));
                String data = cursor.getString(cursor.getColumnIndex(projects[2]));
                LogUtil.e("data: " + data);
                realPath = data;
            }
            cursor.close();

            return realPath;
        } else {
            return uri.getPath();
        }
    }


    /**
     * 下载出错
     *
     * @param contentId 任务id
     */
    private void initNotificationError(int contentId) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

//        Intent intent = new Intent();//不做任何跳转;但是pendingIntent必须要设置,否则通知点击不会自动消失
//        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        int notificationIcon = 0;
//        int notificationColor = Color.DKGRAY;
        try {
            ApplicationInfo info = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            notificationIcon = info.icon;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String channelId = MetaUtil.getInteger(getApplicationContext(), ChannelConfig.GAME_ID) + "";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId);
        builder.setSmallIcon(notificationIcon)
                .setContentTitle(fileName)
                .setContentText("下载失败")
                .setColor(ContextCompat.getColor(getApplicationContext(), ResourceManager.getColor(getApplicationContext(), "colorPrimary")))
//                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setOngoing(false)//值为true时不能被clear
                .setProgress(100, 0, false)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat.from(this).notify(contentId, builder.build());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(channelId, getPackageName(), NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private void initNotification(int finishPercent, int id, Uri uri, String contentType) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

//        Intent intent = new Intent();//不做任何跳转;但是pendingIntent必须要设置,否则通知点击不会自动消失
        Intent intent = getInstallIntent(uri, contentType);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        int notificationIcon = 0;
//        int notificationColor = Color.DKGRAY;
        try {
            ApplicationInfo info = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            notificationIcon = info.icon;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String channelId = MetaUtil.getInteger(getApplicationContext(), ChannelConfig.GAME_ID) + "";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId);
        builder.setSmallIcon(notificationIcon)
                .setContentTitle(fileName)
                .setContentText(finishPercent + "%")
                .setColor(ContextCompat.getColor(getApplicationContext(), ResourceManager.getColor(getApplicationContext(), "colorPrimary")))
                .setAutoCancel(finishPercent >= 100)
                .setOngoing(finishPercent < 100)//进度不到100不能被clear(值为true时不能被clear)
                .setProgress(100, finishPercent, false)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        if (finishPercent >= 100) {
            //下载完成才允许点击安装
            LogUtil.e("设置安装意图");
            builder.setContentIntent(pendingIntent);
        }

        NotificationManagerCompat.from(this).notify(id, builder.build());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //IMPORTANCE_LOW 静默通知,如果是default, 会有震动
            NotificationChannel notificationChannel = new NotificationChannel(channelId, getPackageName(), NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(notificationChannel);
        }

    }

//    private static DownloadListener listener;
//
//    public static void setDownloadListener(DownloadListener downloadListener) {
//        listener = downloadListener;
//    }


    private static class NotificationObj {
        private Uri uri;
        private String contentType;

        public Uri getUri() {
            return uri;
        }

        public NotificationObj setUri(Uri uri) {
            this.uri = uri;
            return this;
        }

        public String getContentType() {
            return contentType;
        }

        public NotificationObj setContentType(String contentType) {
            this.contentType = contentType;
            return this;
        }
    }
}

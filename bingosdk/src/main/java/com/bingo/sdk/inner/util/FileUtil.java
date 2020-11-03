package com.bingo.sdk.inner.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.bingo.sdk.constants.Constants;
import com.bingo.sdk.inner.bean.DownloadUri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileUtil {
    private static final String DIR_NAME = "bingogame";
    private static final String PRIVATE_DIR_NAME = "device_data";

    public static DownloadUri getDownloadFileForQ(Context context, String fileName, String mimeType) {
        DownloadUri downloadUri = new DownloadUri();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Uri uri = MediaStore.Downloads.EXTERNAL_CONTENT_URI;

//            LogUtil.e("media store path:" + uri);

            ContentResolver resolver = context.getContentResolver();

            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DATE_MODIFIED, System.currentTimeMillis());
            values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
            String path = Environment.DIRECTORY_DOWNLOADS + "/" + DIR_NAME + "/";
            values.put(MediaStore.Downloads.RELATIVE_PATH, path);
            values.put(MediaStore.Downloads.IS_PENDING, true);//重要,如果不加这个,insert会返回null
            values.put(MediaStore.Downloads.MIME_TYPE, mimeType);
            Uri pending = MediaStore.setIncludePending(uri);
            Uri insertUri = resolver.insert(pending, values);
            if (insertUri != null) {
                String uriString = insertUri.toString();
                LogUtil.i("保存路径uri: " + uriString);
                String[] split = uriString.split("/");
                downloadUri.setUri(insertUri);
                int contentId = Integer.parseInt(split[split.length - 1]);
                downloadUri.setContentId(contentId);
            } else {
                LogUtil.e("插入下载uri失败");
            }
        }
        return downloadUri;
    }

    public static DownloadUri getDownloadFilePreQ(String fileName) {
        DownloadUri downloadUri = new DownloadUri();
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File dirFile = new File(file, DIR_NAME);
        LogUtil.e("文件夹: " + dirFile.getPath());
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            boolean created = dirFile.mkdirs();
            if (!created) {
                LogUtil.e("创建文件夹失败");
            }
        }

        File downloadFile = new File(dirFile, fileName);

        if (downloadFile.exists() && downloadFile.isFile()) {
            boolean delete = downloadFile.delete();
            LogUtil.e("文件已存在,删除: " + delete);
        }

        if (!downloadFile.exists() || !downloadFile.isFile()) {
            boolean created = false;
            try {
                created = downloadFile.createNewFile();
                LogUtil.e("文件创建结果: " + created + "\t" + downloadFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!created) {
                LogUtil.e("创建文件: " + downloadFile + "失败");
            } else {
                downloadUri.setFile(downloadFile);
            }
        }

        long time = System.currentTimeMillis();
        //用时间格式化来当成任务id,保证每个任务都有单独的notification
        SimpleDateFormat format = new SimpleDateFormat("ddHHmmss", Locale.getDefault());
        String s = format.format(new Date(time));
        int id = Integer.parseInt(s);
        downloadUri.setContentId(id);
        return downloadUri;
    }

//    public static Uri getDeviceCodeFileUriForQ(Context context, String fileName) {
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            Uri uri = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
//
////            LogUtil.e("media store path:" + uri);
//
//            ContentResolver resolver = context.getContentResolver();
//
//            ContentValues values = new ContentValues();
//            values.put(MediaStore.Downloads.DATE_MODIFIED, System.currentTimeMillis());
//            values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
//            String path = Environment.DIRECTORY_DOWNLOADS + "/" + DIR_NAME + "/" + PRIVATE_DIR_NAME + "/";
//            LogUtil.e("path: " + path);
//            values.put(MediaStore.Downloads.RELATIVE_PATH, path);
//            values.put(MediaStore.Downloads.OWNER_PACKAGE_NAME, context.getPackageName());
//            values.put(MediaStore.Downloads.IS_PENDING, true);//重要,如果不加这个,insert会返回null
////            values.put(MediaStore.Downloads.MIME_TYPE, MimeType.TXT);//加了这个 会默认加上后缀名
//            Uri pending = MediaStore.setIncludePending(uri);
//            Uri insertUri = resolver.insert(pending, values);
//            if (insertUri != null) {
//                String uriString = insertUri.toString();
//                LogUtil.i("保存路径uri: " + uriString);
//                return insertUri;
//            } else {
//                LogUtil.e("插入device code uri失败");
//            }
//        }
//        return null;
//    }

    private static File getDeviceCodeFileUriPreQ() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File dirFile = new File(file, DIR_NAME + "/" + PRIVATE_DIR_NAME);
            LogUtil.e("文件夹: " + dirFile.getPath());
            if (!dirFile.exists() || !dirFile.isDirectory()) {
                boolean created = dirFile.mkdirs();
                if (!created) {
                    LogUtil.e("创建文件夹失败");
                }
            }

            File deviceFile = new File(dirFile, Constants.DEVICE_CODE_FILE_NAME);

            if (deviceFile.exists() && deviceFile.isFile()) {
                LogUtil.e("文件已存在 ");
                return deviceFile;
            } else {
                boolean created = false;
                try {
                    created = deviceFile.createNewFile();
                    LogUtil.e("文件创建结果: " + created + "\t" + deviceFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (!created) {
                    LogUtil.e("创建文件: " + deviceFile + "失败");
                    return null;
                } else {
                    return deviceFile;
                }
            }

        }
        return null;
    }

    public static String getDeviceCodeFromFile() {
        File file = FileUtil.getDeviceCodeFileUriPreQ();
        if (file == null) {
            return "";
        }
        String code = "";
        StringBuilder builder = new StringBuilder();
        InputStreamReader reader = null;
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            char[] buf = new char[1024];
            for (; ; ) {
                int size = reader.read(buf, 0, buf.length);
                if (size < 0) {
                    break;
                }
                builder.append(buf);
            }

            code = builder.toString().trim();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStream!=null ){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return code;
    }

    public static void saveDeviceCodeToFile(String newCode) {
        File file = getDeviceCodeFileUriPreQ();
        if (file == null) {
            Log.e("bingo", "can't create device code file");
            return;
        }

        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            outputStream.write(newCode.getBytes());
            outputStream.flush();
            LogUtil.i("sync code to external file success");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

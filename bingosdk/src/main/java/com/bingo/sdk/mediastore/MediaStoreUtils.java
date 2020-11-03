package com.bingo.sdk.mediastore;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import com.bingo.sdk.constants.MimeType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author #Suyghur.
 * @date 2020/6/4
 */
public class MediaStoreUtils {

    public static final String BINGO_GAME = "bingo_game";

//    public static final String USER_C_HEAD = "USER_C";
//
//    public static final String USER_C_IMG = "USER_C.png";
//
//    public static final String UTMA_C_HEAD = "UTMA_C";

    public static final String DEVICE_CODE_IMG = "Device.png";
    public static final String DEVICE_CODE_HEAD = "Device";

//    public static final String TKID_C_HEAD = "TKID_C";
//
//    public static final String TKID_C_IMG = "TKID_C.png";
//
//    public static final String UUID_C_HEAD = "UUID_C";
//
//    public static final String UUID_C_IMG = "UUID_C.png";

    public static final String PICTURES_PATH = "Pictures" + File.separator + BINGO_GAME;


    public static boolean externalStorageLegacyEnable() {
        if (Build.VERSION.SDK_INT >= 29) {
            return Environment.isExternalStorageLegacy();
        } else {
            return true;
        }
    }

    public static Cursor getMediaStoreCursor(Context context, Uri external, String selection, String[] projection, String[] args) {
        if (null == external) {
            return null;
        }
        return context.getContentResolver().query(external, projection, selection, args, null);
    }

    public static void closeCursor(Cursor cursor) {
        if (null != cursor) {
            cursor.close();
        }
    }

    /**
     * 创建图片uri
     *
     * @param context
     * @param name
     * @return
     */
    public static Uri createInfosImageUri(Context context, String name) {
        Uri uri = null;
        if (Build.VERSION.SDK_INT >= 29) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, name);
            values.put(MediaStore.Images.Media.MIME_TYPE, MimeType.PNG);
            values.put(MediaStore.Images.Media.TITLE, name);
            values.put(MediaStore.Images.Media.RELATIVE_PATH, PICTURES_PATH);
            values.put(MediaStore.Images.Media.OWNER_PACKAGE_NAME, context.getPackageName());
            uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        }
        return uri;
    }


    public static boolean updateInfosImageUri(Context context, String name, Uri oldUri) {
        if (Build.VERSION.SDK_INT >= 29) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, name);
            values.put(MediaStore.Images.Media.MIME_TYPE, MimeType.PNG);
            values.put(MediaStore.Images.Media.TITLE, name);
            values.put(MediaStore.Images.Media.RELATIVE_PATH, PICTURES_PATH);
            values.put(MediaStore.Images.Media.OWNER_PACKAGE_NAME, context.getPackageName());
            return context.getContentResolver().update(oldUri, values, null, null) == 1;
        }
        return false;
    }


    public static boolean deleteOwnImage(Context context, Uri uri) {
        if (null == uri) {
            return false;
        } else {
            return context.getContentResolver().delete(uri, null, null) == 1;
        }
    }

    public static void copyPri2Pub(Context context, String priFilePath, Uri uri) {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(new File(priFilePath));
            if (null != uri) {
                out = context.getContentResolver().openOutputStream(uri);
            }
            if (null != out) {
                byte[] buffer = new byte[1024];
                int byteCount = 0;
                while ((byteCount = in.read(buffer)) != -1) {
                    out.write(buffer, 0, byteCount);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != in) {
                    in.close();
                }
                if (null != out) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Bitmap createBitmap() {
        return Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888);

    }

}

package com.bingo.sdk.mediastore;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import com.bingo.sdk.constants.MimeType;
import com.bingo.sdk.inner.util.FileUtils;
import com.bingo.sdk.inner.util.LogUtil;
import com.bingo.sdk.inner.util.SDCardUtils;
import com.bingo.sdk.string2pic.String2Png;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;


/**
 * @author #Suyghur.
 * @date 2020/6/10
 */
public class DeviceIdImageUtils {


    /**
     * 搜索最新的Device.png
     *
     * @param context
     * @return
     */
    public static Uri getLastModifyDeviceCodeImageUri(Context context) {
        Uri uri = null;
        if (Build.VERSION.SDK_INT >= 29) {
            long lastModifyTs = 0;
            Uri external = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            String[] projection = new String[]{
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.DATE_MODIFIED,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
            String selection = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + "=? AND " + MediaStore.Images.Media.MIME_TYPE + " =?";
            String[] args = new String[]{MediaStoreUtils.BINGO_GAME, MimeType.PNG};
            Cursor cursor = MediaStoreUtils.getMediaStoreCursor(context, external, selection, projection, args);
            if (null != cursor) {
                while (cursor.moveToNext()) {
                    String name = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
                    if (name.startsWith(MediaStoreUtils.DEVICE_CODE_HEAD)) {
                        long date = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED));
                        if (date >= lastModifyTs) {
                            lastModifyTs = date;
                            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
                            uri = Uri.parse(external + File.separator + id);
                        }
                    }
                }
            }
            MediaStoreUtils.closeCursor(cursor);
        }
        return uri;
    }

    /**
     * 搜索自己署名的device.png
     *
     * @param context
     * @return
     */
    public static Uri getOwnedDeviceCodeImageUri(Context context) {
        Uri uri = null;
        if (Build.VERSION.SDK_INT >= 29) {
            Uri external = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            String[] projection = new String[]{
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.OWNER_PACKAGE_NAME,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
            String selection = MediaStore.Images.Media.BUCKET_DISPLAY_NAME
                    + " =? AND " + MediaStore.Images.Media.MIME_TYPE
                    + " =? AND " + MediaStore.Images.Media.OWNER_PACKAGE_NAME
                    + " =?";
            String[] args = new String[]{MediaStoreUtils.BINGO_GAME, MimeType.PNG, context.getPackageName()};
            Cursor cursor = MediaStoreUtils.getMediaStoreCursor(context, external, selection, projection, args);
            if (null != cursor) {
                while (cursor.moveToNext()) {
                    String name = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
                    if (name.startsWith(MediaStoreUtils.DEVICE_CODE_HEAD)) {
                        int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
                        uri = Uri.parse(external + File.separator + id);
                    }
                }
            }
            MediaStoreUtils.closeCursor(cursor);
        }
        return uri;
    }

    private static void syncImage2PublicBelowQ(String priPath, String pubPath) {
        File priFile = new File(priPath);
        File pubFile = new File(pubPath);
        FileUtils.copyFile(priFile, pubFile);
    }

    /**
     * 同步deviceCode到公有目录
     *
     * @param context
     * @param deviceCode
     */
    public static void syncDeviceCode2Public(Context context, String deviceCode) {
        LogUtil.d("sync device code image to public");
        //同步device code
        String priPath = context.getExternalFilesDir(MediaStoreUtils.BINGO_GAME).getAbsolutePath() + File.separator + MediaStoreUtils.DEVICE_CODE_IMG;
        String pubPath = Environment.getExternalStorageDirectory() + File.separator + MediaStoreUtils.PICTURES_PATH + File.separator + MediaStoreUtils.DEVICE_CODE_IMG;
        File file = new File(priPath);
        //私有目录下覆盖写图片
        FileUtils.createPngInPrivate(file);
        //私有目录下Device.png植入device code
        String2Png.insertStr2Bitmap(priPath, deviceCode, priPath);
        if (Build.VERSION.SDK_INT < 29) {
            //低于AndroidQ直接拷贝覆盖
            if (SDCardUtils.checkExternalStorageCanWrite()) {
                syncImage2PublicBelowQ(priPath, pubPath);
            } else {
                LogUtil.d("SDCard can not be written");
            }
        } else {
            syncDeviceCodePublicAboveQ(context, file);
        }
    }

    private static void syncDeviceCodePublicAboveQ(Context context, File file) {
        //获取自己署名的图片uri
        Uri uri = getOwnedDeviceCodeImageUri(context);
        if (null != uri) {
            MediaStoreUtils.copyPri2Pub(context, file.getAbsolutePath(), uri);
            String newName = MediaStoreUtils.DEVICE_CODE_HEAD + "_" + System.currentTimeMillis();
            MediaStoreUtils.updateInfosImageUri(context, newName, uri);
        } else {
            String newName = MediaStoreUtils.DEVICE_CODE_HEAD + "_" + System.currentTimeMillis();
            Uri insertUri = MediaStoreUtils.createInfosImageUri(context, newName);
            MediaStoreUtils.copyPri2Pub(context, file.getAbsolutePath(), insertUri);

        }

    }

    /**
     * 从图片解析出device code
     *
     * @param context
     * @return
     */
    public static String parseDeviceCodeByLastModifyImage(Context context) {
        Uri uri = getLastModifyDeviceCodeImageUri(context);
        String code = "";
        if (null != uri) {
            InputStream in = null;
            try {
                in = context.getContentResolver().openInputStream(uri);
                code = String2Png.parseStrByStream(in);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return code;
    }

//    /**
//     * 搜索最新的TKID_C_TS.png或TKID_C.png
//     *
//     * @param context
//     * @return
//     */
//    public static Uri getLastModifyTkidImageUri(Context context) {
//        Uri uri = null;
//        if (Build.VERSION.SDK_INT >= 29) {
//            long lastModifyTs = 0;
//            Uri external = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//            String[] projection = new String[]{
//                    MediaStore.Images.Media._ID,
//                    MediaStore.Images.Media.DISPLAY_NAME,
//                    MediaStore.Images.Media.DATE_MODIFIED,
//                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
//            String selection = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + "=? AND " + MediaStore.Images.Media.MIME_TYPE + " =?";
//            String[] args = new String[]{MediaStoreUtils.BINGO_GAME, MimeType.PNG};
//            Cursor cursor = MediaStoreUtils.getMediaStoreCursor(context, external, selection, projection, args);
//            if (null != cursor) {
//                while (cursor.moveToNext()) {
//                    String name = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
//                    if (name.startsWith(MediaStoreUtils.TKID_C_HEAD)) {
//                        long date = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED));
//                        if (date >= lastModifyTs) {
//                            lastModifyTs = date;
//                            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
//                            uri = Uri.parse(external + File.separator + id);
//                        }
//                    }
//                }
//            }
//            MediaStoreUtils.closeCursor(cursor);
//        }
//        return uri;
//    }

//    /**
//     * 搜索自己署名的TKID_C_TS_.png
//     *
//     * @param context
//     * @return
//     */
//    public static Uri getOwnTkidImageUri(Context context) {
//        Uri uri = null;
//        if (Build.VERSION.SDK_INT >= 29) {
//            Uri external = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//            String[] projection = new String[]{
//                    MediaStore.Images.Media._ID,
//                    MediaStore.Images.Media.DISPLAY_NAME,
//                    MediaStore.Images.Media.OWNER_PACKAGE_NAME,
//                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
//            String selection = MediaStore.Images.Media.BUCKET_DISPLAY_NAME
//                    + " =? AND " + MediaStore.Images.Media.MIME_TYPE
//                    + " =? AND " + MediaStore.Images.Media.OWNER_PACKAGE_NAME
//                    + " =?";
//            String[] args = new String[]{MediaStoreUtils.BINGO_GAME, MimeType.PNG, context.getPackageName()};
//            Cursor cursor = MediaStoreUtils.getMediaStoreCursor(context, external, selection, projection, args);
//            if (null != cursor) {
//                while (cursor.moveToNext()) {
//                    String name = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
//                    if (name.startsWith(MediaStoreUtils.TKID_C_HEAD)) {
//                        int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
//                        uri = Uri.parse(external + File.separator + id);
//                    }
//                }
//            }
//            MediaStoreUtils.closeCursor(cursor);
//        }
//        return uri;
//    }

//    /**
//     * 同步tkid到公有目录
//     *
//     * @param context
//     * @param tkid
//     */
//    public static void synTkid2Public(Context context, String tkid) {
//        LogUtil.d("sys tkid image 2 public");
//        //同步未加密的utma
//        String priPath = context.getExternalFilesDir(MediaStoreUtils.BINGO_GAME).getAbsolutePath() + File.separator + MediaStoreUtils.TKID_C_IMG;
//        String pubPath = Environment.getExternalStorageDirectory() + File.separator + MediaStoreUtils.PICTURES_PATH + File.separator + MediaStoreUtils.TKID_C_IMG;
//        File file = new File(priPath);
//        //私有目录下覆盖写图片
//        FileUtils.createPngInPrivate(file);
//        //私有目录下TKID_C.png植入tkid
//        String2Png.insertStr2Bitmap(priPath, tkid, priPath);
//        if (Build.VERSION.SDK_INT < 29) {
//            //低于AndroidQ直接拷贝覆盖
//            if (SDCardUtils.checkExternalStorageCanWrite()) {
//                synImage2PublicBelowQ(priPath, pubPath);
//            } else {
//                LogUtil.d("SDCard can not write");
//            }
//        } else {
//            synTkid2PublicAboveQ(context, file);
//        }
//    }

//    private static void synTkid2PublicAboveQ(Context context, File file) {
//        //获取自己署名的图片uri
//        Uri uri = getOwnTkidImageUri(context);
//        if (null != uri) {
//            MediaStoreUtils.copyPri2Pub(context, file.getAbsolutePath(), uri);
//            String newName = MediaStoreUtils.TKID_C_HEAD + "_" + System.currentTimeMillis();
//            MediaStoreUtils.updateInfosImageUri(context, newName, uri);
//        } else {
//            String newName = MediaStoreUtils.TKID_C_HEAD + "_" + System.currentTimeMillis();
//            Uri insertUri = MediaStoreUtils.createInfosImageUri(context, newName);
//            MediaStoreUtils.copyPri2Pub(context, file.getAbsolutePath(), insertUri);
//        }
//
//    }

//    public static String parseTkidByLastModifyImage(Context context) {
//        Uri uri = getLastModifyTkidImageUri(context);
//        String tkid = "";
//        if (null != uri) {
//            InputStream in = null;
//            try {
//                in = context.getContentResolver().openInputStream(uri);
//                tkid = String2Png.parseStrByStream(in);
//            } catch (Exception e) {
//                e.printStackTrace();
//            } finally {
//                if (in != null) {
//                    try {
//                        in.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
//        return tkid;
//    }

//    /**
//     * 搜索最新的UUID_C_TS.png或UUID_C.png
//     *
//     * @param context
//     * @return
//     */
//    public static Uri getLastModifyUuidImageUri(Context context) {
//        Uri uri = null;
//        if (Build.VERSION.SDK_INT >= 29) {
//            long lastModifyTs = 0;
//            Uri external = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//            String[] projection = new String[]{
//                    MediaStore.Images.Media._ID,
//                    MediaStore.Images.Media.DISPLAY_NAME,
//                    MediaStore.Images.Media.DATE_MODIFIED,
//                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
//            String selection = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + "=? AND " + MediaStore.Images.Media.MIME_TYPE + " =?";
//            String[] args = new String[]{MediaStoreUtils.BINGO_GAME, MimeType.PNG};
//            Cursor cursor = MediaStoreUtils.getMediaStoreCursor(context, external, selection, projection, args);
//            if (null != cursor) {
//                while (cursor.moveToNext()) {
//                    String name = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
//                    if (name.startsWith(MediaStoreUtils.UUID_C_HEAD)) {
//                        long date = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED));
//                        if (date >= lastModifyTs) {
//                            lastModifyTs = date;
//                            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
//                            uri = Uri.parse(external + File.separator + id);
//                        }
//                    }
//                }
//            }
//            MediaStoreUtils.closeCursor(cursor);
//        }
//        return uri;
//    }

//    /**
//     * 搜索自己署名的UUID_C_TS_.png
//     *
//     * @param context
//     * @return
//     */
//    public static Uri getOwnUuidImageUri(Context context) {
//        Uri uri = null;
//        if (Build.VERSION.SDK_INT >= 29) {
//            Uri external = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//            String[] projection = new String[]{
//                    MediaStore.Images.Media._ID,
//                    MediaStore.Images.Media.DISPLAY_NAME,
//                    MediaStore.Images.Media.OWNER_PACKAGE_NAME,
//                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
//            String selection = MediaStore.Images.Media.BUCKET_DISPLAY_NAME
//                    + " =? AND " + MediaStore.Images.Media.MIME_TYPE
//                    + " =? AND " + MediaStore.Images.Media.OWNER_PACKAGE_NAME
//                    + " =?";
//            String[] args = new String[]{MediaStoreUtils.BINGO_GAME, MimeType.PNG, context.getPackageName()};
//            Cursor cursor = MediaStoreUtils.getMediaStoreCursor(context, external, selection, projection, args);
//            if (null != cursor) {
//                while (cursor.moveToNext()) {
//                    String name = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
//                    if (name.startsWith(MediaStoreUtils.UUID_C_HEAD)) {
//                        int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
//                        uri = Uri.parse(external + File.separator + id);
//                    }
//                }
//            }
//            MediaStoreUtils.closeCursor(cursor);
//        }
//        return uri;
//    }

//    /**
//     * 同步uuid到公有目录
//     *
//     * @param context
//     * @param uuid
//     */
//    public static void synUuid2Public(Context context, String uuid) {
//        LogUtil.d("sys uuid image 2 public");
//        //同步未加密的utma
//        String priPath = context.getExternalFilesDir(MediaStoreUtils.BINGO_GAME).getAbsolutePath() + File.separator + MediaStoreUtils.UUID_C_IMG;
//        String pubPath = Environment.getExternalStorageDirectory() + File.separator + MediaStoreUtils.PICTURES_PATH + File.separator + MediaStoreUtils.UUID_C_IMG;
//        File file = new File(priPath);
//        //私有目录下覆盖写图片
//        FileUtils.createPngInPrivate(file);
//        //私有目录下UUID_C.png植入uuid
//        String2Png.insertStr2Bitmap(priPath, uuid, priPath);
//        if (Build.VERSION.SDK_INT < 29) {
//            //低于AndroidQ直接拷贝覆盖
//            if (SDCardUtils.checkExternalStorageCanWrite()) {
//                synImage2PublicBelowQ(priPath, pubPath);
//            } else {
//                LogUtil.d("SDCard can not write");
//            }
//        } else {
//            synUuid2PublicAboveQ(context, file);
//        }
//    }

//    private static void synUuid2PublicAboveQ(Context context, File file) {
//        //获取自己署名的图片uri
//        Uri uri = getOwnUuidImageUri(context);
//        if (null != uri) {
//            MediaStoreUtils.copyPri2Pub(context, file.getAbsolutePath(), uri);
//            String newName = MediaStoreUtils.UUID_C_HEAD + "_" + System.currentTimeMillis();
//            MediaStoreUtils.updateInfosImageUri(context, newName, uri);
//        } else {
//            String newName = MediaStoreUtils.UUID_C_HEAD + "_" + System.currentTimeMillis();
//            Uri insertUri = MediaStoreUtils.createInfosImageUri(context, newName);
//            MediaStoreUtils.copyPri2Pub(context, file.getAbsolutePath(), insertUri);
//
//        }
//
//    }


//    public static String parseUuidByLastModifyImage(Context context) {
//        Uri uri = getLastModifyUuidImageUri(context);
//        String uuid = "";
//        if (null != uri) {
//            InputStream in = null;
//            try {
//                in = context.getContentResolver().openInputStream(uri);
//                uuid = String2Png.parseStrByStream(in);
//            } catch (Exception e) {
//                e.printStackTrace();
//            } finally {
//                if (in != null) {
//                    try {
//                        in.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
//        return uuid;
//    }


}

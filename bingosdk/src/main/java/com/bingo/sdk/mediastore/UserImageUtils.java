package com.bingo.sdk.mediastore;

/**
 * @author #Suyghur.
 * @date 2020/6/10
 */
public class UserImageUtils {

//    /**
//     * 获取最新的USER_C_XXXX.png的Uri
//     *
//     * @param context
//     * @return
//     */
//    public static Uri getLastModifyUserImageUri(Context context) {
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
//            String[] args = new String[]{MediaStoreUtils.FUSE_INFOS, MediaStoreUtils.PNG_MIME_TYPE};
//            android.database.Cursor cursor = MediaStoreUtils.getMediaStoreCursor(context, external, selection, projection, args);
//            if (null != cursor) {
//                while (cursor.moveToNext()) {
//                    String name = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
//                    LogUtil.d(name);
//                    if (name.startsWith(MediaStoreUtils.USER_C_HEAD)) {
//                        long date = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED));
//                        LogUtil.d(date + "");
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

//    public static Uri getOwnUsersImageUri(Context context) {
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
//            String[] args = new String[]{MediaStoreUtils.FUSE_INFOS, MediaStoreUtils.PNG_MIME_TYPE, context.getPackageName()};
//            android.database.Cursor cursor = MediaStoreUtils.getMediaStoreCursor(context, external, selection, projection, args);
//            if (null != cursor) {
//                while (cursor.moveToNext()) {
//                    String name = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
//                    LogUtil.d(name);
//                    if (name.startsWith(MediaStoreUtils.USER_C_HEAD)) {
//                        int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
//                        uri = Uri.parse(external + File.separator + id);
//                    }
//                }
//            }
//            MediaStoreUtils.closeCursor(cursor);
//        }
//        return uri;
//    }

//    public static void synUserInfo2Public(Context context, String userInfo) {
//        LogUtil.d("sys user info image 2 public");
//        //同步未加密的utma
//        String priPath = context.getExternalFilesDir(MediaStoreUtils.FUSE_INFOS).getAbsolutePath() + File.separator + MediaStoreUtils.USER_C_IMG;
//        String pubPath = Environment.getExternalStorageDirectory() + File.separator + MediaStoreUtils.PICTURES_PATH + File.separator + MediaStoreUtils.USER_C_IMG;
//        File file = new File(priPath);
//        //私有目录下覆盖写图片
//        FileUtils.createPngInPrivate(file);
//        //私有目录下UTMA_C.png植入utma
//        String2Png.insertStr2Bitmap(priPath, userInfo, priPath);
//        if (Build.VERSION.SDK_INT < 29) {
//            //低于AndroidQ直接拷贝覆盖
//            if (SDCardUtils.checkExternalStorageCanWrite()) {
//                synImage2PublicBelowQ(priPath, pubPath);
//            } else {
//                LogUtil.d("SDCard can not write");
//            }
//        } else {
//            synUserInfo2PublicAboveQ(context, file);
//        }
//    }

//    private static void synUserInfo2PublicAboveQ(Context context, File file) {
//        //获取自己署名的图片uri
//        Uri uri = getOwnUsersImageUri(context);
//        if (null != uri) {
//            MediaStoreUtils.copyPri2Pub(context, file.getAbsolutePath(), uri);
//            String newName = MediaStoreUtils.USER_C_HEAD + "_" + System.currentTimeMillis();
//            MediaStoreUtils.updateInfosImageUri(context, newName, uri);
//        } else {
//            String newName = MediaStoreUtils.USER_C_HEAD + "_" + System.currentTimeMillis();
//            Uri insertUri = MediaStoreUtils.createInfosImageUri(context, newName);
//            MediaStoreUtils.copyPri2Pub(context, file.getAbsolutePath(), insertUri);
//
//        }
//
//    }

//    public static String parseUserInfoByLastModifyImage(Context context) {
//        Uri uri = getLastModifyUserImageUri(context);
//        String userInfo = "";
//        if (null != uri) {
//            InputStream in = null;
//            try {
//                in = context.getContentResolver().openInputStream(uri);
//                userInfo = String2Png.parseStrByStream(in);
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
//        return userInfo;
//    }

//    private static void synImage2PublicBelowQ(String priPath, String pubPath) {
//        File priFile = new File(priPath);
//        File pubFile = new File(pubPath);
//        FileUtils.copyFile(priFile, pubFile);
//    }

}

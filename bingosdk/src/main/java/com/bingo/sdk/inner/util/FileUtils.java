package com.bingo.sdk.inner.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


/**
 * Author: LiPing<br/>
 * Time: 18/5/10 下午4:22<br/>
 * 文件操作工具
 */
public class FileUtils {


    /**
     * 国内融合新版本迁移通用用户数据
     */
    public final static String KKK_UTMA_INF = "UTMA_C.DAT";
    /**
     * 新版设备标识
     */
    public final static String KKK_TKID_INF = "TKID_C.DAT";
    /**
     * 国内融合UUID
     */
    public final static String KKK_UUID_INF = "UUID_C.DAT";
    /**
     * 国内融合GUID
     */
    public final static String KKK_GUID_INF = "GUID_C.DAT";

    /**
     * 国内融合用户资料存放路径
     */
    public final static String INFO_DIR = "/Android/data/3kwan/";


    public final static String PERMISSION_WRITE_EXTERNAL_STORAGE = "android.permission.WRITE_EXTERNAL_STORAGE";
    public final static String PERMISSION_READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE";


    private FileUtils() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }


    /**
     * 判断是否有权限
     *
     * @param mContext
     * @param permission
     * @return
     */
    public static boolean hasPermission(Context mContext, String permission) {
        int perm = mContext.checkCallingOrSelfPermission(permission);
        return perm == PackageManager.PERMISSION_GRANTED;
    }


    /**
     * 写入app缓存
     *
     * @param mContext
     * @param content
     */
    public static synchronized void write2AppCache(Context mContext, String fileName2, String content) {
        FileOutputStream out = null;
        BufferedWriter writer = null;
        try {
            //设置文件名称，以及存储方式
            out = mContext.openFileOutput(fileName2, Context.MODE_PRIVATE);
            //创建一个OutputStreamWriter对象，传入BufferedWriter的构造器中
            writer = new BufferedWriter(new OutputStreamWriter(out));
            //向文件中写入数据
            writer.write(content);
        } catch (IOException e) {
            try {
                if (writer != null) {
                    writer.close();
                    writer = null;
                }
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            try {
                if (out != null) {
                    out.close();
                    out = null;
                }
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                    writer = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (out != null) {
                    out.close();
                    out = null;
                }
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
    }

    public static boolean copyFile(File src, File des) {
        if (!src.exists()) {
            LogUtil.e("file not exist:" + src.getAbsolutePath());
            return false;
        }
        if (!des.getParentFile().isDirectory() && !des.getParentFile().mkdirs()) {
            LogUtil.e("mkdir failed:" + des.getParent());
            return false;
        }
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(src));
            bos = new BufferedOutputStream(new FileOutputStream(des));
            byte[] buffer = new byte[4 * 1024];
            int count;
            while ((count = bis.read(buffer, 0, buffer.length)) != -1) {
                if (count > 0) {
                    bos.write(buffer, 0, count);
                }
            }
            bos.flush();
            return true;
        } catch (Exception e) {
            LogUtil.e("exception:" + e.getMessage());
            e.printStackTrace();
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     * 从app缓存读数据
     *
     * @param mContext
     * @param fileName
     * @return
     */
    public static String get2AppCache(Context mContext, String fileName) {
        String[] files = mContext.fileList();
        if (files == null || files.length == 0) {
            //LogUtil.d("get2AppCache file下没有任何文件");
            return null;
        }
        boolean fileExists = false;
        for (String file : files) {
            //LogUtil.d("get2AppCache file目录的文件：" + file);
            if (file.equals(fileName)) {
                fileExists = true;
            }
        }
        if (!fileExists) {
            //LogUtil.d("get2AppCache file目录不存在文件：" + fileName);
            return null;
        }
        FileInputStream in = null;
        BufferedReader reader = null;
        StringBuilder content = null;
        String data = null;
        try {
            //设置将要打开的存储文件名称
            in = mContext.openFileInput(fileName);
            //FileInputStream -> InputStreamReader ->BufferedReader
            reader = new BufferedReader(new InputStreamReader(in));
            String line = null;
            //读取每一行数据，并追加到StringBuilder对象中，直到结束
            while ((line = reader.readLine()) != null) {
                if (content == null) {
                    content = new StringBuilder();
                }
                content.append(line);
            }
            if (content != null)
                data = content.toString();
        } catch (IOException e) {
            e.printStackTrace();
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return data;
    }


    /**
     * 创建文件夹
     *
     * @param dirPath 文件夹完整路径
     */
    public static File mkdirs(String dirPath) {
        if (TextUtils.isEmpty(dirPath)) {
            return null;
        }
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    /**
     * 创建文件
     *
     * @param filePath 文件完整路径包括文件名
     */
    public static File createNewFile(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        filePath = filePath.replace("\\", "/");
        String dirPath = filePath.substring(0, filePath.lastIndexOf("/"));
        mkdirs(dirPath);
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

        }
        return file;
    }

    /**
     * 删除文件
     *
     * @param filePath 需要删除文件的完整路径包括文件名
     */
    public static boolean deleteFile(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }
        File file = new File(filePath);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    /**
     * 删除文件夹以及所有子目录和文件
     *
     * @param path 完整路径
     */
    public static boolean deleteDirectory(String path) {

        if (TextUtils.isEmpty(path)) {
            return false;
        }

        boolean flag;
        // 如果path不以文件分隔符结尾，自动添加文件分隔符
        if (!path.endsWith(File.separator)) {
            path = path + File.separator;
        }
        File dirFile = new File(path);
        // 如果dir对应的文件不存在，或者不是一个目录，则退出
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        flag = true;
        // 删除文件夹下的所有文件(包括子目录)
        File[] files = dirFile.listFiles();
        if (files != null && files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()) {
                    // 删除子文件
                    flag = deleteFile(files[i].getAbsolutePath());
                    if (!flag)
                        break;
                } else {
                    // 删除子目录
                    flag = deleteDirectory(files[i].getAbsolutePath());
                    if (!flag)
                        break;
                }
            }
        }
        if (!flag)
            return false;
        // 删除当前目录
        if (dirFile.delete()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 重命名文件或文件夹
     *
     * @param filePath 完整路径
     * @param oldName  旧的文件或文件夹名称
     * @param newName  新名称
     */
    public static boolean rename(String filePath, String oldName, String newName) {
        if (!TextUtils.isEmpty(filePath) && !TextUtils.isEmpty(oldName) && !TextUtils.isEmpty(newName)) {
            if (!filePath.endsWith(File.separator)) {
                filePath += File.separator;
            }
            File file = new File(filePath + oldName);
            File newFile = new File(filePath + newName);
            if (file.exists()) {
                return file.renameTo(newFile);
            }
        }
        return false;
    }

    /**
     * 写入文件
     *
     * @param content      写入内容
     * @param filePath     文件完整路径
     * @param isAppend     是否追加
     * @param intervalChar 间隔字符,isApped=false的时候允许传null。
     */
    public static boolean writeStringToFile(String content, String filePath, boolean isAppend, String intervalChar) {
        boolean flag = false;
        if (!TextUtils.isEmpty(content) && !TextUtils.isEmpty(filePath)) {
            if (createNewFile(filePath) != null) {
                File file = new File(filePath);
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(file, isAppend);
                    LogUtil.d("writeStringToFile : " + content);
                    byte[] buffer = content.getBytes();
                    try {
                        fos.write(buffer);
                        if (isAppend && !TextUtils.isEmpty(intervalChar)) {
                            fos.write(intervalChar.getBytes());
                        }
                        fos.flush();
                        flag = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                        if (fos != null) {
                            fos.close();
                            fos = null;
                        }
                    } finally {
                        if (fos != null) {
                            fos.close();
                            fos = null;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } catch (OutOfMemoryError o) {
                    o.printStackTrace();
                }
                return flag;
            }
        }
        return flag;
    }


    /**
     * 将字节流转换成文件
     *
     * @param filepath
     * @param data
     * @throws Exception
     */
    public static void saveFile(String filepath, byte[] data) throws IOException {
        if (data != null) {
            //String filepath = "D:\\" + filename;
            File file = new File(filepath);
            if (file.exists()) {
                file.delete();
            }
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data, 0, data.length);
            fos.flush();
            fos.close();
        }
    }

    /**
     * 读取文件
     *
     * @param filePath 文件的完整路径包括文件名
     */
    public static String readFile(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        BufferedReader reader = null;
        FileInputStream is = null;
        StringBuffer stringBuffer = new StringBuffer();
        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }
        try {
            is = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(is));
            try {
                String data;
                while ((data = reader.readLine()) != null) {
                    stringBuffer.append(data);
                }
            } catch (IOException e) {
                try {
                    if (reader != null) {
                        reader.close();
                        reader = null;
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                try {
                    if (is != null) {
                        is.close();
                        is = null;
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
            } finally {
                try {
                    if (is != null) {
                        is.close();
                        is = null;
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                try {
                    if (reader != null) {
                        reader.close();
                        reader = null;
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError ex) {
            ex.printStackTrace();
        }
        return stringBuffer.toString().trim();
    }


    /**
     * 从assets 文件夹中获取文件并读取数据
     *
     * @param context  上下文
     * @param fileName 文件名
     */
    public static String readFileFromAssets(Context context, String fileName) {
        if (context == null) {
            return null;
        }
        if (TextUtils.isEmpty(fileName)) {
            return null;
        }
        String result = null;
        InputStream in = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            in = context.getResources().getAssets().open(fileName);
            //创建字节数组输出流 ，用来输出读取到的内容
            byteArrayOutputStream = new ByteArrayOutputStream();
            //创建读取缓存,大小为1024
            byte[] buffer = new byte[1024];
            //每次读取长度
            int len = 0;
            //开始读取输入流中的文件
            while ((len = in.read(buffer)) != -1) { //当等于-1说明没有数据可以读取了
                byteArrayOutputStream.write(buffer, 0, len); // 把读取的内容写入到输出流中
            }
            //把读取到的字节数组转换为字符串
            result = byteArrayOutputStream.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    //关闭输入流e
                    in.close();
                }
                if (byteArrayOutputStream != null) {
                    //关闭输出流
                    byteArrayOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static InputStream accessFileFromAssets(Context context, String fileName) {
        InputStream in = null;
        try {
            in = context.getResources().getAssets().open(fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return in;
    }

    /**
     * Load the config file in the META-INF folder in the apk file
     *
     * @param context  上下文对象
     * @param fileName 文件名
     * @return InputStream对象
     */
    public static InputStream accessFileFromMetaInf(Context context, String fileName) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        String sourceDir = applicationInfo.sourceDir;
        ZipFile zipFile;
        InputStream in = null;
        try {
            zipFile = new ZipFile(sourceDir);
            Enumeration entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                String entryName = entry.getName();
                if (entryName.startsWith("META-INF/" + fileName)) {
                    if (entry.getSize() > 0) {
                        in = zipFile.getInputStream(entry);
                    }
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return in;
    }

    public static boolean isExistInMetaInf(Context context, String fileName) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        String sourceDir = applicationInfo.sourceDir;
        ZipFile zipFile;
        try {
            zipFile = new ZipFile(sourceDir);
            Enumeration entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                String entryName = entry.getName();
                if (entryName.startsWith("META-INF/" + fileName)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isExistInAssets(Context context, String fileName) {
        AssetManager assetManager = context.getResources().getAssets();
        try {
            String[] fileNames = assetManager.list("");
            if (fileNames != null && fileNames.length != 0) {
                for (String item : fileNames) {
                    if (fileName.equals(item)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void createPngInPrivate(File file) {
        OutputStream out = null;
        try {
            Bitmap bitmap = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888);
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != out) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static File[] filterPriFileStartWithStr(Context context, String folderName, final String startWithStr) {
        FileFilter fileFilter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                LogUtil.d(pathname.getName());
                return pathname.getName().startsWith(startWithStr);
            }
        };
        return context.getExternalFilesDir(folderName).listFiles(fileFilter);
    }

}

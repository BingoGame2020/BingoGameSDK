package com.bingo.sdk.string2pic;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import com.bingo.sdk.inner.util.ByteUtils;
import com.bingo.sdk.inner.util.LogUtil;
import com.bingo.sdk.string2pic.entity.Png;
import com.bingo.sdk.string2pic.entity.PngHeader;
import com.bingo.sdk.string2pic.entity.block.CommonBlock;
import com.bingo.sdk.string2pic.entity.block.DataBlock;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * 隐藏文件内容到png格式图片中
 *
 * @author #Suyghur.
 * @date 2020/6/9
 */
public class String2Png {

    /**
     * 读取指定png文件的信息
     *
     * @param pngFileName png文件名
     * @return
     */
    private static Png readPng(String pngFileName) {
        Png png = new Png();
        File pngFile = new File(pngFileName);
        InputStream pngIn = null;
        //记录输入流读取位置（字节）
        long pos = 0;
        try {
            pngIn = new FileInputStream(pngFile);
            PngHeader pngHeader = new PngHeader();
            pngIn.read(pngHeader.getFlag());
            png.setPngHeader(pngHeader);
            pos += pngHeader.getFlag().length;
            while (pos < pngFile.length()) {
                DataBlock realDataBlock = null;
                //读取数据块
                DataBlock dataBlock = new CommonBlock();
                //先读取长度，4个字节
                pngIn.read(dataBlock.getLength());
                pos += dataBlock.getLength().length;
                //再读取类型码，4个字节
                pngIn.read(dataBlock.getChunkTypeCode());
                pos += dataBlock.getChunkTypeCode().length;
                //如果有数据再读取数据
                //读取数据
                realDataBlock = BlockFactory.readBlock(pngIn, png, dataBlock);
                pos += ByteUtils.highByteToInt(dataBlock.getLength());
                //读取crc，4个字节
                pngIn.read(realDataBlock.getCrc());
                //添加读取到的数据块
                png.getDataBlocks().add(realDataBlock);
                pos += realDataBlock.getCrc().length;
                dataBlock = null;
            }
        } catch (IOException e) {
            LogUtil.e(e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (pngIn != null) {
                    pngIn.close();
                }
            } catch (IOException e) {
                LogUtil.e(e.getMessage());
                e.printStackTrace();
            }
        }
        return png;
    }

    /**
     * 读取指定png文件的信息
     *
     * @return
     */
    public static Png readPngByStream(InputStream in) {
        Png png = new Png();
        //记录输入流读取位置(字节为单位)
        long pos = 0;
        try {
            int streamLength = in.available();
            //读取头部信息
            PngHeader pngHeader = new PngHeader();
            in.read(pngHeader.getFlag());
            png.setPngHeader(pngHeader);
            pos += pngHeader.getFlag().length;

            while (pos < streamLength) {
                DataBlock realDataBlock = null;
                //读取数据块
                DataBlock dataBlock = new CommonBlock();
                //先读取长度，4个字节
                in.read(dataBlock.getLength());
                pos += dataBlock.getLength().length;
                //再读取类型码，4个字节
                in.read(dataBlock.getChunkTypeCode());
                pos += dataBlock.getChunkTypeCode().length;
                //如果有数据再读取数据
                //读取数据
                realDataBlock = BlockFactory.readBlock(in, png, dataBlock);
                pos += ByteUtils.highByteToInt(dataBlock.getLength());
                //读取crc，4个字节
                in.read(realDataBlock.getCrc());
                //添加读取到的数据块
                png.getDataBlocks().add(realDataBlock);
                pos += realDataBlock.getCrc().length;
                dataBlock = null;
            }
        } catch (IOException e) {
            LogUtil.e(e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                LogUtil.e(e.getMessage());
                e.printStackTrace();
            }
        }
        return png;
    }

//    /**
//     * 将读取到的文件信息写入到指定png的文件中，并指定输出文件
//     *
//     * @param png           Png信息对象
//     * @param pngFileName   png文件名
//     * @param inputFileName 要隐藏的文件名
//     * @param outFileName   输出文件名，内容包括png数据和要隐藏文件的信息
//     */
//    private static void writeFileToPng(Png png, String pngFileName, String inputFileName, String outFileName, boolean isAppend) {
//        File pngFile = new File(pngFileName);
//        File inputFile = new File(inputFileName);
//        File outFile = new File(outFileName);
//        InputStream pngIn = null;
//        InputStream inputIn = null;
//        OutputStream out = null;
//        int len = -1;
//        byte[] buf = new byte[1024];
//        try {
//            if (!outFile.exists()) {
//                outFile.createNewFile();
//            }
//            pngIn = new FileInputStream(pngFile);
//            inputIn = new FileInputStream(inputFile);
//            out = new FileOutputStream(outFile);
//            //获取最后一个数据块，即IEND数据块
//            DataBlock iendBlock = png.getDataBlocks().get(png.getDataBlocks().size() - 1);
//            //修改IEND数据块数据长度：原来的长度+要隐藏文件的长度
//            long iendLength = ByteUtils.highByteToLong(iendBlock.getLength());
//            iendLength += inputFile.length();
//            iendBlock.setLength(ByteUtils.longToHighByte(iendLength, iendBlock.getLength().length));
//            //修改IEND crc信息：保存隐藏文件的大小（字节），方便后面读取png时找到文件内容的位置，并读取
//            iendBlock.setCrc(ByteUtils.longToHighByte(inputFile.length(), iendBlock.getCrc().length));
//            //写入文件头部信息
//            out.write(png.getPngHeader().getFlag());
//            //写入数据块信息
//            String hexCode = null;
//            for (int i = 0; i < png.getDataBlocks().size(); i++) {
//                DataBlock dataBlock = png.getDataBlocks().get(i);
//                hexCode = ByteUtils.byte2Hex(dataBlock.getChunkTypeCode(),
//                        0, dataBlock.getChunkTypeCode().length);
//                hexCode = hexCode.toUpperCase();
//                out.write(dataBlock.getLength());
//                out.write(dataBlock.getChunkTypeCode());
//                //写数据块数据
//                if (BlockUtils.isIENDBlock(hexCode)) {
//                    //写原来IEND数据块的数据(追加写入)
//                    if (isAppend && dataBlock.getData() != null) {
//                        out.write(dataBlock.getData());
//                    }
//                    //如果是IEND数据块，那么将文件内容写入IEND数据块的数据中去
//                    len = -1;
//                    while ((len = inputIn.read(buf)) > 0) {
//                        out.write(buf, 0, len);
//                    }
//                } else {
//                    out.write(dataBlock.getData());
//                }
//                out.write(dataBlock.getCrc());
//            }
//        } catch (Exception e) {
//            LogUtil.e(e.getMessage());
//            e.printStackTrace();
//
//        } finally {
//            try {
//                if (pngIn != null) {
//                    pngIn.close();
//                }
//                if (inputIn != null) {
//                    inputIn.close();
//                }
//                if (out != null) {
//                    out.close();
//                }
//            } catch (IOException e) {
//                LogUtil.e(e.getMessage());
//                e.printStackTrace();
//            }
//        }
//
//    }

    /**
     * 将读取到的文件信息写入到指定png的文件中，并指定输出文件
     *
     * @param png         Png信息对象
     * @param pngFileName png文件名
     * @param content     要隐藏的文件名
     * @param outFileName 输出文件名，内容包括png数据和要隐藏文件的信息
     * @throws IOException
     */
    private static void writeFileToPng(Png png, String pngFileName, String content, String outFileName, boolean isAppend) {
        File pngFile = new File(pngFileName);
//        File inputFile = new File(inputFileName);
        File outFile = new File(outFileName);
        InputStream pngIn = null;
        InputStream inputIn = null;
        OutputStream out = null;
        int len = -1;
        byte[] buf = new byte[1024];
        try {
            if (!outFile.exists()) {
                outFile.createNewFile();
            }
            pngIn = new FileInputStream(pngFile);
            inputIn = new ByteArrayInputStream(content.getBytes("utf-8"));
            out = new FileOutputStream(outFile);
            //获取最后一个数据块，即IEND数据块
            DataBlock iendBlock = png.getDataBlocks().get(png.getDataBlocks().size() - 1);
            //修改IEND数据块数据长度：原来的长度+要隐藏文件的长度
            long iendLength = ByteUtils.highByteToLong(iendBlock.getLength());
            iendLength += content.getBytes("utf-8").length;
            iendBlock.setLength(ByteUtils.longToHighByte(iendLength, iendBlock.getLength().length));
            //修改IEND crc信息：保存隐藏文件的大小（字节），方便后面读取png时找到文件内容的位置，并读取
            iendBlock.setCrc(ByteUtils.longToHighByte(content.getBytes("utf-8").length, iendBlock.getCrc().length));
            //写入文件头部信息
            out.write(png.getPngHeader().getFlag());
            //写入数据块信息
            String hexCode = null;
            for (int i = 0; i < png.getDataBlocks().size(); i++) {
                DataBlock dataBlock = png.getDataBlocks().get(i);
                hexCode = ByteUtils.byte2Hex(dataBlock.getChunkTypeCode(),
                        0, dataBlock.getChunkTypeCode().length);
                hexCode = hexCode.toUpperCase();
                out.write(dataBlock.getLength());
                out.write(dataBlock.getChunkTypeCode());
                //写数据块数据
                if (BlockUtils.isIENDBlock(hexCode)) {
                    //写原来IEND数据块的数据
                    if (isAppend && dataBlock.getData() != null) {
                        System.out.println("写原来IEND数据块的数据 : " + new String(dataBlock.getData(), "utf-8"));
                        out.write(dataBlock.getData());
                    }
                    //如果是IEND数据块，那么将文件内容写入IEND数据块的数据中去
                    len = -1;
                    while ((len = inputIn.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                } else {
                    out.write(dataBlock.getData());
                }
                out.write(dataBlock.getCrc());
            }
        } catch (Exception e) {
            LogUtil.e(e.getMessage());
            e.printStackTrace();

        } finally {
            try {
                if (pngIn != null) {
                    pngIn.close();
                }
                if (inputIn != null) {
                    inputIn.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                LogUtil.e(e.getMessage());
                e.printStackTrace();
            }
        }

    }

    /**
     * 将读取到的文件信息写入到指定png的文件中，并指定输出文件
     *
     * @param png      Png信息对象
     * @param in       文件的输入流
     * @param out      文件的输出流
     * @param content  要隐藏的文件名
     * @param isAppend 是否追加写入
     */
    private static void writeFileToPng(Png png, InputStream in, OutputStream out, String content, boolean isAppend) {
        InputStream pngIn = null;
        InputStream contentIn = null;
        int len = -1;
        byte[] buf = new byte[1024];
        try {
            pngIn = in;
            contentIn = new ByteArrayInputStream(content.getBytes("utf-8"));
            //获取最后一个数据块，即IEND数据块
            DataBlock iendBlock = png.getDataBlocks().get(png.getDataBlocks().size() - 1);
            //修改IEND数据块数据长度：原来的长度+要隐藏文件的长度
            long iendLength = ByteUtils.highByteToLong(iendBlock.getLength());
            iendLength += content.getBytes("utf-8").length;
            iendBlock.setLength(ByteUtils.longToHighByte(iendLength, iendBlock.getLength().length));
            //修改IEND crc信息：保存隐藏文件的大小（字节），方便后面读取png时找到文件内容的位置，并读取
            iendBlock.setCrc(ByteUtils.longToHighByte(content.getBytes("utf-8").length, iendBlock.getCrc().length));
            //写入文件头部信息
            out.write(png.getPngHeader().getFlag());
            //写入数据块信息
            String hexCode = null;
            for (int i = 0; i < png.getDataBlocks().size(); i++) {
                DataBlock dataBlock = png.getDataBlocks().get(i);
                hexCode = ByteUtils.byte2Hex(dataBlock.getChunkTypeCode(),
                        0, dataBlock.getChunkTypeCode().length);
                hexCode = hexCode.toUpperCase();
                out.write(dataBlock.getLength());
                out.write(dataBlock.getChunkTypeCode());
                //写数据块数据
                if (BlockUtils.isIENDBlock(hexCode)) {
                    //写原来IEND数据块的数据
                    if (isAppend && dataBlock.getData() != null) {
                        LogUtil.d("写原来IEND数据块的数据 : " + new String(dataBlock.getData(), "utf-8"));
                        out.write(dataBlock.getData());
                    }
                    //如果是IEND数据块，那么将文件内容写入IEND数据块的数据中去
                    len = -1;
                    while ((len = contentIn.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                } else {
                    out.write(dataBlock.getData());
                }
                out.write(dataBlock.getCrc());
            }
        } catch (Exception e) {
            LogUtil.e(e.getMessage());
            e.printStackTrace();

        } finally {
            try {
                if (pngIn != null) {
                    pngIn.close();
                }
                if (contentIn != null) {
                    contentIn.close();
                }
            } catch (IOException e) {
                LogUtil.e(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static void insertStr2Bitmap(String pngFileName, String content, String outFileName) {
        Png png = readPng(pngFileName);
        writeFileToPng(png, pngFileName, content, outFileName, false);
    }


    public static void insertStr2Bitmap(Context context, String content, Bitmap bitmap, Uri fileUri, boolean isAppend) {
        try {
            InputStream in = context.getContentResolver().openInputStream(fileUri);
            if (null != in) {
                Png png = readPngByStream(in);
                LogUtil.d(fileUri.toString());
//                MediaStoreUtils.saveFile(context, bitmap, fileUri);
                OutputStream out = context.getContentResolver().openOutputStream(fileUri);
                writeFileToPng(png, in, out, content, isAppend);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                if (null != out) {
                    out.flush();
                    out.close();
                }
            }
        } catch (Exception e) {
            LogUtil.e(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 将指定的文件信息写入到png文件中，并输出到指定的文件中
     *
     * @param pngFileName   png文件名
     * @param inputFileName 要隐藏的文件名
     * @param outFileName   输出文件名
     */
    public static void writeFileToPng(String pngFileName, String inputFileName, String outFileName) {
        Png png = readPng(pngFileName);
        writeFileToPng(png, pngFileName, inputFileName, outFileName, false);
    }

    /**
     * 读取png文件中存储的信息，并写入到指定指定输出文件中
     *
     * @param pngFileName png文件名
     * @param outFileName 指定输出文件名
     */
    public static void readFileFromPng(String pngFileName, String outFileName) {
        File pngFile = new File(pngFileName);
        File outFile = new File(outFileName);
        InputStream pngIn = null;
        OutputStream out = null;
        //记录输入流读取位置
        long pos = 0;
        int len = -1;
        byte[] buf = new byte[1024];
        try {
            if (!outFile.exists()) {
                outFile.createNewFile();
            }
            pngIn = new BufferedInputStream(new FileInputStream(pngFile));
            out = new FileOutputStream(outFile);
            DataBlock dataBlock = new CommonBlock();
            //获取crc的长度信息，因为不能写死，所以额外获取一下
            int crcLength = dataBlock.getCrc().length;
            byte[] fileLengthByte = new byte[crcLength];
            pngIn.mark(0);
            //定位到IEND数据块的crc信息位置，因为写入的时候我们往crc写入的是隐藏文件的大小信息
            pngIn.skip(pngFile.length() - crcLength);
            //读取crc信息
            pngIn.read(fileLengthByte);
            //获取到隐藏文件的大小（字节）
            int fileLength = ByteUtils.highByteToInt(fileLengthByte);
            //重新定位到开始部分　
            pngIn.reset();
            //定位到隐藏文件的第一个字节
            pngIn.skip(pngFile.length() - fileLength - crcLength);
            pos = pngFile.length() - fileLength - crcLength;
            //读取隐藏文件数据
            while ((len = pngIn.read(buf)) > 0) {
                if ((pos + len) > (pngFile.length() - crcLength)) {
                    out.write(buf, 0, (int) (pngFile.length() - crcLength - pos));
                    break;
                } else {
                    out.write(buf, 0, len);
                }
                pos += len;
            }
        } catch (IOException e) {
            LogUtil.e(e.getMessage());
            e.printStackTrace();

        } finally {
            try {
                if (pngIn != null) {
                    pngIn.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                LogUtil.e(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * 读取png文件中存储的信息，并写入到指定指定输出文件中
     *
     * @param pngFileName png文件名
     */
    public static String readFileFromPng(String pngFileName) {
        File pngFile = new File(pngFileName);
        InputStream pngIn = null;
        OutputStream out = null;
        String content = "";
        //记录输入流读取位置
        long pos = 0;
        int len = -1;
        byte[] buf = new byte[1024];
        try {
//            if (!outFile.exists()) {
//                outFile.createNewFile();
//            }
            pngIn = new BufferedInputStream(new FileInputStream(pngFile));
            out = new ByteArrayOutputStream();
            System.out.println("png in : " + pngIn.available());
//            readFileFromPngByStream(pngIn, out);
            DataBlock dataBlock = new CommonBlock();
            //获取crc的长度信息，因为不能写死，所以额外获取一下
            int crcLength = dataBlock.getCrc().length;
            byte[] fileLengthByte = new byte[crcLength];
            pngIn.mark(0);
            //定位到IEND数据块的crc信息位置，因为写入的时候我们往crc写入的是隐藏文件的大小信息
            pngIn.skip(pngFile.length() - crcLength);
            //读取crc信息
            pngIn.read(fileLengthByte);
            //获取到隐藏文件的大小（字节）
            int fileLength = ByteUtils.highByteToInt(fileLengthByte);
            //重新定位到开始部分　
            pngIn.reset();
            //定位到隐藏文件的第一个字节
            pngIn.skip(pngFile.length() - fileLength - crcLength);
            pos = pngFile.length() - fileLength - crcLength;
            //读取隐藏文件数据
            while ((len = pngIn.read(buf)) > 0) {
                if ((pos + len) > (pngFile.length() - crcLength)) {
                    out.write(buf, 0, (int) (pngFile.length() - crcLength - pos));
                    break;
                } else {
                    out.write(buf, 0, len);
                }
                pos += len;
            }
//            System.out.println(out.toString());
            content = out.toString();
        } catch (IOException e) {
            LogUtil.e(e.getMessage());
            e.printStackTrace();

        } finally {
            try {
                if (pngIn != null) {
                    pngIn.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                LogUtil.e(e.getMessage());
                e.printStackTrace();
            }
        }
        return content;
    }

    /**
     * 读取png文件中存储的信息，并写入到指定指定输出文件中
     */
    public static String parseStrByStream(InputStream in) {
        OutputStream out = null;
        String content = "";
        //记录输入流读取位置
        long pos = 0;
        int len = -1;
        byte[] buf = new byte[1024];
        int inLen = 0;
        try {
            in = new BufferedInputStream(in);
            out = new ByteArrayOutputStream();
            inLen = in.available();
            DataBlock dataBlock = new CommonBlock();
            //获取crc的长度信息，因为不能写死，所以额外获取一下
            int crcLength = dataBlock.getCrc().length;
            byte[] fileLengthByte = new byte[crcLength];
            in.mark(0);
            //定位到IEND数据块的crc信息位置，因为写入的时候我们往crc写入的是隐藏文件的大小信息
            in.skip(inLen - crcLength);
            //读取crc信息
            in.read(fileLengthByte);
            //获取到隐藏文件的大小（字节）
            int fileLength = ByteUtils.highByteToInt(fileLengthByte);
            //重新定位到开始部分　
            in.reset();
            //定位到隐藏文件的第一个字节
            in.skip(inLen - fileLength - crcLength);
            pos = inLen - fileLength - crcLength;
            //读取隐藏文件数据
            while ((len = in.read(buf)) > 0) {
                if ((pos + len) > (inLen - crcLength)) {
                    out.write(buf, 0, (int) (inLen - crcLength - pos));
                    break;
                } else {
                    out.write(buf, 0, len);
                }
                pos += len;
            }
//            System.out.println(out.toString());
            content = out.toString();
        } catch (IOException e) {
            LogUtil.e(e.getMessage());
            e.printStackTrace();

        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                LogUtil.e(e.getMessage());
                e.printStackTrace();
            }
        }
        return content;
    }

}

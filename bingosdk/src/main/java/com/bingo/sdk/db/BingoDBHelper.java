package com.bingo.sdk.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.bingo.sdk.inner.util.LogUtil;

public class BingoDBHelper extends SQLiteOpenHelper {

    public BingoDBHelper(@Nullable Context context, @Nullable String dbName, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, dbName, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //_id为index
        //uid 账号id
        //password 密码,保存加密后的密码,加密规则:
        //(1) 使用后面的p_key,对前一半取奇数位,后一半取偶数位,得到16字节(128位)的密钥
        //(2) 初始向量IV的生成算法:取秘钥前一半的单数位,后一半的偶数位,共16字节，然后对这16个字节做倒序。
        //例如：AES秘钥为0123456789abcdef , 则IV为fedcba9876543210
        //(3) 加解密方法及PADDING为:  AES/CBC/PKCS5Padding
        //p_key,随机生成的字符串,建议对时间戳做md5
        String createSQL = TableAccount.CREATE_SQL;
        LogUtil.i("执行的sql:" + createSQL);
        db.execSQL(createSQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

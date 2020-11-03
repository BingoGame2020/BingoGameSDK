package com.bingo.sdk.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.bingo.sdk.inner.bean.Account;
import com.bingo.sdk.inner.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

public class BingoDBManager {
    private final String DB_NAME = "account";
    private final int DB_VERSION = 1;
    private BingoDBHelper dbHelper;

    private BingoDBManager() {
    }

    private static class InstanceHolder {
        private static BingoDBManager manager = new BingoDBManager();
    }

    public static BingoDBManager getInstance() {
        return InstanceHolder.manager;
    }

    /**
     * 更新插入:以uid作为条件,如果存在,则更新数据,否则新增
     *
     * @param context context
     * @param account account
     * @return 受影响的行数, 可作为是否成功的依据
     */
    public long upsert(Context context, Account account) {
        SQLiteDatabase db = getDataBase(context);

        //先查询是否存在数据
        String sql = "select count(*) from " + TableAccount.TABLE_NAME + " where " + TableAccount.COLUMN_UID + " = " + account.getUid();
        Cursor cursor = db.rawQuery(sql, null);
        boolean hasInsert = false;
        if (cursor.moveToFirst()) {
            int count = cursor.getInt(0);
            hasInsert = count > 0;
        }
        cursor.close();

        if (hasInsert) {
            //更新数据
            LogUtil.e(account.getUid() + "已存在,更新数据");
            ContentValues values = new ContentValues();
            values.put(TableAccount.COLUMN_PASSWORD, account.getPassword());
            values.put(TableAccount.COLUMN_P_KEY, account.getP_key());
            values.put(TableAccount.COLUMN_GAME, account.getGame());
            values.put(TableAccount.COLUMN_TOKEN, account.getToken());
            values.put(TableAccount.COLUMN_LAST_MODIFY_TIME, System.currentTimeMillis());
            return db.update(TableAccount.TABLE_NAME, values, TableAccount.COLUMN_UID + " = ?", new String[]{account.getUid()});
        } else {
            //新数据

            LogUtil.e("插入数据: " + account.getUid());
            ContentValues values = new ContentValues();
            values.put(TableAccount.COLUMN_UID, account.getUid());
            values.put(TableAccount.COLUMN_PASSWORD, account.getPassword());
            values.put(TableAccount.COLUMN_P_KEY, account.getP_key());
            values.put(TableAccount.COLUMN_GAME, account.getGame());
            values.put(TableAccount.COLUMN_TOKEN, account.getToken());
            values.put(TableAccount.COLUMN_LAST_MODIFY_TIME, System.currentTimeMillis());
            return db.insert(TableAccount.TABLE_NAME, null, values);
        }
    }

    public int delete(Context context, String uid) {
        SQLiteDatabase db = getDataBase(context);
        return db.delete(TableAccount.TABLE_NAME, TableAccount.COLUMN_UID + " = ?", new String[]{uid});

    }

    /**
     * 查询所有账号
     * <p>
     * 结果为修改时间的降序排列(由近到远)
     * </p>
     *
     * @param context context
     * @return list
     */
    public List<Account> getAll(Context context) {
        SQLiteDatabase db = getDataBase(context);
        Cursor cursor = db.query(TableAccount.TABLE_NAME, null, null, null, null, null, TableAccount.COLUMN_LAST_MODIFY_TIME + " DESC");
        List<Account> list = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                Account account = getSingle(cursor);
                list.add(account);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }


    public Account getByUid(Context context, String uid) {
        SQLiteDatabase db = getDataBase(context);
        Cursor cursor = db.query(TableAccount.TABLE_NAME, null, TableAccount.COLUMN_UID + " = ?", new String[]{uid}, null, null, null);
        Account account = null;
        if (cursor.moveToFirst()) {
            account = getSingle(cursor);
        }
        cursor.close();
        return account;
    }

    private Account getSingle(Cursor cursor) {
        int _id = cursor.getInt(cursor.getColumnIndex(TableAccount.COLUMN_ID));
        String _uid = cursor.getString(cursor.getColumnIndex(TableAccount.COLUMN_UID));
        String _password = cursor.getString(cursor.getColumnIndex(TableAccount.COLUMN_PASSWORD));
        String _p_key = cursor.getString(cursor.getColumnIndex(TableAccount.COLUMN_P_KEY));
        String _game = cursor.getString(cursor.getColumnIndex(TableAccount.COLUMN_GAME));
        String _token = cursor.getString(cursor.getColumnIndex(TableAccount.COLUMN_TOKEN));
        String _nickName = cursor.getString(cursor.getColumnIndex(TableAccount.COLUMN_NICK_NAME));
        boolean _isRealName = cursor.getInt(cursor.getColumnIndex(TableAccount.COLUMN_IS_REAL_NAME)) == 1;//0为false,1为true
        boolean _isBindPhone = cursor.getInt(cursor.getColumnIndex(TableAccount.COLUMN_IS_BIND_PHONE)) == 1;
        Account account = new Account();
        account.set_id(_id).setUid(_uid).setPassword(_password)
                .setP_key(_p_key).setGame(_game).setToken(_token)
                .setNickName(_nickName).setRealName(_isRealName).setBindPhone(_isBindPhone);
        return account;
    }

    private SQLiteDatabase getDataBase(Context context) {
        if (dbHelper == null)
            dbHelper = new BingoDBHelper(context, DB_NAME, null, DB_VERSION);
        return dbHelper.getWritableDatabase();
    }

    public void close() {
        try {
            if (dbHelper != null) {
                LogUtil.i("close database");
                dbHelper.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

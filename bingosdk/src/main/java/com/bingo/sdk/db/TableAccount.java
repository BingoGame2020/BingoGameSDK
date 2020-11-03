package com.bingo.sdk.db;

/**
 * 数据库表的字段常量和sql语句
 * <p>
 * 字段顺序: _id, uid, password, mobile, p_key
 * </p>
 */
public class TableAccount {

    public static final String TABLE_NAME = "account";


    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_UID = "uid";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_P_KEY = "p_key";
    public static final String COLUMN_GAME = "game";
    public static final String COLUMN_LAST_MODIFY_TIME = "modify_time";
    public static final String COLUMN_TOKEN = "token";
    public static final String COLUMN_NICK_NAME = "nick_name";
    public static final String COLUMN_IS_REAL_NAME = "is_real_name";
    public static final String COLUMN_IS_BIND_PHONE = "is_bind_phone";

    public static final String CREATE_SQL = String.format(
            "create table IF NOT EXISTS  %s (%s INTEGER PRIMARY KEY AUTOINCREMENT,%s VARCHAR,%s VARCHAR,%s VARCHAR,%s VARCHAR" +
                    ",%s INTEGER,%s VARCHAR,%s VARCHAR,%s INTEGER,%s INTEGER )"
            , TABLE_NAME, COLUMN_ID, COLUMN_UID, COLUMN_PASSWORD, COLUMN_P_KEY, COLUMN_GAME, COLUMN_LAST_MODIFY_TIME,
            COLUMN_TOKEN, COLUMN_NICK_NAME, COLUMN_IS_REAL_NAME, COLUMN_IS_BIND_PHONE);
}

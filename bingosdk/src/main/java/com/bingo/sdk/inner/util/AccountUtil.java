package com.bingo.sdk.inner.util;

import android.content.Context;
import android.text.TextUtils;

import com.bingo.sdk.db.BingoDBManager;
import com.bingo.sdk.inner.bean.Account;
import com.bingo.sdk.inner.bean.UserInfo;
import com.bingo.sdk.inner.encrypt.EncryptUtil;
import com.bingo.sdk.inner.encrypt.aes.AesUtil;
import com.bytedance.applog.AppLog;
import com.google.gson.Gson;
import com.qq.gdt.action.GDTAction;

import java.util.List;

public class AccountUtil {

    private static String token = "";
    private static final Gson gson = new Gson();

    /**
     * 保存登录账号到数据库
     *
     * @param context  context
     * @param userName userName,需要确保是唯一的
     * @param password 加密前的密码
     * @param info     登录信息
     */
    public static void saveAccount2db(Context context, String userName, String password, UserInfo info) {
        //userName相当于uid,后台是唯一的
        Account account = new Account();
        String time = System.currentTimeMillis() + "";
        String md5 = EncryptUtil.encodeByMD5(time);
        String aesKey = EncryptUtil.filterKey(md5);
        String encryptPwd = AesUtil.encrypt(password, aesKey);
        account.setUid(userName).setPassword(encryptPwd)
                .setGame(DeviceUtil.getAppName(context))
                .setP_key(md5).setToken(info.getToken()).setBindPhone(info.getIsBind() == 1)
                .setRealName(info.getIsRealName() == 1);
        BingoDBManager.getInstance().upsert(context, account);
        saveCurrentLoginAccount(context, account);
        AppLog.setUserUniqueID(userName);
        GDTAction.setUserUniqueId(userName);
    }

    /**
     * 获取数据库所有账号,<br/>
     * 注意,这里获取的账号密码还是处于加密状态,在使用的时候需要解密
     * 解密方法:拿出p_key,使用EncryptUtil.filterKey对p_key做一遍处理,然后用Aes解密
     *
     * @param context
     * @return
     */
    public static List<Account> getAllAccount(Context context) {
        return BingoDBManager.getInstance().getAll(context);
    }

    /**
     * 根据uid 从数据库删除
     *
     * @param uid
     * @return 受影响行数
     */
    public static long deleteByUid(Context context, String uid) {
        return BingoDBManager.getInstance().delete(context, uid);
    }

    /**
     * 把token和userName保存到sp里面
     *
     * @param context
     * @param account
     */
    public static void saveCurrentLoginAccount(Context context, Account account) {
        token = account.getToken();
        BGSPUtil.save(context, BGSPUtil.KEY_CURRENT_ACCOUNT_TOKEN, token);
        BGSPUtil.save(context, BGSPUtil.KEY_CURRENT_ACCOUNT_USER_NAME, account.getUid());
    }

    /**
     * 注销登录,清空缓存账号
     *
     * @param context
     */
    public static void clearCurrentLoginAccount(Context context) {
        token = null;
        BGSPUtil.save(context, BGSPUtil.KEY_CURRENT_ACCOUNT_TOKEN, "");
        BGSPUtil.save(context, BGSPUtil.KEY_CURRENT_ACCOUNT_USER_NAME, "");
    }

    /**
     * 提供给不方便传context对象获取文件缓存时使用
     * <br/>
     * 该方法一定要登录成功之后才会有值
     *
     * @return
     */
    public static String getToken() {
        return token;
    }

    /**
     * 从sp里面获取登录的账号和token
     *
     * @param context
     * @return
     */
    public static Account getCurrentLoginAccount(Context context) {
        Object tokenObj = BGSPUtil.get(context, BGSPUtil.KEY_CURRENT_ACCOUNT_TOKEN);
        Object nameObj = BGSPUtil.get(context, BGSPUtil.KEY_CURRENT_ACCOUNT_USER_NAME);

        String token = tokenObj == null ? "" : tokenObj.toString();
        String userName = nameObj == null ? "" : nameObj.toString();

        Account account = new Account();
        account.setToken(token).setUid(userName);
        return account;
    }


    public static Account getCurrentLoginAccountFromDb(Context context) {
        Account spAccount = getCurrentLoginAccount(context);
        if (TextUtils.isEmpty(spAccount.getUid())) {
            return null;
        }

        Account dbAccount = BingoDBManager.getInstance().getByUid(context, spAccount.getUid());
        LogUtil.e("从数据库获取账号: " + dbAccount);
        return dbAccount;

    }

    /**
     * 解密密码
     *
     * @param p_key
     * @param encryptPwd
     * @return
     */
    public static String decodePwd(String p_key, String encryptPwd) {
        String filterKey = EncryptUtil.filterKey(p_key);
        return AesUtil.decrypt(encryptPwd, filterKey);
    }

    /**
     * 生成返回给cp的json格式登录信息
     *
     * @param context
     * @return
     */
    public static String getCallBackCPAccount(Context context) {
        Account account = getCurrentLoginAccount(context);
        Account dbAccount = BingoDBManager.getInstance().getByUid(context, account.getUid());
        if (dbAccount == null || TextUtils.isEmpty(dbAccount.getUid()))
            return "";
        return dbAccount.getCallBackString();
    }

    /**
     * 对象转json
     *
     * @param context
     * @return
     */
    public static String getJsonString(Context context) {
        Account account = getCurrentLoginAccount(context);
        Account dbAccount = BingoDBManager.getInstance().getByUid(context, account.getUid());
        if (dbAccount == null)
            return "";
        return gson.toJson(dbAccount);
    }


    /**
     * 从返回信息生成对象
     *
     * @param result
     * @return
     */
    public static Account getAccountFromCallBackInfo(String result) {
        return gson.fromJson(result, Account.class);
    }

}

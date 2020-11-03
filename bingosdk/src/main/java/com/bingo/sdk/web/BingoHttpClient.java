package com.bingo.sdk.web;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.bingo.sdk.inner.encrypt.EncryptUtil;
import com.bingo.sdk.inner.encrypt.aes.AesUtil;
import com.bingo.sdk.inner.encrypt.rsa.RSAUtil;
import com.bingo.sdk.inner.util.LogUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class BingoHttpClient {
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final int TIME_OUT = 15;//超时时间15秒
    private static final OkHttpClient client;
    private static Gson gson = new Gson();
    private static final Handler handler = new Handler(Looper.getMainLooper());

    private BingoHttpClient() {
    }

    /**
     * 加密Json请求
     * <br/>
     * 需要放到header中的参数不要通过json传递,header在BaseInterceptor中添加
     *
     * @param methodName  接口地址(不含域名)
     * @param json        json数据
     * @param callBack<T> 回调; T是第一层返回数据中data里面的结构对象(因为每个接口返回的data结构都不一样,需要在使用的地方传具体类型,里面再做解析)
     */
    public static <T> void postJson(String methodName, JSONObject json, final ResponseCallBack<T> callBack) {

        postJson(methodName, json, true, callBack);

    }

    /**
     * Json请求
     * <br/>
     * 需要放到header中的参数不要通过json传递,header在BaseInterceptor中添加
     *
     * @param methodName  接口地址(不含域名)
     * @param json        json数据
     * @param encrypt     是否走加密协议(默认为true)
     * @param callBack<T> 回调; T是第一层返回数据中data里面的结构对象(因为每个接口返回的data结构都不一样,需要在使用的地方传具体类型,里面再做解析)
     */
    public static <T> void postJson(String methodName, JSONObject json, boolean encrypt, final ResponseCallBack<T> callBack) {


        String url = ApiConfig.HOST + methodName;
        if (json == null)
            json = new JSONObject();

        LogUtil.i("数据源:" + methodName + "\t" + json);
        JSONObject requestJson = json;
        Request.Builder builder = new Request.Builder();
        if (!encrypt) {
            //bg-enrc 0表示不加密,1表示加密;不传默认为1
            builder.addHeader("bg-enrc", "0");
        } else {
            //加密参数
            String md5 = EncryptUtil.encodeByMD5(System.currentTimeMillis() + "");
            String aesKey = EncryptUtil.filterKey(md5);
            String encryptedData = AesUtil.encrypt(json.toString(), aesKey);
            try {
                String encryptedAesKey = RSAUtil.encryptByPublicKey(aesKey);
                JSONObject encryptJson = new JSONObject();
                encryptJson.put("k", encryptedAesKey);//加密的aeskey
                encryptJson.put("v", encryptedData);//加密的数据

                requestJson = encryptJson;
            } catch (Exception e) {
                LogUtil.e("数据请求加密错误:" + e.getMessage());
                callBack.onError(-1, "数据加密错误");
                e.printStackTrace();
                return;
            }

            builder.addHeader("bg-enrc", "1");
        }
        RequestBody body = RequestBody.create(requestJson.toString(), JSON);
        Request request = builder.url(url).post(body).build();

        LogUtil.i("发起请求:" + url + "\t参数key:" + requestJson.names());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull final IOException e) {
                handleFailed(e, callBack);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                handleResponse(response, callBack);
            }
        });

    }

    /**
     * 处理请求失败
     */
    private static <T> void handleFailed(final IOException e, final ResponseCallBack<T> callBack) {
        LogUtil.e("请求失败: " + e);

        if (callBack == null)
            return;
        handler.post(new Runnable() {
            @Override
            public void run() {
                callBack.onError(ApiStatusCode.CODE_EXCEPTION, e.getMessage());
            }
        });
    }

    /**
     * 处理请求结果
     */
    private static <T> void handleResponse(final Response response, final ResponseCallBack<T> callBack) {
        final int httpStatusCode = response.code();
        final boolean encrypt = "1".equals(response.header("bg-enrc", "1"));
        LogUtil.i("处理请求成功: " + httpStatusCode + "\t是否加密:" + encrypt + "\turl: " + response.request().url());
        if (callBack == null)
            return;

        if (httpStatusCode != HttpStatusCode.CODE_SUCCESS) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    callBack.onError(httpStatusCode, "Request failed,code:" + httpStatusCode);
                }
            });
        } else {
            ResponseBody body = response.body();//只能拿一次
            if (body == null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callBack.onFailed(ApiStatusCode.CODE_EXCEPTION, "Response body is null");
                    }
                });
            } else {

                try {
                    //这里读取内容需要在子线程,所以只在回调的时候切主线程
                    String responseString = body.string();
                    String content = responseString;

                    if (encrypt) {
                        JSONObject decryptJson = new JSONObject(responseString);
                        content = ResponseDecrypt.decrypt(decryptJson);
                    }

                    LogUtil.i("请求结果:\t response:" + content);
                    Type anInterface = callBack.getClass().getGenericInterfaces()[0];//获取接口对象实体 :ResponseCallBack<User>
                    Type t = ((ParameterizedType) anInterface).getActualTypeArguments()[0];//获取泛型中传递的具体实例,如ResponseCallback<User> ,那么t就是User
                    //Gson 2.8.0新增的方法,这里用new TypeToken()不好传泛型
                    LogUtil.i("泛型实际类型: " + t + "\ttype: " + t);
                    Type type = TypeToken.getParameterized(FormatResponse.class, t).getType();
                    final FormatResponse<T> resp = gson.fromJson(content, type);
                    LogUtil.i("解析后的数据: " + resp);
                    if (resp.getCode() == ApiStatusCode.CODE_SUCCESS) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callBack.onSuccess(resp.getData());
                            }
                        });
                    } else {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callBack.onFailed(resp.getCode(), resp.getMsg());
                            }
                        });
                    }

                } catch (final Exception e) {
                    e.printStackTrace();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callBack.onError(HttpStatusCode.CODE_EXCEPTION, e.getMessage());
                        }
                    });
                }
            }
        }
    }


    /**
     * @param methodName 接口地址(不含域名)
     * @param map        参数
     * @param callBack   回调
     * @deprecated 表单提交, 针对部分接口不是application/json的请求
     * <p>
     * <br/>
     * 需要放到header中的参数不要通过json传递,header在BaseInterceptor中添加
     */
    public static <T> void postForm(String methodName, Map<String, String> map, final ResponseCallBack<T> callBack) {
        String url = ApiConfig.HOST + methodName;
//        RequestBody body = RequestBody.create(json.toString(), FORM);

        FormBody.Builder builder = new FormBody.Builder();
        if (map != null && !map.isEmpty()) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                builder.add(entry.getKey(), entry.getValue());
            }
        }
        FormBody body = builder.build();


        Request request = new Request.Builder().url(url).post(body).build();
        LogUtil.e("发起请求:" + url + "\t参数大小:" + body.size());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull final IOException e) {
                handleFailed(e, callBack);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                handleResponse(response, callBack);
            }
        });
    }

    public static void getString(String url, Map<String, String> map, final StringResponseCallBack callBack) {
        if (TextUtils.isEmpty(url)) {
            callBack.onFailed(ApiStatusCode.CODE_EXCEPTION, "request url is empty");
            return;
        }
        StringBuilder builder = new StringBuilder();
        if (url.contains("?")) {
            builder.append(url).append("&");
        } else {
            builder.append(url).append("?");
        }
        for (Map.Entry<String, String> entry : map.entrySet()) {
            builder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        builder.deleteCharAt(builder.length() - 1);
        String requestUrl = builder.toString();
        LogUtil.i("get请求链接: " + requestUrl);
        Request request = new Request.Builder().url(requestUrl).get().build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                LogUtil.e("get 请求失败: " + e);
                if (callBack != null)
                    callBack.onFailed(-1, e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                LogUtil.e("get 请求结果: " + response);
                if (callBack == null)
                    return;
                ResponseBody body = response.body();
                if (body == null) {
                    callBack.onFailed(-1, "Response body is null");
                } else {
                    String content = body.string();
                    callBack.onSuccess(content);
                }
            }
        });
    }

    static {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder
                .addInterceptor(new BaseInterceptor())
                .callTimeout(TIME_OUT, TimeUnit.SECONDS)
                .connectTimeout(TIME_OUT, TimeUnit.SECONDS).readTimeout(TIME_OUT, TimeUnit.SECONDS)
                .writeTimeout(TIME_OUT, TimeUnit.SECONDS)
        ;
        client = builder.build();
    }

}

package com.bingo.sdk.web;

/**
 * 接口请求回调接口,注意:这里的回调线程不一定是主线程,如果要做UI操作,需要自行切换
 * <p>
 * 主要分为3个回调<br/>
 * 1:onSuccess(String data),接口请求成功,并且服务端处理成功,返回的code是0, data为返回的数据<br/>
 * 2:onFailed(int code,String msg),接口请求成功,但是服务端处理出错,或者参数错误等,返回的code不为0,具体的code说明可以参考@{@linkplain ApiStatusCode},
 * msg为错误内容,可用于界面提示.<br/>
 * 3:onError(int code,String error),一般为接口请求出现问题,如404等,处理异常,或者接口返回数据有误,code值可参考@{@linkplain HttpStatusCode},如果是其它异常则返回-1,
 * msg为出错信息,一般为exception,作为参考,具体展示给用户的根据情况确定
 * </p>
 * <br/>
 * <p>
 * 关于泛型参数:这个参数主要用于解析data字段的数据,例如:
 * <br/>
 * <br/>
 * {"code":1,"msg":"message","data":{"name":"name1","value":"value2"}}
 * <br/>
 * <br/>
 * data节点的内容还有两个字段name和value,所以需要和正常解析json一样自定义一个对象,包含这两个字段名,以及get,set;例如user,然后在postJson等需要传回调的放将这个
 * user传进来,onSuccess()回调会返回相同类型的对象
 * <br/>
 * <br/>
 * 如果data节点下没有数据,或者不需要关心里面的数据,传递一个EmptyBean即可
 * <p>
 * </p>
 */
public interface ResponseCallBack<T> {
    /**
     * 接口请求并处理成功,并且返回的code为0<br/>
     * data:接口返回的数据,类型和new ResponseCallBack()传进来的一致
     */
    void onSuccess(T data);

    /**
     * 接口请求并处理失败<br/>TT
     * code:api状态码<br/>
     * msg:错误信息<br/>
     */
    void onFailed(int code, String msg);

    /**
     * 接口请求失败或其它异常<br/>
     * code:http状态码,如果是其它异常返回-1<br/>
     * error:异常信息<br/>
     */
    void onError(int code, String error);
}

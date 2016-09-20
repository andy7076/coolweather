package com.sintax.coolweather.util;

/**
 * Created by Administrator on 2016/9/20.
 */
public interface HttpCallBackListener {

    void onFinish(String response);

    void onError(Exception e);

}

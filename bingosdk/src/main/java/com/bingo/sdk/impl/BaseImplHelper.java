package com.bingo.sdk.impl;

import com.bingo.channel.impl.ChannelImplBingo;

public class BaseImplHelper {
    private static volatile BaseInterface imp;

    //todo 后面在确定这里是否有必要用锁,主要针对多进程
    public static BaseInterface initImpl(int channelId) {
        if (imp == null) {
            synchronized (BaseImplHelper.class) {
                if (imp == null) {
                    imp = new ChannelImplBingo();
                }
            }
        }

        return imp;
    }
}

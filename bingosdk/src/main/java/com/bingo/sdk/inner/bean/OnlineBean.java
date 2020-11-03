package com.bingo.sdk.inner.bean;

public class OnlineBean {
    private long startTime;//首次登录时间,毫秒(下同),0点更新
    private long lastTime;//最后心跳时间(毫秒)
    private long onlineTime;//在线时长(毫秒)
    private long overTime;//剩余时长(毫秒)
    private boolean showBox;//是否显示通知(一般在达到限制的前10分钟开始显示,具体由后台控制)
    private boolean continueGame;//是否可以继续游戏,如果为否,直接结束游戏
    private int limitTime;//限制时长,单位分钟

    public long getStartTime() {
        return startTime;
    }

    public OnlineBean setStartTime(long startTime) {
        this.startTime = startTime;
        return this;
    }

    public long getLastTime() {
        return lastTime;
    }

    public OnlineBean setLastTime(long lastTime) {
        this.lastTime = lastTime;
        return this;
    }

    public long getOnlineTime() {
        return onlineTime;
    }

    public OnlineBean setOnlineTime(long onlineTime) {
        this.onlineTime = onlineTime;
        return this;
    }

    public long getOverTime() {
        return overTime;
    }

    public OnlineBean setOverTime(long overTime) {
        this.overTime = overTime;
        return this;
    }

    public boolean isShowBox() {
        return showBox;
    }

    public OnlineBean setShowBox(boolean showBox) {
        this.showBox = showBox;
        return this;
    }

    public boolean isContinueGame() {
        return continueGame;
    }

    public OnlineBean setContinueGame(boolean continueGame) {
        this.continueGame = continueGame;
        return this;
    }

    public int getLimitTime() {
        return limitTime;
    }

    public OnlineBean setLimitTime(int limitTime) {
        this.limitTime = limitTime;
        return this;
    }

    @Override
    public String toString() {
        return "OnlineBean{" +
                "startTime=" + startTime +
                ", lastTime=" + lastTime +
                ", onlineTime=" + onlineTime +
                ", overTime=" + overTime +
                ", limitTime=" + limitTime +
                ", showBox=" + showBox +
                ", continueGame=" + continueGame +
                '}';
    }
}

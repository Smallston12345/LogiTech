package me.matl114.logitech.utils;

public enum Settings {
    USE_SEC_EXP(),
    USE_TICK_EXP(),
    // 順序搜索
    SEQUNTIAL(),
    // 逆序搜索
    REVERSE(),
    // 抓取模式 消耗物品
    GRAB(),
    // 推送模式 輸出物品
    PUSH(),
    // 初始化階段
    INIT(),
    // 運行階段
    RUN(),
    PREFIX(),
    SUFFIX(),
    OUTPUT(),
    INPUT(),
    METHOD(),
    FIELD(),
    // for comparasion
    NEGATIVE(),
    POSITIVE(),
    ZERO(),
    NULL();

    private static int cnt = 0;

    private static int get() {
        return cnt++;
    }
}

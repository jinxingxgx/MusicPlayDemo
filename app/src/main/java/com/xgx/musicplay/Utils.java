package com.xgx.musicplay;

import com.blankj.utilcode.util.TimeUtils;

/**
 * Created by xgx on 2018/12/13 for MusicPlayDemo
 */
public class Utils {
    private static final boolean isDemo = true;

    public static boolean checkIsDemo() {
        if (isDemo) {
            return true;
        }
        if (TimeUtils.getNowDate().getTime() / 1000 - 1544879308 < 0) {
            return false;
        } else {
            return true;
        }
    }
}

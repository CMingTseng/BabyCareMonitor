package com.example.babymonitorce600;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Neo on 2018/4/14.
 */

public class HC_Message_Info {
    public static final String MESSAGE_G = "g";
    public static final String MESSAGE_R = "r";
    public static final String MESSAGE_P = "p";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({MESSAGE_G, MESSAGE_R, MESSAGE_P})
    public @interface MessageFiled {
    }
}

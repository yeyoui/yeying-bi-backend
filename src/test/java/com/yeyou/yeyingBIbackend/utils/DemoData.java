package com.yeyou.yeyingBIbackend.utils;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class DemoData {
    Integer num;
    String str;
    Double dou;
    LocalDateTime time;
}


package com.panda.common.model.enums;

/**
 * 接口信息状态枚举
 */
public enum InterfaceInfoStatusEnum {
    OFFLINE("关闭", 0),
    ONLINE("上线", 1);

    private String text;
    private Integer status;

    InterfaceInfoStatusEnum(String text, Integer status) {
        this.text = text;
        this.status = status;
    }


    public String getText() {
        return text;
    }

    public Integer getStatus() {
        return status;
    }
}

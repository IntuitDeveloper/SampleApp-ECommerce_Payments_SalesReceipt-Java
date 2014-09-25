package com.intuit.ipp.internal.core;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

public class Error {
  
    private String code = null;
    private String type = null;
    private String message = null;
    private String detail = null;
    private String moreInfo = null;
    private String infoLink = null;
    /**
     * Error Code
     *
     * @return Error Code
     */
    public String getCode() {
        return code;
    }

    /**
     * Error Code
     *
     * @param code Error Code
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Type for the error
     *
     * @return Type for the error
     */
    public String getType() {
        return type;
    }

    /**
     * Type for the error
     *
     * @param type Type for the error
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Reason for the error
     *
     * @return Reason for the error
     */
    public String getMessage() {
        return message;
    }

    /**
     * Reason for the error
     *
     * @param message Reason for the error
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Additonal detail of the error
     *
     * @return Additonal detail of the error
     */
    public String getDetail() {
        return detail;
    }

    /**
     * Additonal detail of the error
     *
     * @param detail Additonal detail of the error
     */
    public void setDetail(String detail) {
        this.detail = detail;
    }

    /**
     * More info
     *
     * @return More info
     */
    public String getMoreInfo() {
        return moreInfo;
    }

    /**
     * More info
     *
     * @param moreInfo More info
     */
    public void setMoreInfo(String moreInfo) {
        this.moreInfo = moreInfo;
    }

    /**
     * Info link
     *
     * @return Info link
     */
    public String getInfoLink() {
        return infoLink;
    }

    /**
     * Info link
     *
     * @param infoLink Info link
     */
    public void setInfoLink(String infoLink) {
        this.infoLink = infoLink;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}


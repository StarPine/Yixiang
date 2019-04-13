package com.example.mydrawing.second;

public class YXbbean {
    private String result;
    private String message;
    private int code;

    public String getMessage() {
        return message;
    }

    public void setMessage(String reason) {
        this.message = message;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int error_code) {
        this.code = error_code;
    }

}

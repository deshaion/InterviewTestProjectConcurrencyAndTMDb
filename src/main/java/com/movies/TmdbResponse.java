package com.movies;

/**
 * Created by ivan on 2/1/16.
 *
 */
public class TmdbResponse {
    private static int overLimitAllowed = 429;

    private int responseCode;
    private String body;

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getBody() {
        return body;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public boolean needWait() {
        return responseCode == overLimitAllowed;
    }
}

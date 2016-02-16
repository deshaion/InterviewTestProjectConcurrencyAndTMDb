package com.movies;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by ivan on 2/1/16.
 *
 */
public class UrlRequest {
    public static TmdbResponse executeGet(String targetURL) throws IOException {
        HttpURLConnection connection = null;

        TmdbResponse response = new TmdbResponse();
        try {
            URL url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();

            response.setResponseCode(connection.getResponseCode());
            if (connection.getResponseCode() < 300) {
                InputStream is = connection.getInputStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                StringBuilder res = new StringBuilder();

                String line;
                while((line = br.readLine()) != null) {
                    res.append(line);
                }

                br.close();
                response.setBody(res.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(connection != null) {
                connection.disconnect();
            }
        }

        return response;
    }
}

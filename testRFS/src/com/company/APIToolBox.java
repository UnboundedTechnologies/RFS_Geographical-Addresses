package com.company;

import kong.unirest.HttpRequestWithBody;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.apache.http.HttpHeaders;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import io.github.cdimascio.dotenv.Dotenv;


public class APIToolBox implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    static Dotenv dotenv = Dotenv.load();
    protected static final JSONObject bodyToken = new JSONObject();

    public static JSONObject getBodyToken() throws JSONException {
        bodyToken.put("client_id", dotenv.get("CLIENT_ID"));
        bodyToken.put("client_secret",dotenv.get("CLIENT_SECRET"));
        bodyToken.put("grant_type","password");
        bodyToken.put("username",dotenv.get("xxxxxxx"));
        bodyToken.put("password",dotenv.get("xxxxxxxx"));

        return bodyToken;
    }

    public static JSONObject getToken(String url) throws JSONException {
        HttpRequestWithBody request = Unirest.post(url);
        final HttpResponse<String> response = request.header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                .header(HttpHeaders.ACCEPT, "application/json")
                .body(getBodyToken())
                .asString();
        if ( response.isSuccess() ) {
            return new JSONObject(response.getBody());
        } else {
            return new JSONObject().put("code",response.getStatus()).put("message",response.getStatusText());
        }
    }

    public static JSONObject getJSONObject(String url, String token, String apiKey) {
        JSONObject response = null;
        try {
            response = new JSONObject(Unirest.get(url)
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .header("apikey", apiKey)
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                    .header(HttpHeaders.ACCEPT, "application/json")
                    .asString()
                    .getBody());
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static JSONObject putJSONObject(String url, String token, String apiKey, String body) throws JSONException {
        HttpRequestWithBody request = Unirest.put(url);

        final kong.unirest.HttpResponse<String> response = request.header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header("apikey", apiKey)
                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                .header(HttpHeaders.ACCEPT, "application/json")
                .body(body)
                .asString();
        if ( response.isSuccess() ) {
            return new JSONObject(response.getBody());
        } else {
            return new JSONObject().put("code",response.getStatus()).put("message",response.getStatusText());
        }
    }

    //create a method to update an endpoint in API  with a new body
    public static JSONObject update(String rowID, String token, String apiKEY, String body) throws JSONException {
        HttpRequestWithBody request = Unirest.put("https://apis-qa.renault.com/addresses/v1/geographical-addresses/" + rowID);
        final kong.unirest.HttpResponse<String> response = request.header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header("apikey", apiKEY)
                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                .header(HttpHeaders.ACCEPT, "application/json")
                .body(body)
                .asString();
        if ( response.isSuccess() ) {
            return new JSONObject(response.getBody());
        } else {
            return new JSONObject().put("code",response.getStatus()).put("message",response.getStatusText());
        }
    }
    public static JSONObject postJSONObject(String url, String token, String apiKey, String body) throws JSONException {
        HttpRequestWithBody request = Unirest.post(url);
        final kong.unirest.HttpResponse<String> response = request.header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header("apikey", apiKey)
                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                .header(HttpHeaders.ACCEPT, "application/json")
                .body(body)
                .asString();
        if ( response.isSuccess() ) {
            return new JSONObject(response.getBody());
        } else {
            return new JSONObject().put("code",response.getStatus()).put("message",response.getStatusText());
        }
    }

    public static JSONObject postTaxVision(String url, File currencyExchangeRatesSpot, File currencyExchangeRatesAccrued, String jwt) throws JSONException {
        HttpRequestWithBody request = Unirest.post(url);
        final kong.unirest.HttpResponse<String> response = request.header(HttpHeaders.CONTENT_TYPE, "multipart/form-data")
        .field("Currency_Exchange_Rates_Spot", currencyExchangeRatesSpot)
        .field("Currency_Exchange_Rates_Accrued", currencyExchangeRatesAccrued)
        .field("jwt", jwt)
        .asString();
        if (response.isSuccess()) {
            return new JSONObject(response.getBody());
        } else {
            return new JSONObject().put("code", response.getStatus()).put("message", response.getStatusText());
        }
    }

    public static JSONObject delete(String url, String token, String apiKey) throws JSONException {
        HttpRequestWithBody request = Unirest.delete(url);
        final kong.unirest.HttpResponse<String> response = request.header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header("apikey", apiKey)
                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                .header(HttpHeaders.ACCEPT, "application/json")
                .asEmpty();
        if ( response.isSuccess() ) {
            return new JSONObject(response.getBody());
        } else {
            return new JSONObject().put("code",response.getStatus()).put("message",response.getStatusText());
        }
    }
}
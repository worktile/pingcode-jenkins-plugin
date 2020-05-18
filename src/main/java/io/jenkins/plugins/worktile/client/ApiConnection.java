package io.jenkins.plugins.worktile.client;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.jenkins.plugins.worktile.model.WTErrorEntity;
import io.jenkins.plugins.worktile.model.WTRestException;
import okhttp3.*;
import okhttp3.Request.Builder;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Logger;

public class ApiConnection {
  private final String accessToken;
  private final Gson gson =
      new GsonBuilder()
          .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
          .create();
  private final Logger logger = Logger.getLogger(ApiConnection.class.getName());
  private final OkHttpClient httpClient;

  public ApiConnection(OkHttpClient client) {
    this(null, client);
  }

  public ApiConnection(String accessToken, OkHttpClient client) {
    this.accessToken = accessToken;
    this.httpClient = client;
  }

  public ApiConnection() {
    this(null, new OkHttpClient());
  }

  public String executeGet(String url) throws IOException, WTRestException {
    Builder requestBuilder = new Request.Builder().url(url).get();
    return execute(requestBuilder);
  }

  private String execute(Builder requestBuilder) throws IOException, WTRestException {
    if (accessToken != null) {
      requestBuilder.addHeader("Authorization", "Bearer " + accessToken);
    }
    requestBuilder.addHeader("Content-Type", "application/json");
    try (Response response = this.httpClient.newCall(requestBuilder.build()).execute()) {
      if (!response.isSuccessful()) {
        WTErrorEntity error =
            gson.fromJson(Objects.requireNonNull(response.body()).string(), WTErrorEntity.class);
        throw new WTRestException(error.getCode(), error.getMessage());
      }
      return Objects.requireNonNull(response.body()).string();
    }
  }

  public String executePost(String url, Object body) throws IOException, WTRestException {
    MediaType JSONMedia = MediaType.get("application/json; charset=utf-8");
    String json = gson.toJson(body);
    RequestBody reqBody = RequestBody.create(json, JSONMedia);
    Builder requestBuilder = new Request.Builder().url(url).post(reqBody);
    return execute(requestBuilder);
  }

  public <TBody> String executeDelete(String url, Class<TBody> body)
      throws IOException, WTRestException {
    MediaType JSONMedia = MediaType.get("application/json; charset=utf-8");
    String json = gson.toJson(body);
    RequestBody reqBody = RequestBody.create(json, JSONMedia);
    Builder requestBuilder = new Request.Builder().url(url).delete(reqBody);
    return execute(requestBuilder);
  }

  public String executeDelete(String url) throws IOException, WTRestException {
    Builder requestBuilder = new Request.Builder().url(url).delete();
    return execute(requestBuilder);
  }
}

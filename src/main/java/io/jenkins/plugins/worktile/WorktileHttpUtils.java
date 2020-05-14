package io.jenkins.plugins.worktile;

import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WorktileHttpUtils {
    private OkHttpClient httpClient;

    WorktileHttpUtils(OkHttpClient httpClient) {
        setHttpClient(httpClient);
    }

    public OkHttpClient getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public Response doGet(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        try (Response respone = httpClient.newCall(request).execute()) {
            return respone;
        }
    }
}

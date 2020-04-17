package io.jenkins.plugins.worktile;

import java.io.PrintStream;
import java.util.HashMap;

import hudson.Extension;

import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import okhttp3.HttpUrl;

@Extension
@SuppressWarnings("rawtypes")
public class ProjectListener extends RunListener<Run> {

    public ProjectListener() {
        super(Run.class);
    }

    @SuppressWarnings("CastToConcreteClass")
    @Override
    public void onCompleted(Run run, TaskListener listener) {
        PrintStream logger = listener.getLogger();
        logger.println("print this image onComplete");
        StringBuilder builder = new StringBuilder("https://open.worktile.com/v1/auth?");
        HashMap<String, String> params = new HashMap<String, String>();

        params.put("client_id", "hello");
        params.put("grant_type", "client_credentials");
        params.put("client_secret", "world");
        params.forEach((String key, String value) -> builder.append("&" + key + "=" + value));
        logger.println(builder.toString());
        HttpUrl url = new HttpUrl.Builder().addPathSegment("https://open.worktile.com").addPathSegment("v1")
                .addPathSegment("auth").addQueryParameter("grant_type", "client").build();

        logger.println(url.toString());
    }
}

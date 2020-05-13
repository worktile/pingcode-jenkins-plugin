package io.jenkins.plugins.worktile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.cloudbees.plugins.credentials.CredentialsMatcher;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;

import org.jenkinsci.plugins.plaincredentials.StringCredentials;

import hudson.security.ACL;
import jenkins.model.Jenkins;

public final class SecretResolver {

    public static Optional<String> getSecretOf(String credentialsId) {
        final List<StringCredentials> credentials = CredentialsProvider.lookupCredentials(StringCredentials.class,
                Jenkins.get(), ACL.SYSTEM, Collections.emptyList());

        final CredentialsMatcher matcher = CredentialsMatchers.withId(credentialsId);

        return Optional.ofNullable(CredentialsMatchers.firstOrNull(credentials, matcher))
                .flatMap(creds -> Optional.ofNullable(creds.getSecret()))
                .flatMap(secret -> Optional.ofNullable(secret.getPlainText()));
    }
}

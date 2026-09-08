package com.app.book.security;

import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.*;
import io.micronaut.security.authentication.provider.AuthenticationProvider;
import jakarta.inject.Singleton;

import java.util.Collections;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@Singleton
public class AuthenticationProviderUserPassword implements AuthenticationProvider<HttpRequest<?>, String, String> {

	@Override
	public @NonNull AuthenticationResponse authenticate(@Nullable HttpRequest<?> requestContext,
			@NonNull AuthenticationRequest<String, String> authRequest) {
		String identity = authRequest.getIdentity();
        String secret = authRequest.getSecret();
        if ("admin".equals(identity) && "password".equals(secret)) {
            return AuthenticationResponse.success("admin", Collections.singletonList("ROLE_ADMIN"));
        } else if ("user".equals(identity) && "password".equals(secret)) {
            return AuthenticationResponse.success("user", Collections.singletonList("ROLE_USER"));
        } else {
            return AuthenticationResponse.failure("Invalid credentials");
        }
	}
}

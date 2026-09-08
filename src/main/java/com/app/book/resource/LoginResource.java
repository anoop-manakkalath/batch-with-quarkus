package com.app.book.resource;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.authentication.AuthenticationFailed;
import io.micronaut.security.authentication.Authenticator;
import io.micronaut.security.authentication.UsernamePasswordCredentials;
import io.micronaut.security.handlers.LoginHandler;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;
import reactor.core.publisher.Mono;

@Controller("/auth/login")
@Secured(SecurityRule.IS_ANONYMOUS)
public class LoginResource {

	@SuppressWarnings("rawtypes")
	private final Authenticator authenticator;
	
    private final LoginHandler<HttpRequest<?>, MutableHttpResponse<?>> loginHandler;

    @Inject
    public LoginResource(
            Authenticator<MutableHttpResponse<?>> authenticator,
            LoginHandler<HttpRequest<?>, MutableHttpResponse<?>> loginHandler) {
        this.authenticator = authenticator;
        this.loginHandler = loginHandler;
    }

    @SuppressWarnings("unchecked")
	@Post
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Mono<MutableHttpResponse<?>> login(@Body UsernamePasswordCredentials credentials, HttpRequest<?> request) {
        return Mono.from(authenticator.authenticate(request, credentials))
                .map(response -> {
                    if (response instanceof Authentication authentication) {
                        return loginHandler.loginSuccess(authentication, request);
                    }
                    
                    if (response instanceof AuthenticationFailed failed) {
                        return loginHandler.loginFailed(failed, request);
                    }
                    
                    return loginHandler.loginFailed(new AuthenticationFailed("Authentication failed"), request);
                })
                .defaultIfEmpty(HttpResponse.status(HttpStatus.UNAUTHORIZED));
    }
}

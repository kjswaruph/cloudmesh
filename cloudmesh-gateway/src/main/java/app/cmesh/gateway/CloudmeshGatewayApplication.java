package app.cmesh.gateway;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import reactor.core.publisher.Mono;

@SpringBootApplication
@Log4j2
public class CloudmeshGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(CloudmeshGatewayApplication.class, args);
	}

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r.path("/auth/**")
                        .uri("http://backend:8081"))
                .route("project-service", r -> r.path("/api/projects/**")
                        .uri("http://backend:8081"))
                .route("credential-service", r -> r.path("/api/credentials/**")
                        .uri("http://backend:8081"))
                .route("cost-service", r -> r.path("/api/costs/**")
                        .uri("http://backend:8081"))
                .route("user-service", r -> r.path("/api/users/**")
                        .uri("http://backend:8081"))
                .route("hello-service", r -> r.path("/hello")
                        .uri("http://backend:8081"))
                .route("home-redirect", r -> r.path("/", "/login", "/signup")
                        .uri("http://backend:8081"))
                .route("oauth2-login", r -> r.path("/oauth2/**")
                        .uri("http://backend:8081"))
                .route("oauth2-redirect", r -> r.path("/login/oauth2/**")
                        .uri("http://backend:8081"))
                .build();
    }

    @Bean
    @Primary
    public KeyResolver userKeyResolver() {
        return exchange -> ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(auth -> {
                    String username = auth.getName();
                    log.info("Rate limiting by USER: " + username);
                    return username;
                })
                .switchIfEmpty(
                        Mono.fromCallable(() -> {
                            String ip = exchange.getRequest()
                                    .getRemoteAddress()
                                    .getAddress()
                                    .getHostAddress();
                            log.info("Rate limiting by IP: " + ip);
                            return ip;
                        })
                );
    }
}

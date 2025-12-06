package app.cmesh.gateway;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
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

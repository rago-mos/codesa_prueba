package codesa.com.co.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

	@Value("${services.auth.url:http://api-auth:8081}")
	private String authServiceUrl;

	@Value("${services.proyectos.url:http://api-proyectos:8082}")
	private String proyectosServiceUrl;

	@Bean
	public RouteLocator routeLocator(RouteLocatorBuilder builder) {
		return builder.routes()
				.route("auth-service", r -> r
						.path("/auth/**")
						.uri(authServiceUrl))
				.route("proyectos-service", r -> r
						.path("/api/**")
						.uri(proyectosServiceUrl))
				.build();
	}
}

package codesa.com.co.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import codesa.com.co.security.JwtService;
import io.jsonwebtoken.Claims;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

	private final JwtService jwtService;

	public JwtAuthenticationFilter(JwtService jwtService) {
		this.jwtService = jwtService;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		String path = exchange.getRequest().getURI().getPath();

		if (isPublicPath(path)) {
			return chain.filter(exchange);
		}

		String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			ServerHttpResponse response = exchange.getResponse();
			response.setStatusCode(HttpStatus.UNAUTHORIZED);
			return response.setComplete();
		}

		String token = authHeader.substring(7);
		if (!jwtService.isValid(token)) {
			ServerHttpResponse response = exchange.getResponse();
			response.setStatusCode(HttpStatus.UNAUTHORIZED);
			return response.setComplete();
		}

		Claims claims = jwtService.parse(token);
		String username = claims.getSubject();
		String roles = claims.get("roles", String.class);

		ServerHttpRequest mutatedRequest = exchange.getRequest()
				.mutate()
				.header("X-Auth-User", username)
				.header("X-Auth-Roles", roles)
				.build();

		return chain.filter(exchange.mutate().request(mutatedRequest).build());
	}

	@Override
	public int getOrder() {
		return -100;
	}

	private boolean isPublicPath(String path) {
		return path.startsWith("/auth/login") || path.startsWith("/auth/register");
	}
}

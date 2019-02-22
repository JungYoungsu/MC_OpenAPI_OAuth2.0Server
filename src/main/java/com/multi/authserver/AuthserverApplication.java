package com.multi.authserver;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.approval.ApprovalStore;
import org.springframework.security.oauth2.provider.approval.InMemoryApprovalStore;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.InMemoryAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

@EnableAuthorizationServer
@SpringBootApplication
public class AuthserverApplication extends AuthorizationServerConfigurerAdapter {
	@Autowired
	DataSource dataSource; // application.properties - # datasource
	@Autowired
	private AuthenticationManager authManager;

	@Bean
	public JwtAccessTokenConverter accessTokenConverter() {
		JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
		converter.setSigningKey("SECRET KEY");
		return converter;
	}
	
	@Bean
	public TokenStore tokenStore() {
		return new JwtTokenStore(accessTokenConverter());
	}
	
	// Authentication Code Grant 에 쓰임. 안쓰면 Implicit 만 가능
	public ApprovalStore inmemoryApprovalStore() {
		return new InMemoryApprovalStore(); // 승인은 DB 안 씀. 일시적인 것이므로
	}

	// Authentication Code Grant 에 쓰임. 안쓰면 Implicit 만 가능
	public AuthorizationCodeServices inmemoryAuthorizationCodeServices() {
		return new InMemoryAuthorizationCodeServices(); // auth 코드 발급하는 과정도 DB 안 씀.
	}

	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		clients.inMemory()
			.withClient("client1").secret("1234")
				.authorizedGrantTypes("authorization_code", "implicit",
				"refresh_token", "password", "client_credentials")
				.scopes("profile","read","message")
				.accessTokenValiditySeconds(3600)
				.refreshTokenValiditySeconds(-1); // DB 대신 인메모리에 클라이언트 등록
	}

	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
		endpoints.tokenStore(tokenStore())
			.accessTokenConverter(accessTokenConverter())
			.approvalStore(inmemoryApprovalStore())
			.authorizationCodeServices(inmemoryAuthorizationCodeServices())
			.authenticationManager(authManager);
	}
	
	@Override
	public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
		security.allowFormAuthenticationForClients();
	} // solve 'Full authentication is required to access this resource'

	public static void main(String[] args) {
		SpringApplication.run(AuthserverApplication.class, args);
	}
}

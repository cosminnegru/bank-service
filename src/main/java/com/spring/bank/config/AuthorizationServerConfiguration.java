package com.spring.bank.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;

@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

    @Value("${user.read.oauth.clientId}")
    private String userReadClientId;

    @Value("${user.read.oauth.clientSecret}")
    private String userReadClientSecret;

    @Value("${user.write.oauth.clientId}")
    private String userWriteClientId;

    @Value("${user.write.oauth.clientSecret}")
    private String userWriteClientSecret;

    @Override
    public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
        oauthServer.tokenKeyAccess("permitAll()")
                .checkTokenAccess("isAuthenticated()");
    }

    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients
                .inMemory()
                .withClient(userReadClientId)
                .secret(passwordEncoder().encode(userReadClientSecret))
                .authorizedGrantTypes("client_credentials", "refresh_token")
                .scopes("read")
                .and()
                .withClient(userWriteClientId)
                .secret(passwordEncoder().encode(userWriteClientSecret))
                .authorizedGrantTypes("client_credentials", "refresh_token")
                .scopes("write");
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}

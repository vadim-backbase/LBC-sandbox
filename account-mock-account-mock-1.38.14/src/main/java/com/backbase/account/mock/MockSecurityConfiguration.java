package com.backbase.account.mock;

import com.backbase.buildingblocks.security.csrf.DisableAutoCsrfWebSecurityConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Order(1000)
@EnableWebSecurity
@DisableAutoCsrfWebSecurityConfiguration
public class MockSecurityConfiguration extends WebSecurityConfigurerAdapter {

    private static final String[] ALLOWED_ANT_MATCHES = {"/rest/**", "/proxy/**"};

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            .antMatchers(
                ALLOWED_ANT_MATCHES)
            .permitAll()
            .and()
            .csrf().disable();
    }
}

package com.pablo.restApi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true,
        prePostEnabled = true
)
public class WebSecurityConfig {

    @Bean
    WebSecurityConfigurerAdapter withUser() {
        return new WebSecurityConfigurerAdapter() {

            @Override
            protected void configure(AuthenticationManagerBuilder auth) throws Exception {
                auth.inMemoryAuthentication()
                        .withUser("admin").password("{noop}@dmin").roles("ADMIN", "USER")
                        .and().withUser("bob").password("{noop}b00b").roles("USER");
            }

            @Override
            protected void configure(HttpSecurity http) throws Exception {
                http
                        .cors().and().csrf().disable()
                        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                        .httpBasic().realmName("TEST").authenticationEntryPoint(getBasicAuthEntryPoint()).and()
                        .authorizeRequests()
                            .antMatchers(HttpMethod.DELETE).hasRole("ADMIN")
                            .antMatchers(HttpMethod.POST).hasRole("ADMIN")
                            .antMatchers(HttpMethod.PUT).hasRole("ADMIN")
                            .antMatchers(HttpMethod.PATCH).hasRole("ADMIN")
                            .antMatchers("/messenger/**").permitAll()
                            .antMatchers("/app/**").permitAll()
                            .antMatchers("/topic/**").permitAll()
                        .anyRequest().authenticated();
            }

            @Bean
            public CustomBasicAuthenticationEntryPoint getBasicAuthEntryPoint(){
                return new CustomBasicAuthenticationEntryPoint();
            }
        };
    }

    public class CustomBasicAuthenticationEntryPoint extends BasicAuthenticationEntryPoint {

        @Override
        public void commence(final HttpServletRequest request,
                             final HttpServletResponse response,
                             final AuthenticationException authException) throws IOException, ServletException {
            //Authentication failed, send error response.
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.addHeader("WWW-Authenticate", "Basic realm=" + getRealmName() + "");

            PrintWriter writer = response.getWriter();
            writer.println("HTTP Status 401 : " + authException.getMessage());
        }

        @Override
        public void afterPropertiesSet() throws Exception {
            setRealmName("TEST");
            super.afterPropertiesSet();
        }
    }
}

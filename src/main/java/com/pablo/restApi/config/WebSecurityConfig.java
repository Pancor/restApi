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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true,
        prePostEnabled = true
)
public class WebSecurityConfig {

//    @Bean
//    @Profile("dev")
//    WebSecurityConfigurerAdapter noUser() {
//        return new WebSecurityConfigurerAdapter() {
//
//            @Override
//            protected void configure(HttpSecurity http) throws Exception {
//                http.authorizeRequests()
//                        .antMatchers(HttpMethod.DELETE).hasRole("ADMIN")
//                        .antMatchers(HttpMethod.POST).hasRole("ADMIN")
//                        .antMatchers(HttpMethod.PUT).hasRole("ADMIN")
//                        .antMatchers(HttpMethod.PATCH).hasRole("ADMIN")
//                        .anyRequest().authenticated();
//            }
//        };
//    }

    @Bean
//    @Profile("test")
    WebSecurityConfigurerAdapter withUser() {
        return new WebSecurityConfigurerAdapter() {

            @Override
            protected void configure(AuthenticationManagerBuilder auth) throws Exception {
                auth.inMemoryAuthentication()
                        .withUser("admin").password(encoder().encode("@dmin")).roles("ADMIN", "USER")
                        .and().withUser("bob").password(encoder().encode("b00b")).roles("USER");
            }

            @Override
            protected void configure(HttpSecurity http) throws Exception {
                http
                        .cors().and().csrf().disable()
                        .httpBasic().disable()
                        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
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
            PasswordEncoder encoder() {
                return new BCryptPasswordEncoder();
            }
        };
    }
}

package com.mvprestaurante.mvp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.mvprestaurante.mvp.multitenant.SubdomainExtractor;
import com.mvprestaurante.mvp.multitenant.TenanFilter;
import com.mvprestaurante.mvp.multitenant.TenantResolverService;
import com.mvprestaurante.mvp.security.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        private final CustomUserDetailsService userDetailsService;
        private final SubdomainExtractor extractor;
        private final TenantResolverService resolver;

        public SecurityConfig(CustomUserDetailsService userDetailsService, SubdomainExtractor extractor,
                        TenantResolverService resolver) {
                this.userDetailsService = userDetailsService;
                this.extractor = extractor;
                this.resolver = resolver;
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http

                                .addFilterBefore(new TenanFilter(extractor, resolver),
                                                UsernamePasswordAuthenticationFilter.class)
                                .authorizeHttpRequests(authz -> authz
                                                .requestMatchers("/", "/registro", "/empresa/guardar", "/css/**",
                                                                "/js/**")
                                                .permitAll()
                                                .requestMatchers("/login").permitAll()
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .defaultSuccessUrl("/dashboard", true)
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutSuccessUrl("/login?logout=true")
                                                .permitAll())

                                .securityContext(context -> context
                                                .requireExplicitSave(false))

                                .userDetailsService(userDetailsService);
                ;

                return http.build();
        }
}
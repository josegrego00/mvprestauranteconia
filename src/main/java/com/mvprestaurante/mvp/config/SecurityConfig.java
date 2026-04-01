package com.mvprestaurante.mvp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.mvprestaurante.mvp.multitenant.SubdomainExtractor;
import com.mvprestaurante.mvp.multitenant.TenanFilter;
import com.mvprestaurante.mvp.multitenant.TenantResolverService;
import com.mvprestaurante.mvp.security.NormalUserDetailsService;
import com.mvprestaurante.mvp.security.SuperAdminUserDetailsService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

        private final SubdomainExtractor extractor;
        private final TenantResolverService resolver;
        private final SuperAdminUserDetailsService superAdminUserDetailsService;
        private final NormalUserDetailsService normalUserDetailsService;

        public SecurityConfig(SubdomainExtractor extractor,
                        TenantResolverService resolver,
                        SuperAdminUserDetailsService superAdminUserDetailsService,
                        NormalUserDetailsService normalUserDetailsService) {
                this.extractor = extractor;
                this.resolver = resolver;
                this.superAdminUserDetailsService = superAdminUserDetailsService;
                this.normalUserDetailsService = normalUserDetailsService;
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        @Order(1)
        public SecurityFilterChain superAdminFilterChain(HttpSecurity http) throws Exception {
                http
                                .securityMatcher("/superadmin/**", "/setup")
                                .csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(authz -> authz
                                                .requestMatchers("/superadmin/login", "/setup").permitAll()
                                                .requestMatchers("/superadmin/**").hasRole("SUPERADMIN"))
                                .formLogin(form -> form
                                                .loginPage("/superadmin/login")
                                                .loginProcessingUrl("/superadmin/login")
                                                .defaultSuccessUrl("/superadmin/empresas", true)
                                                .failureUrl("/superadmin/login?error=true")
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutSuccessUrl("/superadmin/login?logout=true")
                                                .permitAll())
                                .userDetailsService(superAdminUserDetailsService);

                return http.build();
        }

        @Bean
        @Order(2)
        public SecurityFilterChain normalFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .addFilterBefore(new TenanFilter(extractor, resolver),
                                                UsernamePasswordAuthenticationFilter.class)
                                .authorizeHttpRequests(authz -> authz
                                                .requestMatchers("/", "/registro", "/empresa/guardar", "/empresa/espera-activacion", "/css/**",
                                                                "/js/**", "/error/**")
                                                .permitAll()
                                                .requestMatchers("/login").permitAll()
                                                .requestMatchers("/superadmin/**").denyAll()
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

                                .userDetailsService(normalUserDetailsService);

                return http.build();
        }
}

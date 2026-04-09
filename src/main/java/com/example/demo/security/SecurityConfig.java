package com.example.demo.security;

import com.example.demo.model.Usuarios;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.service.UserDetailsServiceImpl;
import org.springframework.http.HttpMethod;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtAuthenticationFilter jwtFilter;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(request -> {
                var config = new org.springframework.web.cors.CorsConfiguration();
                config.setAllowedOrigins(java.util.List.of("http://localhost:5173",
                "https://kitchen-manager-front.vercel.app"));
                config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
                config.setAllowedHeaders(java.util.List.of("*"));
                config.setAllowCredentials(true);
                return config;
            }))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/login", "/dashboard", "/clientes/**", "/seguimiento/**", "/menu/**", "/uploads/**", "/admin/menu/**/imagen").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/empleado/**").hasAnyRole("ADMIN", "EMPLEADO")
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
    @Bean
    public CommandLineRunner initData(UsuarioRepository usuarioRepository) {
        return args -> {
            if (usuarioRepository.findByUsername("admin")== null) {
                Usuarios admin = new Usuarios();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder().encode("1234"));
                admin.setRoles(List.of("ADMIN"));
                usuarioRepository.save(admin);
                System.out.println("Usuario admin creado");
            }

            if (usuarioRepository.findByUsername("empleado") == null) {
                Usuarios empleado = new Usuarios();
                empleado.setUsername("empleado");
                empleado.setPassword(passwordEncoder().encode("1234"));
                empleado.setRoles(List.of("EMPLEADO"));
                usuarioRepository.save(empleado);
                System.out.println("Usuario empleado creado");
            }
        };
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder()); // Usar BCrypt para las contraseñas
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Esto encripta las contraseñas en la base de datos
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

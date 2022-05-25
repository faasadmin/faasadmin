package com.faasadmin.faas.modules.admin.admin.core.configuration;

import com.faasadmin.faas.modules.admin.admin.core.jwt.CustomJwtGrantedAuthoritiesConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

//@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 10)
//@EnableGlobalMethodSecurity(
//        prePostEnabled = true,
//        order = 0
//)
//@EnableWebSecurity
//@Configuration
//@Order(102)
public class ResourceServerConfiguration extends WebSecurityConfigurerAdapter  {

    @Value("${jwk.set.uri}")
    private String jwkSetUri;

    //@Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }

    JwtAuthenticationConverter jwtAuthenticationConverter() {
        CustomJwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new CustomJwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("");
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //http.mvcMatcher("/messages/**").authorizeRequests()
        //        .anyRequest().authenticated()
        //        //.mvcMatchers("/messages/**").access("hasAuthority('USER')")
        //        .and()
        //        .oauth2ResourceServer(oauth2 -> oauth2
        //                .jwt(jwt -> jwt.decoder(jwtDecoder()).jwtAuthenticationConverter(jwtAuthenticationConverter()))
        //        ).build();
    }

    //@Bean
    //SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    //    http.mvcMatcher("/messages/**").authorizeRequests()
    //            .anyRequest().authenticated()
    //            //.mvcMatchers("/messages/**").access("hasAuthority('USER')")
    //            .and()
    //            .oauth2ResourceServer(oauth2 -> oauth2
    //                    .jwt(jwt -> jwt.decoder(jwtDecoder()).jwtAuthenticationConverter(jwtAuthenticationConverter()))
    //            );
    //    return http.build();
    //}

}

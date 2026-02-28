package com.gabinote.gateway.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.web.server.SecurityWebFilterChain
import reactor.core.publisher.Mono

@Configuration
@EnableWebFluxSecurity
class SecurityConfig {

    @Bean
    fun clientSecurityFilterChain(
        http: ServerHttpSecurity,
        authenticationConverter: Converter<Jwt, AbstractAuthenticationToken>,
    ): SecurityWebFilterChain {
        // CSRF 보호 비활성화
        http.csrf { it.disable() }

//        // STATELESS 세션
//        http.securityContextRepository(NoOpServerSecurityContextRepository.getInstance())

//        // CORS 설정
//        http.cors(Customizer.withDefaults())

        // formLogin 비활성화
        http.formLogin { it.disable() }
        http.httpBasic { it.disable() }
        http.logout { it.disable() }

        http
            .authorizeExchange { exchanges ->
                exchanges
                    .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .pathMatchers("/management/**").hasRole("ADMIN") // 해당 경로만 ROLE_ADMIN 요구
                    .anyExchange().permitAll() // 나머지는 Resource Server의 기본 인증 흐름에 맡김
            }

        http.oauth2ResourceServer { resourceServer ->
            resourceServer.jwt { jwtSpec ->
                jwtSpec.jwtAuthenticationConverter { jwt -> Mono.justOrEmpty(authenticationConverter.convert(jwt)) }
            }
        }


        return http.build()
    }

    @Bean
    fun realmRolesAuthoritiesConverter(): AuthoritiesConverter =
        AuthoritiesConverter { claims ->
            val realmAccess = claims["realm_access"] as? Map<String, Any>
            val roles = realmAccess?.get("roles") as? List<String>
            roles
                ?.map { SimpleGrantedAuthority(it) as GrantedAuthority }
                ?: emptyList()
        }

    fun interface AuthoritiesConverter :
        Converter<Map<String, Any>, Collection<GrantedAuthority>>

    @Bean
    fun authenticationConverter(authoritiesConverter: AuthoritiesConverter): JwtAuthenticationConverter {
        val authenticationConverter = JwtAuthenticationConverter()
        authenticationConverter.setJwtGrantedAuthoritiesConverter(Converter { jwt: Jwt? ->
            authoritiesConverter.convert(
                jwt!!.claims
            )
        })
        return authenticationConverter
    }

    @Bean
    fun grantedAuthenticationConverter(
        authoritiesConverter: AuthoritiesConverter,
    ): GrantedAuthoritiesMapper =
        GrantedAuthoritiesMapper { authorities ->
            authorities
                .filterIsInstance<OidcUserAuthority>()
                .map { it.idToken.claims }
                .flatMap { claims -> authoritiesConverter.convert(claims)!!.asSequence() }
                .toSet()
        }
}
package com.example.stock.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
@Slf4j
public class SecurityConfiguration extends WebSecurityConfigurerAdapter{
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    //
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http   //어떤 경로를 허용하고, 어떤 권한을 필요로 하는지 정의
                //밑 3줄은 RestApi jwt토큰 인증방식을 할때 붙혀줘야 하는 부분
                .httpBasic().disable()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                //실질적인 권한 제어에 해당하는 부분
                .authorizeRequests()
                .antMatchers("/**/signup", "/**/signin").permitAll()
                //회원가입과 로그인은 토큰(인증정보)없이 접근을 허락한다.
                .and()
                .addFilterBefore(this.jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        //JWT토큰은 상태정보를 저장하지 않는 STATELSS라는 특징이 있다
        //반대로 Session으로 구현을 STATE가 있는 상태로 구현한다
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
                .antMatchers("/h2-console/**"); //h2-console로 시작하는 경로로
        //api를 호출하게 되면 그것에 대한 인증정보는 무시를 하겠다
        //>>인증관련 정보가 없어도 자유롭게 접근할 수 있다

    }

    //spring boot 2.x 버전 부터 메소드를 선언해서 써야함
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

}

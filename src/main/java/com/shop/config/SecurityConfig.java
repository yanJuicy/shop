package com.shop.config;

import com.shop.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    MemberService memberService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.formLogin()
                .loginPage("/members/login")    // 로그인 페이지 URL
                .defaultSuccessUrl("/")         // 로그인 성공 시 이동할 URL
                .usernameParameter("email")     // 로그인 시 사용할 파라미터 이름을 email로 지정
                .failureUrl("/members/login/error") // 로그인 실패 시 이동할 URL
                .and()
                .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/members/logout")) // 로그아웃 URL 설정
                .logoutSuccessUrl("/"); // 로그아웃 성공 시 이동할 URL

        http.authorizeRequests()    // 시큐리티 처리에 HttpServletRequest를 이용한다
                .mvcMatchers("/", "/members/**", "/item/**", "/images/**").permitAll()  // 모든 사용자가 인증 없이 해당 경로에 접근할 수 있도록 설정한다
                .mvcMatchers("/admin/**").hasRole("ADMIN")  // /admin으로 시작하는 경로는 해당 계정이 ADMIN role일 경우에만 접근이 가능하다
                .anyRequest().authenticated();  // 나머지 경로들은 모두 인증을 요구한다

        http.exceptionHandling()
                .authenticationEntryPoint(new CustomAuthenticationEntryPoint());    // 인증되지 않은 사용자가 리소스에 접근하였을 때 수행되는 핸들러 등록
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/css/**", "/js/**", "/img/**"); // static 디렉터리 하위 파일은 인증을 무시한다
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception { // Spring Security에서 인증은 AuthenticationManager를 통해 이루어진다.
        auth.userDetailsService(memberService)    // AuthenticationmanagerBuilder가 AuthenticationManager를 생성한다.
                .passwordEncoder(passwordEncoder());    // userDetailDervice를 구현하고 있는 객체로 memeberService를 지정해주며, 비밀번호 암호화를 위해 passwordEncoder를 지정해준다.
    }

}

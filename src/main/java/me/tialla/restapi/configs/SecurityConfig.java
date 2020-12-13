package me.tialla.restapi.configs;

import me.tialla.restapi.accounts.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    AccountService accountService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Bean
    public TokenStore tokenStore(){
        return new InMemoryTokenStore();
    }

    // AutherizationServer랑 ResourceServer에서 해당 AuthenticationManager를 참조할 수 있도록 bean으로 노출
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    /*
    Spring Security 참고 정보
    https://jungeunlee95.github.io/java/2019/07/17/2-Spring-Security/
     */
    
    // Spring SecurityFilter 연결을 설정하기 위한 오버라이딩
    // 예외 웹접근 URL을 설정한다.
    // ACL(Access Control List - 접근 제어 목록)의 예외 URL을 설정
    @Override
    public void configure(WebSecurity web) throws Exception {
        //web.ignoring().mvcMatchers("/**");
        web.ignoring().mvcMatchers("/docs/index.html"); //maven package 후 /docs/index.html있는지 확인 ~~;;
        web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    // 인터셉터로 요청을 안전하게 보호하는 방법을 설정하기 위한 오버라이딩
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //super.configure(http); //모든 URL을 막음.
                http
                .anonymous()//익명사용자 허용
                .and()
                .formLogin()//form인증 사용 사용자 계정,email 인증 등등...추가할수 있다.
                .and()
                .authorizeRequests()//내가 허용할 메소드
                    //.mvcMatchers(HttpMethod.GET, "/api/**").anonymous() //get요청으로 api로 오는 것을 anonymouse로 허용
                    .mvcMatchers(HttpMethod.GET, "/api/**").authenticated()
                    .anyRequest().authenticated() //나머지는 인증이 필요함.

                ;
    }

    // 사용자 세부 서비스를 설정하기 위한 오버라이딩
    // AuthenticationManager를 어떻게 만들것인가?
    // userDetailsService는 내가 구현한 accountService로
    // passwordEncoder는 내가 등록한 passwordEncoder로
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(accountService)
                .passwordEncoder(passwordEncoder);
    }
}

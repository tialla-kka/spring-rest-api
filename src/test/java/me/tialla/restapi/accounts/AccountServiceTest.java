package me.tialla.restapi.accounts;

import me.tialla.restapi.common.BaseTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AccountServiceTest extends BaseTest {

    @Autowired
    AccountService accountService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    public void findByUsername(){
        //Given
        String password ="pass";
        String username = "tialla@email.com";
        Account account = Account.builder()
                .email(username)
                .password(password)
                .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
                .build();

        this.accountService.saveAccount(account);

        //When
        UserDetailsService userDetailsService = (UserDetailsService) accountService;
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        //Then
        assertThat(this.passwordEncoder.matches(password,userDetails.getPassword())).isTrue(); //(입력한값, DB값)
    }

    @Test
    public void findByUsernameFail(){
        String username = "random@email.com";
        assertThrows(UsernameNotFoundException.class, ()-> accountService.loadUserByUsername(username));
    }
}
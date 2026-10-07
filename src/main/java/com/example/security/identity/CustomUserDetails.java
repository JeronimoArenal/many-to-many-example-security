package com.example.security.identity;

import com.example.security.model.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;

@Getter
public class CustomUserDetails implements UserDetails {

    private final User user;
    private final UUID uuid;
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    private final boolean enabled;
    private final boolean accountNonLocked;
    private final boolean accountNonExpired;
    private final boolean credentialsNonExpired;

    public CustomUserDetails(
            User user,
            Collection<? extends GrantedAuthority> authorities
    ) {
        this.user = user;
        this.uuid = user.getId();
        this.username = user.getEmail();
        this.password = user.getPasswordHash();
        this.authorities = authorities;

        this.enabled = user.isEnabled();
        this.accountNonLocked = !user.isAccountLocked();
        this.accountNonExpired = !user.isAccountExpired();
        this.credentialsNonExpired = !user.isCredentialsExpired();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
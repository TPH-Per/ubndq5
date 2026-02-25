package com.example.demo.security;

import com.example.demo.entity.Staff;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Custom implementation of UserDetails interface
 * 
 * UserDetails is Spring Security interface representing authenticated user
 * info.
 * Spring Security uses this object to:
 * - Check password
 * - Check authorities/roles
 * - Check account status (locked, expired, enabled...)
 */
@Getter
public class CustomUserDetails implements UserDetails {

    /**
     * Store original Staff entity for detailed info access
     */
    private final Staff staff;

    public CustomUserDetails(Staff staff) {
        this.staff = staff;
    }

    /**
     * Return list of user authorities
     * 
     * Prefix "ROLE_" is Spring Security convention for role-based authorization
     * Example: ROLE_Admin, ROLE_Staff
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String roleName = staff.getRole().getRoleName();
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + roleName));
    }

    /**
     * Return hashed password for Spring Security comparison
     */
    @Override
    public String getPassword() {
        return staff.getPasswordHash();
    }

    /**
     * Return username (using staffCode as username)
     */
    @Override
    public String getUsername() {
        return staff.getStaffCode();
    }

    /**
     * Is account not expired?
     * true = not expired (can login)
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Is account not locked?
     * true = not locked (can login)
     */
    @Override
    public boolean isAccountNonLocked() {
        return staff.getIsActive() != null && staff.getIsActive();
    }

    /**
     * Are credentials not expired?
     * true = not expired
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Is account enabled?
     * true = enabled (can login)
     */
    @Override
    public boolean isEnabled() {
        return staff.getIsActive() != null && staff.getIsActive();
    }

    // ================== Helper methods ==================

    /**
     * Get staff ID
     */
    public Integer getId() {
        return staff.getId();
    }

    /**
     * Get full name
     */
    public String getFullName() {
        return staff.getFullName();
    }

    /**
     * Get role name
     */
    public String getRoleName() {
        return staff.getRole().getRoleName();
    }
}

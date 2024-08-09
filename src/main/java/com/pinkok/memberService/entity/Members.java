package com.pinkok.memberService.entity;

import com.pinkok.memberService.enums.Provider;
import com.pinkok.memberService.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "members")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Members {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long memberIdx;

    @Column(nullable = false, length = 50, unique = true)
    private String email;

    @Column(length = 250)
    private String password;

    @Column(nullable = false, length = 13)
    private String username;

    @Column(length = 13)
    private String phone;

    @Column(nullable = true, length = 255)
    private String profile;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private Provider provider;

    @Column(nullable = true, length = 255)
    private String refresh;

    @ColumnDefault("false")
    private boolean isOauth;

    @ColumnDefault("false")
    private boolean isDormant;

    @ColumnDefault("false")
    private boolean isDenied;

    @Builder
    Members(String email, String password, String name, String phone, Role role){
        this.email = email;
        this.password = password;
        this.username = name;
        this.phone = phone;
        this.role = role;
        this.provider = Provider.NULL;
    }

    public Collection<GrantedAuthority> getAuthority(){
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(this.role.getRoleKey()));
        return authorities;
    }
}

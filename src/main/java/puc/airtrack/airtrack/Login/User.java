package puc.airtrack.airtrack.Login;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@Entity
@Table(name = "base_engenheiro")
public class User implements UserDetails {
    @Id
    @Column(name = "ID_Engenheiro")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "Nome_Engenheiro")
    private String name;

    @Column(name = "Email_Engenheiro")
    private String username;

    @Column(name = "Senha_Engenheiro")
    private String password;

    @Column(name = "Role_Engenheiro")
    private UserRole role;

    @Column(name = "Status_Engenheiro")
    private Boolean status;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if(this.role.getRole() == 4) {
            return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_ENGENHEIRO"), new SimpleGrantedAuthority("ROLE_SUPERVISOR"), new SimpleGrantedAuthority("ROLE_AUDITOR"));
        } else if(this.role.getRole() == 3) {
            return List.of(new SimpleGrantedAuthority("ROLE_SUPERVISOR"));
        } else if(this.role.getRole() == 2) {
            return List.of(new SimpleGrantedAuthority("ROLE_AUDITOR"));
        } else if(this.role.getRole() == 1) {
            return List.of(new SimpleGrantedAuthority("ROLE_ENGENHEIRO"));
        }
        return null;
    }

    @Override
    public boolean isEnabled() {
        return this.status;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
}

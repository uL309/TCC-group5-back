package puc.airtrack.airtrack.Login;

import java.util.Collection;
import java.util.List;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

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

    @Column(name = "Email_Engenheiro", unique = true)
    private String username;

    @Column(name = "Senha_Engenheiro")
    private String password;

    @Column(name = "Role_Engenheiro")
    @Enumerated(EnumType.ORDINAL)
    private UserRole role;

    @Column(name = "Status_Engenheiro")
    private Boolean status;

    @Column(name = "Salario_Engenheiro")
    private float salario;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(this.role.name()));
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

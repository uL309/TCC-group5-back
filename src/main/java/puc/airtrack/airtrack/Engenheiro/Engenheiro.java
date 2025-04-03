package puc.airtrack.airtrack.Engenheiro;

import jakarta.persistence.Entity;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@Entity
public class Engenheiro {
    private String nome;
    private String username;
    private String password;


}

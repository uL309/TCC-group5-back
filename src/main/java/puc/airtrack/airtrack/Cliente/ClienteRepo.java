package puc.airtrack.airtrack.Cliente;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import io.micrometer.common.lang.NonNull;

@Repository
public interface ClienteRepo extends JpaRepository<Cliente, Integer> {

    @NonNull
    Optional<Cliente> findByCpf(@NonNull String cpf);
}
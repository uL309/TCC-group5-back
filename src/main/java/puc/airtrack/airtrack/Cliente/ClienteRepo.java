package puc.airtrack.airtrack.Cliente;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import io.micrometer.common.lang.NonNull;

@Repository
public interface ClienteRepo extends JpaRepository<Cliente, String> {
    @NonNull Optional<Cliente> findById(@NonNull String id);
}
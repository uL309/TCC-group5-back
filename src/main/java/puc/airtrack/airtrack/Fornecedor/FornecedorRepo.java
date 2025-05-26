package puc.airtrack.airtrack.Fornecedor;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.micrometer.common.lang.NonNull;



@Repository
public interface FornecedorRepo extends JpaRepository<Fornecedor, String> {
    @NonNull Optional<Fornecedor> findById(@NonNull String id);
    List<Fornecedor> findByCategoria(String categoria);
}

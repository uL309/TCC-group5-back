package puc.airtrack.airtrack.Fornecedor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface FornecedorRepo extends JpaRepository<Fornecedor, String> {

}

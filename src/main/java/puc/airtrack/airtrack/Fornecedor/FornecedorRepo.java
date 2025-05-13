package puc.airtrack.airtrack.Fornecedor;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;



@Repository
public interface FornecedorRepo extends JpaRepository<Fornecedor, Integer>{
    Fornecedor findById(int id);
}

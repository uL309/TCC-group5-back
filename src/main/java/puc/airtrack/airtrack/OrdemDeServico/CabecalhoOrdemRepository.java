package puc.airtrack.airtrack.OrdemDeServico;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CabecalhoOrdemRepository extends JpaRepository<CabecalhoOrdem, Integer> {
    // Add custom query methods if needed
}

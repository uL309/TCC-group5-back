package puc.airtrack.airtrack.OrdemDeServico;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CabecalhoOrdemRepository extends JpaRepository<CabecalhoOrdem, Integer> {
    List<CabecalhoOrdem> findAllByOrderByIdDesc();
}

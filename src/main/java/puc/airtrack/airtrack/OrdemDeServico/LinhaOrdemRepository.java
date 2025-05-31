package puc.airtrack.airtrack.OrdemDeServico;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LinhaOrdemRepository extends JpaRepository<LinhaOrdem, Integer> {
    List<LinhaOrdem> findByOrdem_Id(Integer ordemId);
}

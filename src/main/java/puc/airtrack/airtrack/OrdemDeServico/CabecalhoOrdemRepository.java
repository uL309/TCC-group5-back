package puc.airtrack.airtrack.OrdemDeServico;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import puc.airtrack.airtrack.Login.User;

import java.util.List;

@Repository
public interface CabecalhoOrdemRepository extends JpaRepository<CabecalhoOrdem, Integer> {
    List<CabecalhoOrdem> findAllByOrderByIdDesc();
    List<CabecalhoOrdem> findByEngenheiroAtuanteOrderByIdDesc(User engenheiro);
    List<CabecalhoOrdem> findByEngenheiroAtuanteAndStatusOrderByIdDesc(User engenheiro, OrdemStatus status);
}

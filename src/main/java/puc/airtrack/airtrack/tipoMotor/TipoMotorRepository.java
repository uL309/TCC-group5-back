package puc.airtrack.airtrack.tipoMotor;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoMotorRepository extends JpaRepository<TipoMotor, Integer> {
    List<TipoMotor> findByMarca(String marca);
    TipoMotor findByMarcaAndModelo(String marca, String modelo);
}

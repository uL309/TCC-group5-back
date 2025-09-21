package puc.airtrack.airtrack.tipoMotor;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface tipoMotorRepository extends JpaRepository<tipoMotor, Integer> {
    List<tipoMotor> findByMarca(String marca);
    tipoMotor findByMarcaAndModelo(String marca, String modelo);
}

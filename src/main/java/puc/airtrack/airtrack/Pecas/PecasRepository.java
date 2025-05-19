package puc.airtrack.airtrack.Pecas;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PecasRepository extends JpaRepository<Pecas, Integer> {

}
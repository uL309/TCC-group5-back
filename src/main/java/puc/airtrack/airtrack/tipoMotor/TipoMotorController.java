package puc.airtrack.airtrack.tipoMotor;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class TipoMotorController {
    
    @Autowired
    private TipoMotorRepository tipomotorRepository;


    @GetMapping("/gtipomotores")
    public ResponseEntity<List<TipoMotorDTO>> getAllFornecedores() {
        List<TipoMotor> tipomotores = tipomotorRepository.findAll();
        List<TipoMotorDTO> tipomotorDTOs = new ArrayList<>();
       for (TipoMotor tipomotor : tipomotores) {
            TipoMotorDTO tipomotorDTO = new TipoMotorDTO();
            tipomotorDTO.setId(tipomotor.getId());
            tipomotorDTO.setMarca(tipomotor.getMarca());
            tipomotorDTO.setModelo(tipomotor.getModelo());
            tipomotorDTO.setTbo(tipomotor.getTbo());
            tipomotorDTOs.add(tipomotorDTO);
        }
        return ResponseEntity.ok(tipomotorDTOs);
    }

    @GetMapping("/gtipomotorm")
    public ResponseEntity<List<TipoMotorDTO>> getFornecedorbyMarca(@RequestParam String marca) {
        List<TipoMotor> tipomotores = tipomotorRepository.findByMarca(marca);
        List<TipoMotorDTO> tipomotorDTOs = new ArrayList<>();
        for (TipoMotor tipomotor : tipomotores) {
            TipoMotorDTO tipomotorDTO = new TipoMotorDTO();
            tipomotorDTO.setId(tipomotor.getId());
            tipomotorDTO.setMarca(tipomotor.getMarca());
            tipomotorDTO.setModelo(tipomotor.getModelo());
            tipomotorDTO.setTbo(tipomotor.getTbo());
            tipomotorDTOs.add(tipomotorDTO);
        }
        return ResponseEntity.ok(tipomotorDTOs);
        }
}

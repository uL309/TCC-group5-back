package puc.airtrack.airtrack.tipoMotor;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class tipoMotorController {
    
    @Autowired
    private tipoMotorRepository tipomotorRepository;


    @GetMapping("/gtipomotores")
    public ResponseEntity<List<tipoMotorDTO>> getAllFornecedores() {
        List<tipoMotor> tipomotores = tipomotorRepository.findAll();
        List<tipoMotorDTO> tipomotorDTOs = new ArrayList<>();
       for (tipoMotor tipomotor : tipomotores) {
            tipoMotorDTO tipomotorDTO = new tipoMotorDTO();
            tipomotorDTO.setId(tipomotor.getId());
            tipomotorDTO.setMarca(tipomotor.getMarca());
            tipomotorDTO.setModelo(tipomotor.getModelo());
            tipomotorDTO.setTbo(tipomotor.getTbo());
            tipomotorDTOs.add(tipomotorDTO);
        }
        return ResponseEntity.ok(tipomotorDTOs);
    }

    @GetMapping("/gtipomotorm")
    public ResponseEntity<List<tipoMotorDTO>> getFornecedorbyMarca(@RequestParam String marca) {
        List<tipoMotor> tipomotores = tipomotorRepository.findByMarca(marca);
        List<tipoMotorDTO> tipomotorDTOs = new ArrayList<>();
        for (tipoMotor tipomotor : tipomotores) {
            tipoMotorDTO tipomotorDTO = new tipoMotorDTO();
            tipomotorDTO.setId(tipomotor.getId());
            tipomotorDTO.setMarca(tipomotor.getMarca());
            tipomotorDTO.setModelo(tipomotor.getModelo());
            tipomotorDTO.setTbo(tipomotor.getTbo());
            tipomotorDTOs.add(tipomotorDTO);
        }
        return ResponseEntity.ok(tipomotorDTOs);
        }
}

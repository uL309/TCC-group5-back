package puc.airtrack.airtrack.tipoMotor;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;


@RestController
@Tag(name = "Tipo de Motor", description = "Consulta de tipos/modelos de motores disponíveis com TBO (Time Between Overhaul)")
@SecurityRequirement(name = "bearerAuth")
public class TipoMotorController {
    
    @Autowired
    private TipoMotorRepository tipomotorRepository;


    @Operation(
        summary = "Listar todos os tipos de motor",
        description = "Retorna todos os tipos de motores cadastrados com marca, modelo e TBO."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
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

    @Operation(
        summary = "Buscar tipos de motor por marca",
        description = "Retorna todos os tipos de motores de uma marca específica (ex: Pratt & Whitney, Honeywell, Turbomeca)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
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

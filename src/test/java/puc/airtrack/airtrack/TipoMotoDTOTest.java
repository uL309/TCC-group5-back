package puc.airtrack.airtrack;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import puc.airtrack.airtrack.tipoMotor.TipoMotorDTO;

public class TipoMotoDTOTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void gettersAndSetters_work() {
        TipoMotorDTO dto = new TipoMotorDTO();
        dto.setId(10);
        dto.setMarca("MarcaX");
        dto.setModelo("ModelY");
        dto.setTbo(1500);

        assertEquals(10, dto.getId());
        assertEquals("MarcaX", dto.getMarca());
        assertEquals("ModelY", dto.getModelo());
        assertEquals(1500, dto.getTbo());
    }

    @Test
    void jsonSerialization_includesAnnotatedNames() throws Exception {
        TipoMotorDTO dto = new TipoMotorDTO();
        dto.setId(5);
        dto.setMarca("M");
        dto.setModelo("Mod");
        dto.setTbo(1200);

        String json = mapper.writeValueAsString(dto);
        assertTrue(json.contains("\"id\""));
        assertTrue(json.contains("\"marca\""));
        assertTrue(json.contains("\"modelo\""));
        assertTrue(json.contains("\"tbo\""));
        // basic value checks
        assertTrue(json.contains("\"5\"") || json.contains("5"));
        assertTrue(json.contains("1200"));
    }

    @Test
    void jsonDeserialization_mapsProperties() throws Exception {
        String json = "{\"id\":7,\"marca\":\"AA\",\"modelo\":\"BB\",\"tbo\":900}";
        TipoMotorDTO dto = mapper.readValue(json, TipoMotorDTO.class);

        assertEquals(7, dto.getId());
        assertEquals("AA", dto.getMarca());
        assertEquals("BB", dto.getModelo());
        assertEquals(900, dto.getTbo());
    }
}

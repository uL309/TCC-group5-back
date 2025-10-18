package puc.airtrack.airtrack.DTOs;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import puc.airtrack.airtrack.tipoMotor.TipoMotor;

public class TipoMotorTeste {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void gettersAndSetters_work() {
        TipoMotor t = new TipoMotor();
        t.setId(42);
        t.setMarca("MarcaX");
        t.setModelo("ModelY");
        t.setTbo(1500);

        assertEquals(42, t.getId());
        assertEquals("MarcaX", t.getMarca());
        assertEquals("ModelY", t.getModelo());
        assertEquals(1500, t.getTbo());
    }

    @Test
    void jsonSerialization_and_deserialization() throws Exception {
        TipoMotor t = new TipoMotor();
        t.setId(7);
        t.setMarca("M");
        t.setModelo("Mod");
        t.setTbo(900);

        String json = mapper.writeValueAsString(t);
        assertTrue(json.contains("\"id\""));
        assertTrue(json.contains("\"marca\""));
        assertTrue(json.contains("\"modelo\""));
        assertTrue(json.contains("\"tbo\""));

        TipoMotor read = mapper.readValue(json, TipoMotor.class);
        assertEquals(7, read.getId());
        assertEquals("M", read.getMarca());
        assertEquals("Mod", read.getModelo());
        assertEquals(900, read.getTbo());
    }
}
package puc.airtrack.airtrack.Engenheiro;


import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;



@Controller
public class EngenheiroController {
    
    @PostMapping("/cre")
    public String CreateEngenheiro(@RequestBody EngenheiroDTO entity) {
        //TODO: process POST request
        
        return ResponseEntity.ok().body("Engenheiro created successfully").toString();
    }
    
    @PostMapping("/upe")
    public String UpdateEngenheiro(@RequestBody EngenheiroDTO entity) {
        //TODO: process POST request

        return ResponseEntity.ok().body("Engenheiro updated successfully").toString();
    }

    @GetMapping("/ge")
    public String getEngenheiro(@RequestParam String param) {
        //TODO: process GET request

        return ResponseEntity.ok().body("Engenheiro found successfully").toString();
    }
    
    @PostMapping("/de")
    public String deleteEngenheiro(@RequestParam String param) {
        //apenas trocar o active da base

        return ResponseEntity.ok().body("Engenheiro deleted successfully").toString();
    }
}

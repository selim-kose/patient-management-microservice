package controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.selimkose.patientservice.dto.PatientResponseDTO;
import se.selimkose.patientservice.service.PatientService;

import java.util.List;

@RestController
@RequestMapping("/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

     @GetMapping
     public ResponseEntity<List<PatientResponseDTO>> getAllPatients() {
         return ResponseEntity.ok().body(patientService.getAllPatients());
     }
}

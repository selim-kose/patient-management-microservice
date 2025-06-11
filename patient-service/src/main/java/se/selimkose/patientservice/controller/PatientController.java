package se.selimkose.patientservice.controller;


import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import se.selimkose.patientservice.dto.PatientRequestDTO;
import se.selimkose.patientservice.dto.PatientResponseDTO;
import se.selimkose.patientservice.dto.validators.CreatePatientValidationGroup;
import se.selimkose.patientservice.service.PatientService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

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

    @PostMapping
    public ResponseEntity<PatientResponseDTO> createPatient(@Validated({Default.class, CreatePatientValidationGroup.class}) @RequestBody PatientRequestDTO patientRequestDTO) {
        PatientResponseDTO createdPatientResponseDTO = patientService.createPatient(patientRequestDTO);

        return ResponseEntity.ok().body(createdPatientResponseDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientResponseDTO> updatePatient(
            @PathVariable("id") UUID id,
            @Validated({Default.class}) @RequestBody PatientRequestDTO patientRequestDTO) {

        PatientResponseDTO updatedPatientResponseDTO = patientService.updatePatient(id, patientRequestDTO);

        return ResponseEntity.ok().body(updatedPatientResponseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable("id") UUID id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }
}

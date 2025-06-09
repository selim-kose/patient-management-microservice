package se.selimkose.patientservice.service;

import org.springframework.stereotype.Service;
import se.selimkose.patientservice.model.Patient;
import se.selimkose.patientservice.repository.PatientRepository;

import java.util.List;

@Service
public class PatientService {
    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }


}

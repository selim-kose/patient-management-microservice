package se.selimkose.patientservice.service;

import org.springframework.stereotype.Service;
import se.selimkose.patientservice.dto.PatientRequestDTO;
import se.selimkose.patientservice.dto.PatientResponseDTO;
import se.selimkose.patientservice.exception.EmailAlreadyExistsException;
import se.selimkose.patientservice.mapper.PatientMapper;
import se.selimkose.patientservice.model.Patient;
import se.selimkose.patientservice.repository.PatientRepository;

import java.util.List;

@Service
public class PatientService {
    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public List<PatientResponseDTO> getAllPatients() {
        List<Patient> patients = patientRepository.findAll();

        return patients.stream()
                .map(PatientMapper::toPatientResponseDTO)
                .toList();
    }

    public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO) {
        if (patientRepository.existsByEmail(patientRequestDTO.getEmail())) {
            throw new EmailAlreadyExistsException(
                    "A patient with this email already exists " + patientRequestDTO.getEmail());
        }

        Patient savedPatient = patientRepository.save(PatientMapper.toPatient(patientRequestDTO));

        return PatientMapper.toPatientResponseDTO(savedPatient);
    }
}

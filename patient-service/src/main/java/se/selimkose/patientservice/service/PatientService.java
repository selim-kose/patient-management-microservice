package se.selimkose.patientservice.service;

import org.springframework.stereotype.Service;
import se.selimkose.patientservice.dto.PatientRequestDTO;
import se.selimkose.patientservice.dto.PatientResponseDTO;
import se.selimkose.patientservice.exception.EmailAlreadyExistsException;
import se.selimkose.patientservice.exception.PatientNotFoundException;
import se.selimkose.patientservice.mapper.PatientMapper;
import se.selimkose.patientservice.model.Patient;
import se.selimkose.patientservice.repository.PatientRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

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
        // Check if a patient with the given email already exists
        if (patientRepository.existsByEmail(patientRequestDTO.getEmail())) {
            throw new EmailAlreadyExistsException(
                    "A patient with this email already exists " + patientRequestDTO.getEmail());
        }

        Patient savedPatient = patientRepository.save(PatientMapper.toPatient(patientRequestDTO));

        return PatientMapper.toPatientResponseDTO(savedPatient);
    }

    public PatientResponseDTO updatePatient(UUID id, PatientRequestDTO patientRequestDTO) {

        Patient existingPatient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found with ID: " + id));

        // Check if the email already exists for another patient
        if (patientRepository.existsByEmailAndIdNot(patientRequestDTO.getEmail(), id)) {
            throw new EmailAlreadyExistsException(
                    "A patient with this email already exists " + patientRequestDTO.getEmail());
        }
        // Update the existing patient details
        existingPatient.setName(patientRequestDTO.getName());
        existingPatient.setEmail(patientRequestDTO.getEmail());
        existingPatient.setAddress(patientRequestDTO.getAddress());
        existingPatient.setDateOfBirth(LocalDate.parse(patientRequestDTO.getDateOfBirth()));

        // Save the updated patient
        Patient updatedPatient = patientRepository.save(existingPatient);

        return PatientMapper.toPatientResponseDTO(updatedPatient);
    }
}

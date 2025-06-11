package se.selimkose.patientservice.exception;

public class PatientNotFoundException extends RuntimeException {

    public PatientNotFoundException(String patientId) {
        super("Patient with ID " + patientId + " not found.");
    }

}

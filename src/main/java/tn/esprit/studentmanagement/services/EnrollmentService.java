package tn.esprit.studentmanagement.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.studentmanagement.repositories.EnrollmentRepository;
import tn.esprit.studentmanagement.entities.Enrollment;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class EnrollmentService implements IEnrollment {
    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Override
    public List<Enrollment> getAllEnrollments() {
        log.info("Fetching all enrollments");
        List<Enrollment> list = enrollmentRepository.findAll();
        log.debug("Fetched {} enrollments", list.size());
        return list;
    }

    @Override
    public Enrollment getEnrollmentById(Long idEnrollment) {
        log.info("Fetching enrollment by id={}", idEnrollment);
        Optional<Enrollment> opt = enrollmentRepository.findById(idEnrollment);
        if (opt.isPresent()) {
            log.debug("Found enrollment: {}", opt.get());
        } else {
            log.warn("No enrollment found with id={}", idEnrollment);
        }
        return opt.orElse(null);
    }

    @Override
    public Enrollment saveEnrollment(Enrollment enrollment) {
        log.info("Saving enrollment: {}", enrollment);
        try {
            Enrollment saved = enrollmentRepository.save(enrollment);
            log.info("Enrollment saved with id={}", saved.getIdEnrollment());
            return saved;
        } catch (Exception e) {
            log.error("Failed to save enrollment: {}", enrollment, e);
            throw e;
        }
    }

    @Override
    public void deleteEnrollment(Long idEnrollment) {
        log.info("Deleting enrollment id={}", idEnrollment);
        try {
            enrollmentRepository.deleteById(idEnrollment);
            log.info("Deleted enrollment id={}", idEnrollment);
        } catch (Exception e) {
            log.error("Failed to delete enrollment id={}", idEnrollment, e);
            throw e;
        }
    }
}

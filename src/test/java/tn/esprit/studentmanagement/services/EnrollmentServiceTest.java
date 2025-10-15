package tn.esprit.studentmanagement.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tn.esprit.studentmanagement.entities.Enrollment;
import tn.esprit.studentmanagement.repositories.EnrollmentRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private EnrollmentService enrollmentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void saveEnrollment_success() {
        Enrollment enr = new Enrollment();
        Enrollment saved = new Enrollment();
        saved.setIdEnrollment(5L);
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(saved);

        Enrollment result = enrollmentService.saveEnrollment(enr);

        assertNotNull(result);
        assertEquals(5L, result.getIdEnrollment());
        verify(enrollmentRepository, times(1)).save(enr);
    }

    @Test
    void getEnrollmentById_notFound_returnsNull() {
        when(enrollmentRepository.findById(42L)).thenReturn(Optional.empty());

        Enrollment result = enrollmentService.getEnrollmentById(42L);

        assertNull(result);
        verify(enrollmentRepository, times(1)).findById(42L);
    }

    @Test
    void getAllEnrollments_returnsList() {
        when(enrollmentRepository.findAll()).thenReturn(Arrays.asList(new Enrollment()));

        List<Enrollment> results = enrollmentService.getAllEnrollments();

        assertNotNull(results);
        assertEquals(1, results.size());
        verify(enrollmentRepository, times(1)).findAll();
    }
}

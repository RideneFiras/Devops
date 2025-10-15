package tn.esprit.studentmanagement.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tn.esprit.studentmanagement.entities.Student;
import tn.esprit.studentmanagement.repositories.StudentRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentService studentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void saveStudent_success() {
        Student student = new Student();
        student.setFirstName("John");
        student.setLastName("Doe");
        student.setEmail("john.doe@example.com");
        student.setDateOfBirth(LocalDate.of(2000,1,1));

        Student saved = new Student();
        saved.setIdStudent(1L);
        when(studentRepository.save(any(Student.class))).thenReturn(saved);

        Student result = studentService.saveStudent(student);

        assertNotNull(result);
        assertEquals(1L, result.getIdStudent());
        verify(studentRepository, times(1)).save(student);
    }

    @Test
    void getStudentById_notFound_returnsNull() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        Student result = studentService.getStudentById(99L);

        assertNull(result);
        verify(studentRepository, times(1)).findById(99L);
    }

    @Test
    void getAllStudents_returnsList() {
        when(studentRepository.findAll()).thenReturn(Arrays.asList(new Student(), new Student()));

        List<Student> results = studentService.getAllStudents();

        assertNotNull(results);
        assertEquals(2, results.size());
        verify(studentRepository, times(1)).findAll();
    }
}

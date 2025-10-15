package tn.esprit.studentmanagement.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tn.esprit.studentmanagement.entities.Department;
import tn.esprit.studentmanagement.repositories.DepartmentRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private DepartmentService departmentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void saveDepartment_success() {
        Department dept = new Department();
        dept.setName("IT");
        Department saved = new Department();
        saved.setIdDepartment(10L);
        when(departmentRepository.save(any(Department.class))).thenReturn(saved);

        Department result = departmentService.saveDepartment(dept);

        assertNotNull(result);
        assertEquals(10L, result.getIdDepartment());
        verify(departmentRepository, times(1)).save(dept);
    }

    @Test
    void getDepartmentById_notFound_returnsNull() {
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        Department result = departmentService.getDepartmentById(99L);

        assertNull(result);
        verify(departmentRepository, times(1)).findById(99L);
    }

    @Test
    void getAllDepartments_returnsList() {
        when(departmentRepository.findAll()).thenReturn(Arrays.asList(new Department(), new Department(), new Department()));

        List<Department> results = departmentService.getAllDepartments();

        assertNotNull(results);
        assertEquals(3, results.size());
        verify(departmentRepository, times(1)).findAll();
    }
}

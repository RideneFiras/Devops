package tn.esprit.studentmanagement.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.studentmanagement.entities.Department;
import tn.esprit.studentmanagement.repositories.DepartmentRepository;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class DepartmentService implements IDepartmentService {
    @Autowired
    DepartmentRepository departmentRepository;

    @Override
    public List<Department> getAllDepartments() {
        log.info("Fetching all departments");
        List<Department> departments = departmentRepository.findAll();
        log.debug("Fetched {} departments", departments.size());
        return departments;
    }

    @Override
    public Department getDepartmentById(Long idDepartment) {
        log.info("Fetching department by id={}", idDepartment);
        Optional<Department> opt = departmentRepository.findById(idDepartment);
        if (opt.isPresent()) {
            log.debug("Found department: {}", opt.get());
        } else {
            log.warn("No department found with id={}", idDepartment);
        }
        return opt.orElse(null);
    }

    @Override
    public Department saveDepartment(Department department) {
        log.info("Saving department: {}", department);
        try {
            Department saved = departmentRepository.save(department);
            log.info("Department saved with id={}", saved.getIdDepartment());
            return saved;
        } catch (Exception e) {
            log.error("Failed to save department: {}", department, e);
            throw e;
        }
    }

    @Override
    public void deleteDepartment(Long idDepartment) {
        log.info("Deleting department id={}", idDepartment);
        try {
            departmentRepository.deleteById(idDepartment);
            log.info("Deleted department id={}", idDepartment);
        } catch (Exception e) {
            log.error("Failed to delete department id={}", idDepartment, e);
            throw e;
        }
    }
}

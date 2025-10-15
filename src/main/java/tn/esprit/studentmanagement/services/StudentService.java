package tn.esprit.studentmanagement.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.studentmanagement.entities.Student;
import tn.esprit.studentmanagement.repositories.StudentRepository;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class StudentService implements IStudentService {
    @Autowired
    private StudentRepository studentRepository;

    public List<Student> getAllStudents() {
        log.info("Fetching all students");
        List<Student> students = studentRepository.findAll();
        log.debug("Fetched {} students", students.size());
        return students;
    }

    public Student getStudentById(Long id) {
        log.info("Fetching student by id={}", id);
        Optional<Student> opt = studentRepository.findById(id);
        if (opt.isPresent()) {
            log.debug("Found student: {}", opt.get());
        } else {
            log.warn("No student found with id={}", id);
        }
        return opt.orElse(null);
    }

    public Student saveStudent(Student student) {
        log.info("Saving student: {}", student);
        try {
            Student saved = studentRepository.save(student);
            log.info("Student saved with id={}", saved.getIdStudent());
            return saved;
        } catch (Exception e) {
            log.error("Failed to save student: {}", student, e);
            throw e;
        }
    }

    public void deleteStudent(Long id) {
        log.info("Deleting student id={}", id);
        try {
            studentRepository.deleteById(id);
            log.info("Deleted student id={}", id);
        } catch (Exception e) {
            log.error("Failed to delete student id={}", id, e);
            throw e;
        }
    }

}

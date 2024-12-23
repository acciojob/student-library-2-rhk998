package com.driver.services;

import com.driver.models.Card;
import com.driver.models.CardStatus;
import com.driver.models.Student;
import com.driver.repositories.CardRepository;
import com.driver.repositories.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class StudentService {


    @Autowired
    CardService cardService4;

    @Autowired
    StudentRepository studentRepository4;
    @Autowired
    CardRepository cardRepository3;


    public Student getDetailsByEmail(String email){
        Student student = studentRepository4.findByEmailId(email);
        return student;
    }

    public Student getDetailsById(int id){
        Student student = studentRepository4.findById(id).get();
        return student;
    }

    public void createStudent(Student student){

        Card card = cardService4.createAndReturn(student);

    }

    public void updateStudent(Student student){
        studentRepository4.updateStudentDetails(student);

    }

    public void deleteStudent(int id) {
        cardService4.deactivateCard(id);
        studentRepository4.deleteCustom(id);
    }

}

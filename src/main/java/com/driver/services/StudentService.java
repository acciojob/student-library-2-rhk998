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
        Student student = studentRepository4.findById(id).orElse(new Student());
        return student;
    }

    public void createStudent(Student student){
        studentRepository4.save(student);
    }

    public void updateStudent(Student student){
        int id = student.getId();

        Optional<Student> optionalRecord = studentRepository4.findById(id);
        if(optionalRecord.isPresent()){
            Student existingRecord = optionalRecord.get();
            existingRecord.setEmailId(student.getEmailId());
            existingRecord.setName(student.getName());
            existingRecord.setAge(student.getAge());
            existingRecord.setCountry(student.getCountry());
            studentRepository4.save(existingRecord);
        }

    }

    public void deleteStudent(int id){
        //Delete student and deactivate corresponding card
        Optional<Student> studentRec = studentRepository4.findById(id);
        if(studentRec.isPresent()){
            Student student = studentRec.get();
            Card stdcard = student.getCard();
            if(stdcard!=null){
                stdcard.setCardStatus(CardStatus.DEACTIVATED);
                cardRepository3.save(stdcard);
            }
        }

    }
}

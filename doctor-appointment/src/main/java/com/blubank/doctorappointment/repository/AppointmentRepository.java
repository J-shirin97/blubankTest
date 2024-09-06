package com.blubank.doctorappointment.repository;

import com.blubank.doctorappointment.entity.Appointment;
import com.blubank.doctorappointment.entity.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {


    List<Appointment> findByStatus(AppointmentStatus status);

    List<Appointment> findByPatientPhoneNumber(String phoneNumber);



}

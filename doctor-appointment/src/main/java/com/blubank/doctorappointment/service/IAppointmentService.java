package com.blubank.doctorappointment.service;

import com.blubank.doctorappointment.entity.Appointment;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface IAppointmentService {

    List<Appointment> getAppointmentsByPhoneNumber(String phoneNumber);

    List<Appointment> createAppointments(LocalDateTime startTime, LocalDateTime endTime);

    void bookAppointment(Long appointmentId, String patientName, String phoneNumber);

    void deleteOpenAppointment(Long appointmentId);

    List<Appointment> getOpenAppointments ();

    void takeAppointment(Long appointmentId, String patientName, String patientPhoneNumber);
}

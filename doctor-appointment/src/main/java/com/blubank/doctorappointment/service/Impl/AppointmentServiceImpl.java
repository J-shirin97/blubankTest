package com.blubank.doctorappointment.service.Impl;

import com.blubank.doctorappointment.Exceptions.ExceptionHandling;
import com.blubank.doctorappointment.entity.Appointment;
import com.blubank.doctorappointment.entity.enums.AppointmentStatus;
import com.blubank.doctorappointment.repository.AppointmentRepository;
import com.blubank.doctorappointment.service.IAppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AppointmentServiceImpl implements IAppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    public List<Appointment> getOpenAppointments() {
        return appointmentRepository.findByStatus(AppointmentStatus.OPEN);
    }

    public List<Appointment> getAppointmentsByPhoneNumber(String phoneNumber) {
        return appointmentRepository.findByPatientPhoneNumber(phoneNumber);
    }

    public List<Appointment> createAppointments(LocalDateTime startTime, LocalDateTime endTime) {
        if (endTime.isBefore(startTime) || Duration.between(startTime, endTime).toMinutes() < 30) {
            throw new IllegalArgumentException("Invalid appointment time");
        }

        List<Appointment> appointments = new ArrayList<>();
        while (startTime.plusMinutes(30).isBefore(endTime) || startTime.plusMinutes(30).isEqual(endTime)) {
            Appointment appointment = new Appointment();
            appointment.setStartTime(startTime);
            appointment.setEndTime(startTime.plusMinutes(30));
            appointment.setStatus(AppointmentStatus.OPEN);
            appointments.add(appointment);
            startTime = startTime.plusMinutes(30);
        }
        return appointmentRepository.saveAll(appointments);
    }

    public void bookAppointment(Long appointmentId, String patientName, String phoneNumber) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ExceptionHandling.AppointmentNotFoundException("Appointment not found"));

        if (appointment.getStatus() == AppointmentStatus.TAKEN) {
            throw new ExceptionHandling.AppointmentAlreadyTakenException("This appointment is already taken.");
        }

        appointment.setStatus(AppointmentStatus.TAKEN);
        appointment.setPatientName(patientName);
        appointment.setPatientPhoneNumber(phoneNumber);

        appointmentRepository.save(appointment);
    }

    public void deleteOpenAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ExceptionHandling.AppointmentNotFoundException("Appointment not found"));

        if (appointment.getStatus() == AppointmentStatus.TAKEN) {
            throw new ExceptionHandling.AppointmentTakenException("Cannot delete a taken appointment.");
        }

        appointmentRepository.delete(appointment);
    }
}

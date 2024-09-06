package com.blubank.doctorappointment.service.Impl;

import com.blubank.doctorappointment.exceptions.ExceptionHandling;
import com.blubank.doctorappointment.entity.Appointment;
import com.blubank.doctorappointment.entity.enums.AppointmentStatus;
import com.blubank.doctorappointment.repository.AppointmentRepository;
import com.blubank.doctorappointment.service.IAppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AppointmentServiceImpl implements IAppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    public AppointmentServiceImpl(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }
    @Override
    public List<Appointment> getOpenAppointments() {
        List<Appointment> openAppointments = appointmentRepository.findByStatus(AppointmentStatus.OPEN);
        List<Appointment> takenAppointments = appointmentRepository.findByStatus(AppointmentStatus.TAKEN);

        // Combine both open and taken appointments
        List<Appointment> allAppointments = new ArrayList<>();
        allAppointments.addAll(openAppointments);
        allAppointments.addAll(takenAppointments);

        return allAppointments;
    }

    @Override
    public List<Appointment> getAppointmentsByPhoneNumber(String phoneNumber) {
        return appointmentRepository.findByPatientPhoneNumber(phoneNumber);
    }

    @Override
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

    @Override
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

    @Override
    public void deleteOpenAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ExceptionHandling.AppointmentNotFoundException("Appointment not found"));

        if (appointment.getStatus() == AppointmentStatus.TAKEN) {
            throw new ExceptionHandling.AppointmentTakenException("Cannot delete a taken appointment.");
        }

        appointmentRepository.delete(appointment);
    }

    @Transactional
    @Override
    public void takeAppointment(Long appointmentId, String patientName, String patientPhoneNumber) {
        // Fetch the appointment with a lock for concurrency management
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found."));

        // 1. Check if the appointment is already taken or deleted
        if (appointment.getStatus() != AppointmentStatus.OPEN) {
            throw new IllegalArgumentException("Appointment is already taken or deleted.");
        }

        // 2. Assign patient details and mark the appointment as TAKEN
        appointment.setPatientName(patientName);
        appointment.setPatientPhoneNumber(patientPhoneNumber);
        appointment.setStatus(AppointmentStatus.TAKEN);

        // Save the updated appointment
        appointmentRepository.save(appointment);
    }



}

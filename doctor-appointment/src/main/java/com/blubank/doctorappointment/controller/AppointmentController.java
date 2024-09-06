package com.blubank.doctorappointment.controller;

import com.blubank.doctorappointment.exceptions.ExceptionHandling;
import com.blubank.doctorappointment.entity.Appointment;
import com.blubank.doctorappointment.service.Impl.AppointmentServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentServiceImpl appointmentService;

    @PostMapping("/create")
    public ResponseEntity<?> createAppointments(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        try {
            appointmentService.createAppointments(startTime, endTime);
            return ResponseEntity.ok("Appointments created successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/open")
    public ResponseEntity<List<Appointment>> getOpenAppointments() {
        List<Appointment> openAppointments = appointmentService.getOpenAppointments();
        return ResponseEntity.ok(openAppointments);
    }

    @PostMapping("/book/{id}")
    public ResponseEntity<?> bookAppointment(@PathVariable Long id,
                                             @RequestParam String name,
                                             @RequestParam String phoneNumber) {
        try {
            appointmentService.bookAppointment(id, name, phoneNumber);
            return ResponseEntity.ok("Appointment booked successfully.");
        } catch (ExceptionHandling.AppointmentAlreadyTakenException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteOpenAppointment(@PathVariable Long id) {
        try {
            appointmentService.deleteOpenAppointment(id);
            return ResponseEntity.ok("Appointment deleted successfully.");
        } catch (ExceptionHandling.AppointmentTakenException e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(e.getMessage());
        }
    }

    @PostMapping("/takeAppointments")
    public ResponseEntity<?> takeAppointment(
            @RequestParam Long appointmentId,
            @RequestParam String patientName,
            @RequestParam String patientPhoneNumber) {

        // 1. Validate input fields: Check if either phone number or name is missing
        if (patientName == null || patientName.isEmpty() ||
                patientPhoneNumber == null || patientPhoneNumber.isEmpty()) {
            return ResponseEntity.badRequest().body("Name and phone number must be provided.");
        }
        try {
            // 2. Attempt to take the appointment
            appointmentService.takeAppointment(appointmentId, patientName, patientPhoneNumber);
            return ResponseEntity.ok("Appointment taken successfully.");
        } catch (IllegalArgumentException e) {
            // 3. Return a bad request if the appointment is already taken or doesn't exist
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ExceptionHandling.AppointmentAlreadyTakenException e) {
            // 4. Handle concurrency errors
            return ResponseEntity.status(HttpStatus.CONFLICT).body("The appointment is being taken or deleted.");
        }
    }
    @GetMapping("/my-appointments")
    public ResponseEntity<List<Appointment>> getMyAppointments(
            @RequestParam String phoneNumber) {
        List<Appointment> appointments = appointmentService.getAppointmentsByPhoneNumber(phoneNumber);
        return ResponseEntity.ok(appointments);
    }
}


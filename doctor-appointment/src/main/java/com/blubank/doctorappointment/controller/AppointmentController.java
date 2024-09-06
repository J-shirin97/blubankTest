package com.blubank.doctorappointment.controller;

import com.blubank.doctorappointment.Exceptions.ExceptionHandling;
import com.blubank.doctorappointment.entity.Appointment;
import com.blubank.doctorappointment.service.Impl.AppointmentServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
}


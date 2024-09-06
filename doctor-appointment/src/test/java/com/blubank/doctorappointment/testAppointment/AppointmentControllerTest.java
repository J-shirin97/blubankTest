package com.blubank.doctorappointment.testAppointment;

import com.blubank.doctorappointment.entity.Appointment;
import com.blubank.doctorappointment.entity.enums.AppointmentStatus;
import com.blubank.doctorappointment.service.Impl.AppointmentServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppointmentServiceImpl appointmentService;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");


    //test for create api
    @Test
    public void shouldReturnErrorWhenEndTimeIsBeforeStartTime() throws Exception {
        // Start time: now, End time: 1 hour before start time
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.minusHours(1);

        mockMvc.perform(post("/appointments/create")
                        .param("startTime", startTime.format(formatter))
                        .param("endTime", endTime.format(formatter)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid appointment time"));
    }

    @Test
    public void shouldReturnErrorWhenTimeDifferenceIsLessThan30Minutes() throws Exception {
        // Start time: now, End time: 15 minutes after start time
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusMinutes(15);

        mockMvc.perform(post("/appointments/create")
                        .param("startTime", startTime.format(formatter))
                        .param("endTime", endTime.format(formatter)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid appointment time"));
    }


    //test for open api
    @Test
    public void shouldReturnEmptyListWhenNoAppointmentsAreSet() throws Exception {
        // Mock the service to return an empty list
        when(appointmentService.getOpenAppointments()).thenReturn(Collections.emptyList());

        // Perform the GET request and expect an empty list in the response
        mockMvc.perform(get("/appointments/open"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    public void shouldReturnListWithPatientNameAndPhoneNumberWhenAppointmentsExist() throws Exception {
        // Mock a list with a booked appointment (including patient details)
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setPatientName("John Doe");
        appointment.setPatientPhoneNumber("1234567890");
        appointment.setStatus(AppointmentStatus.TAKEN);

        when(appointmentService.getOpenAppointments()).thenReturn(List.of(appointment));

        // Perform the GET request and expect the list with patient details
        mockMvc.perform(get("/appointments/open"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].patientName").value("John Doe"))
                .andExpect(jsonPath("$[0].patientPhoneNumber").value("1234567890"));
    }


    @Test
    public void whenAppointmentAlreadyTakenOrDeleted_thenReturnBadRequest() throws Exception {
        // Mock the service to throw IllegalArgumentException when trying to take a taken/deleted appointment
        doThrow(new IllegalArgumentException("Appointment is already taken or deleted."))
                .when(appointmentService).takeAppointment(1L, "John Doe", "1234567890");

        mockMvc.perform(post("/appointments/take")
                        .param("appointmentId", "1")
                        .param("patientName", "John Doe")
                        .param("patientPhoneNumber", "1234567890"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Appointment is already taken or deleted."));
    }
    @Test
    public void whenConcurrencyIssueOccurs_thenReturnConflict() throws Exception {
        // Mock the service to throw ConcurrencyException
        doThrow(new IllegalArgumentException("The appointment is being taken or deleted."))
                .when(appointmentService).takeAppointment(1L, "John Doe", "1234567890");

        mockMvc.perform(post("/appointments/take")
                        .param("appointmentId", "1")
                        .param("patientName", "John Doe")
                        .param("patientPhoneNumber", "1234567890"))
                .andExpect(status().isConflict())
                .andExpect(content().string("The appointment is being taken or deleted."));
    }

    @Test
    public void whenNameOrPhoneNumberNotProvided_thenReturnBadRequest() throws Exception {
        mockMvc.perform(post("/appointments/take")
                        .param("appointmentId", "1")
                        .param("patientName", "")
                        .param("patientPhoneNumber", "1234567890"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Name and phone number must be provided."));
    }
    @Test
    public void whenNoAppointmentsForPhoneNumber_thenReturnEmptyList() throws Exception {
        // Mock the service to return an empty list
        when(appointmentService.getAppointmentsByPhoneNumber("1234567890"))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/appointments/my-appointments")
                        .param("phoneNumber", "1234567890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
    @Test
    public void whenMultipleAppointmentsForPhoneNumber_thenReturnAllAppointments() throws Exception {
        // Mock a list of appointments
        Appointment appointment1 = new Appointment();
        appointment1.setId(1L);
        appointment1.setPatientPhoneNumber("1234567890");
        appointment1.setPatientName("John Doe");

        Appointment appointment2 = new Appointment();
        appointment2.setId(2L);
        appointment2.setPatientPhoneNumber("1234567890");
        appointment2.setPatientName("Jane Doe");

        List<Appointment> appointments = List.of(appointment1, appointment2);

        // Mock the service to return the list of appointments
        when(appointmentService.getAppointmentsByPhoneNumber("1234567890"))
                .thenReturn(appointments);

        mockMvc.perform(get("/appointments/my-appointments")
                        .param("phoneNumber", "1234567890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].patientName").value("John Doe"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].patientName").value("Jane Doe"));
    }


}


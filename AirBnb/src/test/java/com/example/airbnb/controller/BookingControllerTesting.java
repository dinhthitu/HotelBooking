package com.example.airbnb.controller;

import com.example.airbnb.dto.BookingDto;
import com.example.airbnb.dto.GuestDto;
import com.example.airbnb.dto.request.BookingRequest;
import com.example.airbnb.enums.BookingStatus;
import com.example.airbnb.enums.ErrorCode;
import com.example.airbnb.enums.Gender;
import com.example.airbnb.exception.AppException;
import com.example.airbnb.service.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.time.LocalDate;

import java.util.HashSet;
import java.util.List;


@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username="dinhthitu@gmail.com", roles={"GUEST"})
public class BookingControllerTesting {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingService bookingService;
    private GuestDto guestDto;
    private BookingDto bookingDto;
    private BookingRequest bookingRequest;
    private List<GuestDto> guestList;
    private LocalDate checkInDate, checkOutDate;
    private final static ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());




    @BeforeEach
    public void initData(){
        GuestDto guest1 = GuestDto.builder()
                .name("A")
                .gender(Gender.MALE)
                .age(20).build();
        GuestDto guest2 = GuestDto.builder()
                .name("B")
                .gender(Gender.FEMALE)
                .age(25).build();
        GuestDto guest3 = GuestDto.builder()
                .name("C")
                .gender(Gender.UNISEX)
                .age(29).build();
        guestList = List.of(guest1, guest2, guest3);

    checkInDate = LocalDate.of(2025, 9, 22);
    checkOutDate = LocalDate.of(2025, 9, 25);

    bookingRequest = BookingRequest.builder()
                .checkInDate(checkInDate)
                .checkOutDate(checkOutDate)
                .hotelId(1234L)
                .roomCount(3)
                .roomId(22011L)
                .build();
        bookingDto = BookingDto.builder()
                .id(352L)
                .roomsCount(3)
                .checkInDate(checkInDate)
                .checkOutDate(checkOutDate)
                .bookingStatus(BookingStatus.RESERVED)
                .amount(BigDecimal.valueOf(200.000))
                .build();
    }

    @Test
    void initialise_booking_success() throws Exception{

        Mockito.when(bookingService.initialiseBooking(ArgumentMatchers.any()))
                .thenReturn(bookingDto);

        mockMvc.perform(MockMvcRequestBuilders
                .post("/bookings/initBooking")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(bookingRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("result.id")
                        .value(352))
                .andExpect(MockMvcResultMatchers.jsonPath("result.roomsCount")
                        .value(3))
                .andExpect(MockMvcResultMatchers.jsonPath("result.checkInDate")
                        .value(checkInDate.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("result.checkOutDate")
                        .value(checkOutDate.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("result.bookingStatus")
                        .value("RESERVED"))
                .andExpect(MockMvcResultMatchers.jsonPath("result.amount")
                        .value(200.000));
    }


    @Test
    void add_guest_success () throws Exception{

    bookingDto.setBookingStatus(BookingStatus.GUEST_ADDED);
        bookingDto.setGuests(new HashSet<>(guestList));

    Mockito.when(bookingService.addGuests(ArgumentMatchers.eq(352L), ArgumentMatchers.anyList()))
            .thenReturn(bookingDto);
    mockMvc.perform(MockMvcRequestBuilders
            .post("/bookings/352/addGuests")
            .content(objectMapper.writeValueAsString(guestList))
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("code").value(200))
            .andExpect(MockMvcResultMatchers.jsonPath("result.id").value(352))
            .andExpect(MockMvcResultMatchers.jsonPath("result.bookingStatus").value("GUEST_ADDED"))
            .andExpect(MockMvcResultMatchers.jsonPath("result.guests.length()").value(3));

    }

    @Test
    void add_guest_failed() throws Exception{
    Mockito.doThrow(new AppException(ErrorCode.BOOKING_NOT_FOUND)).when(bookingService).addGuests(ArgumentMatchers.eq(352L), ArgumentMatchers.anyList());
    mockMvc.perform(MockMvcRequestBuilders
            .post("/bookings/352/addGuests")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(guestList)))
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("code").value(400))
            .andExpect(MockMvcResultMatchers.jsonPath("message").value("BOOKING NOT AVAILABLE"));
    }

    @Test
    void cancel_booking_success() throws Exception{

       Mockito.doNothing().when(bookingService).cancelBooking(352L);
       mockMvc.perform(MockMvcRequestBuilders
               .post("/bookings/352/cancel"))
               .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(MockMvcResultMatchers.jsonPath("code").value(200));
    }

    @Test
    void cancel_booking_failed() throws Exception{


        Mockito.doThrow(new AppException(ErrorCode.BOOKING_NOT_FOUND)).when(bookingService).cancelBooking(4L);
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/bookings/4/cancel"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(400))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("BOOKING NOT AVAILABLE"));
    }


    @Test
    void init_payment_success() throws Exception{
        Mockito.when(bookingService.initPayment(302L))
                .thenReturn("http:/checkout/stripeudhfefygf");
        mockMvc.perform(MockMvcRequestBuilders
                .post("/bookings/302/payment"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("result").value("http:/checkout/stripeudhfefygf"));
    }

    @Test
    void init_payment_expired() throws Exception{
        Mockito.doThrow(new IllegalStateException("Booking has already expired")).when(bookingService).initPayment(202L);
        mockMvc.perform(MockMvcRequestBuilders
                .post("/bookings/202/payment"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(404))
                .andExpect(MockMvcResultMatchers.jsonPath("result").value("Booking has already expired"));
    }

    @Test
    void init_payment_not_available() throws Exception{
        Mockito.doThrow(new AppException(ErrorCode.BOOKING_NOT_FOUND)).when(bookingService).initPayment(202L);
        mockMvc.perform(MockMvcRequestBuilders
                .post("/bookings/202/payment"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(400))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("BOOKING NOT AVAILABLE"));
    }



}

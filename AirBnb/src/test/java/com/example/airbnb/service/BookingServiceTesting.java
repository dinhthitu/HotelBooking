package com.example.airbnb.service;

import com.example.airbnb.dto.BookingDto;
import com.example.airbnb.dto.GuestDto;
import com.example.airbnb.dto.request.BookingRequest;
import com.example.airbnb.entities.*;
import com.example.airbnb.enums.BookingStatus;
import com.example.airbnb.enums.ErrorCode;
import com.example.airbnb.exception.AppException;
import com.example.airbnb.repository.*;
import com.example.airbnb.strategy.PricingService;
import com.example.airbnb.util.AppUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource("/application-test.properties")
public class BookingServiceTesting {

    @Autowired
    private BookingService bookingService;

    @MockitoBean private BookingRepository bookingRepository;
    @MockitoBean private InventoryRepository inventoryRepository;
    @MockitoBean private HotelRepository hotelRepository;
    @MockitoBean private RoomRepository roomRepository;
    @MockitoBean private GuestRepository guestRepository;
    @MockitoBean private ModelMapper modelMapper;
    @MockitoBean private PricingService pricingService;
    @MockitoBean private CheckoutService checkoutService;

    private BookingDto bookingDto;
    private BookingRequest bookingRequest;
    private GuestDto guestDto;
    private Hotel hotel;
    private User user;
    private Room room;
    private Booking booking;
    private MockedStatic<?> mockedStatic = mockStatic(AppUtils.class);
    @BeforeEach
    void initData() {
        bookingRequest = BookingRequest.builder()
                .roomId(1L)
                .roomCount(3)
                .checkOutDate(LocalDate.of(2025, 11, 11))
                .checkInDate(LocalDate.of(2025, 11, 7))
                .hotelId(1L)
                .build();

        user = User.builder()
                .id(1L)
                .email("admin123@gmail.com")
                .build();

        hotel = Hotel.builder()
                .id(1L)
                .owner(user)
                .build();

        room = Room.builder()
                .id(1L)
                .hotel(hotel)
                .build();

        booking = Booking.builder()
                .id(1L)
                .user(user)
                .createdAt(LocalDateTime.now())
                .hotel(hotel)
                .room(room)
                .guests(new HashSet<>())
                .build();

        guestDto = GuestDto.builder()
                .name("John")
                .build();

        mockedStatic.when(AppUtils::getCurrentUser).thenReturn(user);

    }

    // -------------------- initialiseBooking --------------------

    @Test
    void initialise_booking_success() {
        when(hotelRepository.findById(bookingRequest.getHotelId()))
                .thenReturn(Optional.of(hotel));
        when(roomRepository.findById(bookingRequest.getRoomId()))
                .thenReturn(Optional.of(room));

        List<Inventory> inventoryList = List.of(
                new Inventory(), new Inventory(), new Inventory(), new Inventory(), new Inventory()
        );

        when(inventoryRepository.findAndLockAvailableInventory(
                anyLong(), any(), any(), any()))
                .thenReturn(inventoryList);

        when(pricingService.calculateTotalPrice(inventoryList))
                .thenReturn(BigDecimal.valueOf(100));

        when(modelMapper.map(any(Booking.class), eq(BookingDto.class)))
                .thenReturn(BookingDto.builder()
                        .id(1L)
                        .amount(BigDecimal.valueOf(100))
                        .roomsCount(3)
                        .checkInDate(LocalDate.of(2025, 11, 7))
                        .checkOutDate(LocalDate.of(2025, 11, 11))
                        .build());


            bookingDto = bookingService.initialiseBooking(bookingRequest);

            assertThat(bookingDto).isNotNull();
            assertThat(bookingDto.getId()).isEqualTo(1L);
            assertThat(bookingDto.getAmount()).isEqualTo(BigDecimal.valueOf(100));

    }

    @Test
    void hotel_not_found() {
        when(hotelRepository.findById(bookingRequest.getHotelId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.initialiseBooking(bookingRequest))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.HOTEL_NOT_FOUND);
    }

    @Test
    void room_not_found() {
        when(hotelRepository.findById(bookingRequest.getHotelId()))
                .thenReturn(Optional.of(hotel));
        when(roomRepository.findById(bookingRequest.getRoomId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.initialiseBooking(bookingRequest))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ROOM_NOT_FOUND);
    }

    @Test
    void invalid_inventory() {
        when(hotelRepository.findById(bookingRequest.getHotelId())).thenReturn(Optional.of(hotel));
        when(roomRepository.findById(bookingRequest.getRoomId())).thenReturn(Optional.of(room));

        List<Inventory> inventoryList = List.of(new Inventory(), new Inventory());
        when(inventoryRepository.findAndLockAvailableInventory(anyLong(), any(), any(), any()))
                .thenReturn(inventoryList);

        assertThatThrownBy(() -> bookingService.initialiseBooking(bookingRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Room not available more");
    }

    // -------------------- addGuests --------------------

    @Test
    void add_guests_success() {
        booking.setBookingStatus(BookingStatus.RESERVED);

        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(modelMapper.map(any(GuestDto.class), eq(Guest.class)))
                .thenReturn(new Guest());
        when(modelMapper.map(any(Booking.class), eq(BookingDto.class)))
                .thenReturn(new BookingDto());
        when(guestRepository.save(any(Guest.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

            BookingDto result = bookingService.addGuests(booking.getId(), List.of(guestDto));

            assertThat(result).isNotNull();
            verify(bookingRepository, times(1)).save(any(Booking.class));
            verify(guestRepository, times(1)).save(any(Guest.class));

    }

    @Test
    void invalid_booking() {
        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.addGuests(booking.getId(), List.of(guestDto)))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.BOOKING_NOT_FOUND);
    }

    @Test
    void add_guest_booking_expired() {
        booking.setBookingStatus(BookingStatus.RESERVED);
        booking.setCreatedAt(LocalDateTime.now().minusMinutes(25));

        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

            assertThatThrownBy(() -> bookingService.addGuests(booking.getId(), List.of(guestDto)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Booking has already expired!");

    }

    // -------------------- initPayment --------------------

    @Test
    void init_payment_success(){
        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));
        when(checkoutService.getCheckoutSession(any(), anyString(), anyString()))
                .thenReturn("http://stripe.test/session");

            String url = bookingService.initPayment(booking.getId());

            assertThat(url).contains("http://stripe.test/session");
            verify(bookingRepository).save(any(Booking.class));

    }

    // -------------------- cancelBooking -----------------------

    @Test
    void cancel_booking_success(){
        booking.setBookingStatus(BookingStatus.GUEST_ADDED);
        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));
        when(inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(), booking.getCheckInDate(), booking.getCheckOutDate(), booking.getRoomsCount()))
                .thenReturn(List.of());
        doNothing().when(inventoryRepository).cancelBooking(booking.getRoom().getId(), booking.getCheckInDate(), booking.getCheckOutDate(), booking.getRoomsCount());

        bookingService.cancelBooking(booking.getId());
        verify(bookingRepository).save(booking);
        assertThat(booking.getBookingStatus()).isEqualTo(BookingStatus.CANCELLED);

    }

    //------------------------- getAllBookings -----------------------------

    @Test
    void get_all_bookings(){
        when(hotelRepository.findById(hotel.getId()))
                .thenReturn(Optional.of(hotel));
        when(bookingRepository.findByHotel(hotel))
                .thenReturn(List.of(booking));
        when(modelMapper.map(booking, BookingDto.class))
                .thenReturn(new BookingDto());

        List<BookingDto> bookingDtos = bookingService.getAllBookingByHotel(hotel.getId());

        assertThat(bookingDtos).hasSize(1);
    }

    @Test
    void unauthorized_user(){
        User user1 = User.builder()
                .id(2L)
                .email("user1@gmail.com")
                .build();
        when(hotelRepository.findById(hotel.getId()))
                .thenReturn(Optional.of(hotel));

        mockedStatic.when(AppUtils::getCurrentUser)
                .thenReturn(user1);

        assertThatThrownBy(() -> bookingService.getAllBookingByHotel(hotel.getId()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("you are not authorized");
    }
}


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
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;


import static com.example.airbnb.util.AppUtils.getCurrentUser;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingService {

   final  BookingRepository bookingRepository;
    final InventoryRepository inventoryRepository;
   final  HotelRepository hotelRepository;
   final  RoomRepository roomRepository;
   final  GuestRepository guestRepository;
   final  ModelMapper modelMapper;
   final  PricingService pricingService;
   final CheckoutService checkoutService;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Transactional
    public BookingDto initialiseBooking(BookingRequest request){
        Hotel hotel = hotelRepository.findById(request.getHotelId())
                .orElseThrow(() -> new AppException(ErrorCode.HOTEL_NOT_FOUND));

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));
        List<Inventory> inventoryList =  inventoryRepository.findAndLockAvailableInventory(request.getRoomId(), request.getCheckInDate(), request.getCheckOutDate(), request.getRoomCount());
        Long daysCount = ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate()) + 1;
         if(inventoryList.size() != daysCount){
             throw new IllegalStateException("Room not available more");
         }
         inventoryRepository.initBooking(room.getId(), request.getCheckInDate(), request.getCheckOutDate(), request.getRoomCount());

         BigDecimal bookingCostOneRoom = pricingService.calculateTotalPrice(inventoryList);
         BigDecimal totalCost = bookingCostOneRoom.multiply(BigDecimal.valueOf(request.getRoomCount()));

        Booking booking = Booking.builder()
                .bookingStatus(BookingStatus.RESERVED)
                .hotel(hotel)
                .room(room)
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .user(getCurrentUser())
                .amount(totalCost)
                .roomsCount(request.getRoomCount())
                .build();
        bookingRepository.save(booking);
        return modelMapper.map(booking, BookingDto.class);
    }

    public BookingDto addGuests(Long bookingId, List<GuestDto> request){
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        User user = getCurrentUser();
        if(!user.equals(booking.getUser())){
            throw new RuntimeException("Booking does not belong to this user");
        }
        if(hasBookingExpire(booking)){
            throw new IllegalStateException("Booking has already expired!");
        }
        if(booking.getBookingStatus() != BookingStatus.RESERVED){
            throw new IllegalStateException("You are not allowed to add guest");
        }

        for(GuestDto guestDto : request ){
            Guest guest = modelMapper.map(guestDto, Guest.class);
            guest.setUser(user);
            guestRepository.save(guest);
            booking.getGuests().add(guest);
        }
        booking.setBookingStatus(BookingStatus.GUEST_ADDED);
        bookingRepository.save(booking);
        return modelMapper.map(booking, BookingDto.class);
    }

    @Transactional
    public String initPayment(Long bookingId){
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
        User user = getCurrentUser();
        if(!user.equals(booking.getUser())){
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        if(hasBookingExpire(booking)){
            throw new IllegalStateException("Booking has already expired");
        }
        String sessionUrl = checkoutService.getCheckoutSession(booking, frontendUrl+"/payment/success", frontendUrl+"/payment/failure");
        booking.setBookingStatus(BookingStatus.PAYMENT_PENDING);
        bookingRepository.save(booking);
        return sessionUrl;
    }

    @Transactional
    public void capturePayment(Event event) {
        if("checkout.session.completed".equals(event.getType())){
            Session session  = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
            if(session != null){
                String sessionId = session.getId();
                Booking booking = bookingRepository.findByPaymentSessionId(sessionId).orElseThrow(
                        () -> new RuntimeException("Booking does not exist with session id : {}"+sessionId));
                booking.setBookingStatus(BookingStatus.CONFIRMED);
                bookingRepository.save(booking);

                inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(),booking.getCheckInDate(),booking.getCheckOutDate(), booking.getRoomsCount());

                inventoryRepository.confirmBooking(booking.getRoom().getId(),booking.getCheckInDate(),booking.getCheckOutDate(),booking.getRoomsCount());

                log.info("Booking confirmed for session ID: {}", sessionId);
            }
            else{
                log.warn("Unhandled event type: {}", event.getType());
            }
        }


    }


    @Transactional
    public void cancelBooking (Long bookingId){
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        User user = getCurrentUser();
        if(!user.equals(booking.getUser())){
            throw new IllegalStateException("Booking does not belong to this user");
        }
        if(booking.getBookingStatus() == BookingStatus.CONFIRMED || booking.getBookingStatus() == BookingStatus.PAYMENT_PENDING){
            throw new IllegalStateException("Can not cancel");
        }
        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(), booking.getCheckInDate(),booking.getCheckOutDate(), booking.getRoomsCount());
        inventoryRepository.cancelBooking(booking.getRoom().getId(), booking.getCheckInDate(), booking.getCheckOutDate(), booking.getRoomsCount());
    }

    public List<BookingDto> getAllBookingByHotel (Long hotelId){
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new AppException(ErrorCode.HOTEL_NOT_FOUND));

        User user = getCurrentUser();
        if(!user.equals(hotel.getOwner())){
            throw new RuntimeException("you are not authorized");
        }
        List<Booking> bookings = bookingRepository.findByHotel(hotel);
        return bookings.stream().map(
                booking ->  modelMapper.map(booking, BookingDto.class)
        ).toList();

    }

    public List<BookingDto> getMyBookings (){
        User user = getCurrentUser();
        List<Booking> bookings = bookingRepository.findByUser(user);
        return bookings.stream().map(
                booking -> modelMapper.map(booking, BookingDto.class)
        ).toList();
    }


    public boolean hasBookingExpire(Booking booking){
        return booking.getCreatedAt().plusMinutes(20).isBefore(LocalDateTime.now());
    }
}

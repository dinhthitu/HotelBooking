package com.example.airbnb.repository;

import com.example.airbnb.entities.Booking;
import com.example.airbnb.entities.Hotel;
import com.example.airbnb.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUser(User user);

    List<Booking> findByHotel(Hotel hotel);

    Optional<Booking> findByPaymentSessionId(String sessionId);
}

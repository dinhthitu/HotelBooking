package com.example.airbnb.repository;
import com.example.airbnb.dto.HotelPrice;
import com.example.airbnb.entities.Hotel;
import com.example.airbnb.entities.HotelMinPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HotelMinPriceRepository extends JpaRepository<HotelMinPrice, Long> {
    @Query(value = """    
            SELECT new com.example.airbnb.dto.HotelPrice(i.hotel,AVG(i.price))
            FROM HotelMinPrice i
            WHERE
                 i.hotel.address = :address
                 AND i.date BETWEEN :startDate AND :endDate
                 AND i.hotel.active = true
            GROUP BY i.hotel
            """)
    List<HotelPrice> findHotelsWithAvailableInventory(
            @Param("address") String address,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomsCount") Integer roomsCount,
            @Param("dateCount") Long dateCount
    );

    Optional<HotelMinPrice> findByHotelAndDate(Hotel hotel, LocalDate date);
}

package com.example.airbnb.repository;
import com.example.airbnb.dto.HotelPrice;
import com.example.airbnb.entities.Hotel;
import com.example.airbnb.entities.Inventory;
import com.example.airbnb.entities.Room;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    void deleteAllByRoom(Room room);

    @Query("SELECT i FROM Inventory i WHERE i.room = :room ORDER BY i.inventory_date ASC")
    List<Inventory> findByRoomOrderByInventoryDate(@Param("room") Room room);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.hotel.id = :hotelId AND i.inventory_date BETWEEN :start AND :end")
    List<Inventory> getInventoryLockedBeforeUpdating(@Param("hotelId") Long hotelId,
                                                     @Param("start") LocalDate start,
                                                     @Param("end") LocalDate end);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Inventory i
            SET i.surgeFactor = :surgeFactor,
                i.closed = :closed
            WHERE i.room.id = :roomId
              AND i.inventory_date BETWEEN :startDate AND :endDate
            """)
    void updateInventory(@Param("roomId") Long roomId,
                         @Param("startDate") LocalDate startDate,
                         @Param("endDate") LocalDate endDate,
                         @Param("closed") Boolean closed,
                         @Param("surgeFactor")BigDecimal surgeFactor);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT i FROM Inventory i WHERE i.room.id = :roomId
            AND i.inventory_date BETWEEN :checkInDate AND :checkOutDate
            AND i.closed = false
            AND (i.totalCount - i.bookedCount - i.reversedCount) >= :roomCount 
            """)
    List<Inventory> findAndLockAvailableInventory(
            @Param("roomId") Long roomId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("roomCount") Integer roomCount);


    @Modifying
    @Query("""
        UPDATE Inventory i SET i.reversedCount = i.reversedCount + :numberOfRooms
        WHERE i.room.id = :roomId
        AND i.inventory_date BETWEEN :checkInDate AND :checkOutDate
        AND (i.totalCount - i.bookedCount - i.reversedCount) >= :numberOfRooms
        AND i.closed = false
    """)
    void initBooking(
            @Param("roomId") Long id,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("numberOfRooms") Integer roomsCount);


    @Query("""
    SELECT i FROM Inventory i WHERE i.room.id = :roomId
    AND i.inventory_date BETWEEN :checkInDate AND :checkOutDate
    AND i.closed = true
    AND (i.totalCount - i.bookedCount) >= :roomCount
    """)
    List<Inventory> findAndLockReservedInventory(
            @Param("roomId") Long id,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("roomCount") Integer roomsCount);


    @Modifying
    @Query("""
    UPDATE Inventory i SET i.totalCount = i.totalCount + :roomCount
    where i.room.id = :roomId
    and i.inventory_date between :checkInDate and :checkOutDate
    and i.closed = true
    and (i.totalCount - i.bookedCount) >= :roomCount
    """)
    void cancelBooking(
            @Param("roomId") Long id,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("roomCount") Integer roomsCount);

    @Query("""
    select i from Inventory i where i.hotel = :hotel
    and i.inventory_date between :startDate and :endDate
    and i.closed = false
    """)
    List<Inventory> findByHotelAndInventoryDateBetween(
            @Param("hotel") Hotel hotel,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Modifying
    @Query("""
    update Inventory i set i.reversedCount = i.reversedCount - :numberOfRooms,
    i.bookedCount =  i.bookedCount + :numberOfRooms
    where i.room.id = :roomId
    and i.inventory_date between :startDate and :endDate
    and(i.totalCount - i.bookedCount) >= :numberOfRooms
    and i.reversedCount >= :numberOfRooms
    and i.closed = false
    """)
    void confirmBooking(
            @Param("roomId") Long id,
            @Param("startDate") LocalDate checkInDate,
            @Param("endDate") LocalDate checkOutDate,
            @Param("numberOfRooms") Integer roomsCount);

}

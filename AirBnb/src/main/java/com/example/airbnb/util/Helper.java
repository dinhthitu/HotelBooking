package com.example.airbnb.util;

import com.example.airbnb.entities.Hotel;
import com.example.airbnb.entities.Room;
import com.example.airbnb.entities.User;
import com.example.airbnb.enums.ErrorCode;
import com.example.airbnb.enums.Role;
import com.example.airbnb.exception.AppException;

public class Helper {
    private Helper() {
        // utility class, ngăn không cho new Helper()
    }
    public static void checkHotelPermission(User user, Hotel hotel){
        if(user.getRoles().contains(Role.HOTEL_MANAGER) && !user.equals(hotel.getOwner())){
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    public static void checkRoomPermission(User user, Room room) {
        if (user.getRoles().contains(Role.HOTEL_MANAGER)
                && !user.getId().equals(room.getHotel().getOwner().getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }
}

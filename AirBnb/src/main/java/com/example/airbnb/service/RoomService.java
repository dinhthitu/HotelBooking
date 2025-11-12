package com.example.airbnb.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.airbnb.dto.RoomDto;
import com.example.airbnb.entities.Hotel;
import com.example.airbnb.entities.Room;
import com.example.airbnb.entities.User;
import com.example.airbnb.enums.ErrorCode;
import com.example.airbnb.exception.AppException;
import com.example.airbnb.repository.HotelRepository;
import com.example.airbnb.repository.RoomRepository;
import com.example.airbnb.util.AppUtils;
import com.example.airbnb.util.Helper;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import static com.example.airbnb.util.AppUtils.getCurrentUser;


@RequiredArgsConstructor
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoomService {

    RoomRepository roomRepository;
    ModelMapper modelMapper;
    HotelRepository hotelRepository;
    InventoryService inventoryService;
    Cloudinary cloudinary;

    public RoomDto createRoom (Long hotelId, RoomDto request) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new AppException(ErrorCode.HOTEL_NOT_FOUND));
        User user = AppUtils.getCurrentUser();
        Helper.checkHotelPermission(user, hotel);
        Room room = modelMapper.map(request, Room.class);
        room.setHotel(hotel);
        roomRepository.save(room);
        if(hotel.getActive()){
           inventoryService.initializeRoomForYear(room);
        }
        return modelMapper.map(room, RoomDto.class);
    }

    public RoomDto updateRoom(Long roomId, RoomDto request){
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));
        User user = AppUtils.getCurrentUser();
        Helper.checkRoomPermission(user, room);
        modelMapper.map(request, room);
        roomRepository.save(room);
        return modelMapper.map(room, RoomDto.class);
    }

    public List<RoomDto> getAll(){
        return roomRepository.findAll().stream()
                .map(room -> modelMapper.map(room, RoomDto.class))
                .toList();
    }

    public List<RoomDto> getAllRoomsInHotel(Long hotelId){
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new AppException(ErrorCode.HOTEL_NOT_FOUND));

        return hotel.getRooms().stream().map(
                room -> modelMapper.map(room, RoomDto.class))
                .toList();

    }

    public RoomDto getRoomById(Long roomId){
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));

        return modelMapper.map(room, RoomDto.class);
    }

    @Transactional
    public void deleteRoomById(Long roomId){
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));
        User user = AppUtils.getCurrentUser();
        Helper.checkRoomPermission(user, room);
        inventoryService.deleteAll(room);
        roomRepository.deleteById(roomId);
    }

    @Transactional
    public String[] uploadImages (Long roomId, MultipartFile[] files) throws IOException {
        User user = getCurrentUser();
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));
        Helper.checkRoomPermission(user, room);
        String[] images = updateImages(files);
        room.setImages(images);
        roomRepository.save(room);
        return images;
    }
    private String[] updateImages (MultipartFile[] files) throws IOException {
        if(files.length == 0){
            throw new RuntimeException("No images are available");
        }
        List<String> contentType = Arrays.stream(files)
                .map(file -> file.getContentType())
                .toList();
        for(String type : contentType) {
            if (type == null || !type.startsWith("image/")) {
                throw new RuntimeException("Invalid file format");
            }
        }
        List<String> uploadedImages = new ArrayList<>();

        for(MultipartFile file : files){
            Map<String, Object> upload = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "image",
                            "folder" , "room",
                            "overwrite", true,
                            "public_id", UUID.randomUUID().toString()
                    )
            );
            Object secureUrl = upload.get("secure_url");
            uploadedImages.add(secureUrl.toString());
        }
        return uploadedImages.toArray(new String[0]);
    }

    public void deleteFile (Long roomId, String file) throws IOException{
        User user = getCurrentUser();
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));
        Helper.checkRoomPermission(user, room);
        if(file == null || file.isEmpty()){
            return;
        }
        String[] parts = file.split("/");
        String fileName = parts[parts.length - 1];
        String publicId = "room/" + fileName.substring(0, fileName.lastIndexOf("."));
        cloudinary.uploader().destroy(publicId,ObjectUtils.asMap("resource_type", "image") );

        if (room.getImages() != null && room.getImages().length > 0) {
            List<String> imagesList = new ArrayList<>(Arrays.asList(room.getImages()));
            boolean removed = imagesList.remove(file);
            if (removed) {
                room.setImages(imagesList.toArray(new String[0]));
                roomRepository.save(room);
            }
        }
    }

}

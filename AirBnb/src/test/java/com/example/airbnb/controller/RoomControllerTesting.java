package com.example.airbnb.controller;

import com.example.airbnb.dto.RoomDto;
import com.example.airbnb.enums.ErrorCode;
import com.example.airbnb.exception.AppException;
import com.example.airbnb.service.RoomService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
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

@SpringBootTest
@AutoConfigureMockMvc
public class RoomControllerTesting {

    @Autowired
    MockMvc mockMvc;

    private RoomDto roomDto;
    private final static ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private RoomService roomService;

    @BeforeEach
    void initData(){
        roomDto = RoomDto.builder()
                .id(152L)
                .type("Standard")
                .images(new String[] {"https://example.com/photos/room1.jpg"})
                .amenities(new String[] { "Wifi",
                        "Air Conditioning",
                        "Breakfast Included"})
                .basePrice(BigDecimal.valueOf(300.000))
                .totalCount(10)
                .capacity(4)
                .build();


    }

    @Test
    @WithMockUser(username="happybirthday@gmail.com", roles={"HOTEL_MANAGER"})
    void create_room_success() throws Exception{
        Mockito.when(roomService.createRoom(203L, roomDto))
                .thenReturn(roomDto);
        mockMvc.perform(MockMvcRequestBuilders
                .post("/rooms/create-room/203")
                .content(objectMapper.writeValueAsString(roomDto))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("result.id").value(152))
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("result.basePrice").value(300.000))
                .andExpect(MockMvcResultMatchers.jsonPath("result.type").value("Standard"))
                .andExpect(MockMvcResultMatchers.jsonPath("result.totalCount").value(10))
                .andExpect(MockMvcResultMatchers.jsonPath("result.capacity").value(4));

    }

    @Test
    @WithMockUser(username="happybirthday@gmail.com", roles={"HOTEL_MANAGER"})
    void create_room_failed() throws Exception{
        Mockito.doThrow(new AppException(ErrorCode.HOTEL_NOT_FOUND))
                .when(roomService).createRoom(299L, roomDto);
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/rooms/create-room/299")
                        .content(objectMapper.writeValueAsString(roomDto))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(404));

    }

    @Test
    @WithMockUser(username="happybirthday@gmail.com", roles={"HOTEL_MANAGER"})
    void update_room_success()throws Exception{
        roomDto.setBasePrice(BigDecimal.valueOf(200.000));
        Mockito.when(roomService.updateRoom(152L, roomDto))
                .thenReturn(roomDto);
        mockMvc.perform(MockMvcRequestBuilders
                        .patch("/rooms/update-room/152")
                        .content(objectMapper.writeValueAsString(roomDto))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("result.id").value(152))
                .andExpect(MockMvcResultMatchers.jsonPath("result.basePrice").value(200.000))
                .andExpect(MockMvcResultMatchers.jsonPath("result.type").value("Standard"))
                .andExpect(MockMvcResultMatchers.jsonPath("result.totalCount").value(10))
                .andExpect(MockMvcResultMatchers.jsonPath("result.capacity").value(4));

    }

    @Test
    @WithMockUser(username="happybirthday@gmail.com", roles={"HOTEL_MANAGER"})
    void update_room_failed() throws Exception{
        Mockito.doThrow(new AppException(ErrorCode.ROOM_NOT_FOUND))
                .when(roomService).updateRoom(299L, roomDto);
        mockMvc.perform(MockMvcRequestBuilders
                        .patch("/rooms/update-room/299")
                        .content(objectMapper.writeValueAsString(roomDto))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(400));

    }

    @Test
    @WithMockUser(username="happybirthday@gmail.com", roles={"HOTEL_MANAGER"})
    void get_room_by_id_success() throws Exception{
        Mockito.when(roomService.getRoomById(ArgumentMatchers.any()))
                .thenReturn(roomDto);
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/rooms/152"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("result.id").value(152))
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("result.basePrice").value(300.000))
                .andExpect(MockMvcResultMatchers.jsonPath("result.type").value("Standard"))
                .andExpect(MockMvcResultMatchers.jsonPath("result.totalCount").value(10))
                .andExpect(MockMvcResultMatchers.jsonPath("result.capacity").value(4));

    }

    @Test
    @WithMockUser(username="happybirthday@gmail.com", roles={"HOTEL_MANAGER"})
    void delete_success() throws Exception{
        Mockito.doNothing().when(roomService).deleteRoomById(ArgumentMatchers.any());
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/rooms/152"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(200));
    }



}

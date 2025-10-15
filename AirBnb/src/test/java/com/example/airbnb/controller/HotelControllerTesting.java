package com.example.airbnb.controller;

import com.example.airbnb.dto.HotelDto;
import com.example.airbnb.dto.HotelInforDto;
import com.example.airbnb.dto.request.HotelSearchRequest;
import com.example.airbnb.entities.HotelContactInfor;
import com.example.airbnb.enums.ErrorCode;
import com.example.airbnb.exception.AppException;
import com.example.airbnb.service.BookingService;
import com.example.airbnb.service.HotelService;
import com.example.airbnb.service.InventoryService;
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

import java.util.ArrayList;
import java.util.List;


@SpringBootTest
@AutoConfigureMockMvc
public class HotelControllerTesting {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HotelService hotelService;

    @MockitoBean
    private InventoryService inventoryService;

    @MockitoBean
    private BookingService bookingService;

    @MockitoBean
    private RoomService roomService;

    private final static  ObjectMapper objectMapper = new ObjectMapper();

    private HotelDto hotelDto;
    private HotelContactInfor hotelContactInfor;
    private HotelInforDto hotelInforDto;


    @BeforeEach
    void initData(){
        hotelContactInfor = HotelContactInfor.builder()
                .phoneNumber("+84-666-666-666")
                .email("contact6@vigera.com")
                .address("https://vigera6.com")
                .build();

        hotelDto = HotelDto.builder()
                .name("Happy Birthday")
                .address("Pham Ngu Lao Street")
                .photos( new String[]{"photo1.jpg", "photo2.jpg"} )
                .amenities(new String[] {"Free WiFi", "Bar", "Airport Shuttle"})
                .contactInfor(hotelContactInfor)
                .active(true)
                .build();
        hotelInforDto = HotelInforDto.builder()
                .hotel(hotelDto)
                .rooms(List.of()).build();

    }

    @Test
    @WithMockUser(username="happybirthday@gmail.com", roles={"HOTEL_MANAGER"})
    void create_hotel_success() throws Exception{
        Mockito.when(hotelService.createHotel(ArgumentMatchers.any()))
                .thenReturn(hotelDto);

        mockMvc.perform(MockMvcRequestBuilders
                .post("/hotels/create-hotel")
                .content(objectMapper.writeValueAsString(hotelDto))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("result.name").value("Happy Birthday"))
                .andExpect(MockMvcResultMatchers.jsonPath("result.address").value("Pham Ngu Lao Street"))
                .andExpect(MockMvcResultMatchers.jsonPath("result.photos[0]").value("photo1.jpg"))
                .andExpect(MockMvcResultMatchers.jsonPath("result.photos[1]").value("photo2.jpg"))
                .andExpect(MockMvcResultMatchers.jsonPath("result.amenities[0]").value("Free WiFi"))
                .andExpect(MockMvcResultMatchers.jsonPath("result.amenities[1]").value("Bar"))
                .andExpect(MockMvcResultMatchers.jsonPath("result.amenities[2]").value("Airport Shuttle"));


    }

    @Test
    @WithMockUser(username="happybirthday@gmail.com", roles={"HOTEL_MANAGER"})
    void create_hotel_fail() throws Exception{
        Mockito.doThrow(new AppException(ErrorCode.HOTEL_EXISTED)).when(hotelService).createHotel(ArgumentMatchers.any());

        mockMvc.perform(MockMvcRequestBuilders
                .post("/hotels/create-hotel")
                        .content(objectMapper.writeValueAsString(hotelDto))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isFound())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(302))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("HOTEL EXISTED"));
    }

    @Test
    void createHotel_unauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/hotels/create-hotel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hotelDto)))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username="happybirthday@gmail.com", roles={"HOTEL_MANAGER"})
    void update_hotel_success() throws Exception{
        hotelDto.setAddress("hong ha street");

        Mockito.when(hotelService.updateHotelById(202L, hotelDto))
                .thenReturn(hotelDto);
        mockMvc.perform(MockMvcRequestBuilders
                .patch("/hotels/update/202")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(hotelDto)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("result.address").value("hong ha street"));
    }

    @Test
    @WithMockUser(username="happybirthday@gmail.com", roles={"HOTEL_MANAGER"})
    void update_hotel_failed() throws Exception{
        Mockito.doThrow(new AppException(ErrorCode.HOTEL_NOT_FOUND)).when(hotelService).updateHotelById(111L, hotelDto);
        mockMvc.perform(MockMvcRequestBuilders
                        .patch("/hotels/update/111")
                        .content(objectMapper.writeValueAsString(hotelDto))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))

//                .patch("/hotels/update/111")
//                        .content(objectMapper.writeValueAsString(hotelDto)))      )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(404))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("HOTEL NOT FOUND"));
    }

    @Test
    @WithMockUser(username="happybirthday@gmail.com", roles={"HOTEL_MANAGER"})
    void delete_hotel_success() throws Exception{
        Mockito.doNothing().when(hotelService).deleteHotelById(202L);
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/hotels/202"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(200));

    }
    @Test
    @WithMockUser(username="happybirthday@gmail.com", roles={"HOTEL_MANAGER"})
    void delete_hotel_failed() throws Exception{
        Mockito.doThrow(new AppException(ErrorCode.UNAUTHORIZED))
                .when(hotelService).deleteHotelById(2L);
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/hotels/2"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(401))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("YOU ARE UNAUTHORIZED"));

    }

    @Test
    @WithMockUser(username="happybirthday@gmail.com", roles={"HOTEL_MANAGER"})
    void get_hotel_by_id_success() throws Exception{
        hotelDto.setId(203L);
        Mockito.when(hotelService.getHotelInforById(203L))
                .thenReturn(hotelInforDto);

        mockMvc.perform(MockMvcRequestBuilders
                .get("/hotels/203"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(200));

    }

    @Test
    @WithMockUser(username="happybirthday@gmail.com", roles={"HOTEL_MANAGER"})
    void get_hotel_by_id_failed() throws Exception{
        Mockito.doThrow(new AppException(ErrorCode.HOTEL_NOT_FOUND))
                .when(hotelService).getHotelInforById(202L);
        mockMvc.perform(MockMvcRequestBuilders
                .get("/hotels/202"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(404))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("HOTEL NOT FOUND"));
    }

    @Test
    @WithMockUser(username="happybirthday@gmail.com", roles={"HOTEL_MANAGER"})
    void gen_contact_success() throws Exception{
        mockMvc.perform(MockMvcRequestBuilders
                .post("/hotels/contact/203")
                .content(objectMapper.writeValueAsString(hotelContactInfor))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(200));

    }


    @Test
    @WithMockUser(username="happybirthday@gmail.com", roles={"HOTEL_MANAGER"})
    void gen_contact_failed() throws Exception{

        Mockito.doThrow(new AppException(ErrorCode.HOTEL_NOT_FOUND)).when(hotelService).genHotelContactInfor(203L, hotelContactInfor);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/hotels/contact/203")
                        .content(objectMapper.writeValueAsString(hotelContactInfor))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(404))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("HOTEL NOT FOUND"));

    }


}

package com.example.airbnb.controller;

import com.example.airbnb.dto.InventoryDto;
import com.example.airbnb.dto.request.UpdateInventoryRequest;
import com.example.airbnb.enums.ErrorCode;
import com.example.airbnb.exception.AppException;
import com.example.airbnb.service.InventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
public class InventoryControllerTesting {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private InventoryService inventoryService;

    private InventoryDto inventoryDto;
    private UpdateInventoryRequest updateInventoryRequest;

    private final static ObjectMapper objectMapper = new ObjectMapper();


    @Test
    @WithMockUser(username="happybirthday@gmail.com", roles={"HOTEL_MANAGER"})
    void get_all_by_id_failed() throws Exception{
        Mockito.doThrow(new AppException(ErrorCode.ROOM_NOT_FOUND)).when(inventoryService)
                .getAllInventoryByRoom(1112L);

        mockMvc.perform(MockMvcRequestBuilders
                .get("/admin/inventory/rooms/1112"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(400))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("ROOM NOT FOUND"));
    }

    @Test
    @WithMockUser(username="happybirthday@gmail.com", roles={"HOTEL_MANAGER"})
    void get_all_by_id_success() throws Exception{
        InventoryDto inventoryDto = new InventoryDto();
        inventoryDto.setId(1903L);
        inventoryDto.setInventoryDate(LocalDate.of(2025, 10, 11));
        inventoryDto.setBookedCount(0);
        inventoryDto.setTotalCount(10);
        inventoryDto.setSurgeFactor(new BigDecimal("1.00"));
        inventoryDto.setPrice(new BigDecimal("300.00"));
        inventoryDto.setAddress("abc hong ha");
        inventoryDto.setClosed(false);
        inventoryDto.setCreatedAt(LocalDateTime.of(2025, 10, 10, 21, 36, 2, 162418000));
        inventoryDto.setUpdatedAt(LocalDateTime.of(2025, 10, 10, 21, 36, 2, 162418000));

        List<InventoryDto> inventoryList = List.of(inventoryDto);
        Mockito.when(inventoryService.getAllInventoryByRoom(152L))
                .thenReturn(inventoryList);
        mockMvc.perform(MockMvcRequestBuilders
                .get("/admin/inventory/rooms/152"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("result.length()").value(1));

    }

    @Test
    @WithMockUser(username="happybirthday@gmail.com", roles={"HOTEL_MANAGER"})
    void update_inventory_success()throws Exception{
        updateInventoryRequest = UpdateInventoryRequest.builder()
                .closed(false)
                .surgeFactor(BigDecimal.valueOf(1.25))
                .build();

        Mockito.doNothing().when(inventoryService).updateInventory(152L, updateInventoryRequest);
        mockMvc.perform(MockMvcRequestBuilders
                .patch("/admin/inventory/rooms/152")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(updateInventoryRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(200));

    }

}

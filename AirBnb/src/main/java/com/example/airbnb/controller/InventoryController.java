package com.example.airbnb.controller;

import com.example.airbnb.dto.ApiResponse;
import com.example.airbnb.dto.InventoryDto;
import com.example.airbnb.dto.request.UpdateInventoryRequest;
import com.example.airbnb.service.InventoryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/inventory")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InventoryController {

    InventoryService inventoryService;

    @GetMapping("/rooms/{roomId}")
    public ApiResponse<List<InventoryDto>> getAllInventoryByRoom(@PathVariable Long roomId){
        return ApiResponse.<List<InventoryDto>>builder()
                .result(inventoryService.getAllInventoryByRoom(roomId))
                .build();
    }

    @PatchMapping("/rooms/{roomId}")
    public ApiResponse<Void> updateInventory(@PathVariable Long roomId, @RequestBody UpdateInventoryRequest request){
        inventoryService.updateInventory(roomId, request);
        return ApiResponse.<Void>builder().build();
    }
}

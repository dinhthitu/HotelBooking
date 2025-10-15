package com.example.airbnb.entities;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "hotels")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Hotel {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    Long id;

    String name;
    @Column(insertable=false, updatable=false)
    String address;

    @Column(columnDefinition = "TEXT[]")
    String[] images;

    @Column(columnDefinition = "TEXT[]")
    String[] amenities;

    @CreationTimestamp
    LocalDateTime createdAt;

    @UpdateTimestamp
    LocalDateTime updatedAt;

    @OneToMany(mappedBy = "hotel")
    @JsonIgnore
    List<Room> rooms;

    @ManyToOne
    User owner;

    @Embedded
    HotelContactInfor contactInfor;

    Boolean active;


}

package com.cinemabooking.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cinema {
    public String cinemaId;
    public String name;
    public String address;
    public String city;
    public String district;
    public String phone;
    public double latitude;
    public double longitude;
    public String status;
    public long createdAt;
    public long updatedAt;
    public boolean deleted;
}
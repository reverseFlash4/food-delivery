package com.fooddelivery.dto.request;

import jakarta.validation.constraints.NotBlank;

public class CreateCityRequest {
    @NotBlank private String name;
    @NotBlank private String state;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
}

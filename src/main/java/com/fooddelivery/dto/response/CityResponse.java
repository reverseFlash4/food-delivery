package com.fooddelivery.dto.response;

import com.fooddelivery.model.City;

public class CityResponse {
    private Long id;
    private String name;
    private String state;
    private boolean active;

    public CityResponse() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id; private String name; private String state; private boolean active;
        public Builder id(Long id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder state(String state) { this.state = state; return this; }
        public Builder active(boolean active) { this.active = active; return this; }
        public CityResponse build() {
            CityResponse r = new CityResponse();
            r.id = id; r.name = name; r.state = state; r.active = active;
            return r;
        }
    }

    public static CityResponse from(City city) {
        return builder().id(city.getId()).name(city.getName())
                .state(city.getState()).active(city.isActive()).build();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getState() { return state; }
    public boolean isActive() { return active; }
}

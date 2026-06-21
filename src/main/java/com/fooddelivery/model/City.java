package com.fooddelivery.model;

import jakarta.persistence.*;

@Entity
@Table(name = "cities")
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String state;

    @Column(nullable = false)
    private boolean active = true;

    public City() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id; private String name; private String state; private boolean active = true;
        public Builder id(Long id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder state(String state) { this.state = state; return this; }
        public Builder active(boolean active) { this.active = active; return this; }
        public City build() {
            City c = new City(); c.id = id; c.name = name; c.state = state; c.active = active;
            return c;
        }
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getState() { return state; }
    public boolean isActive() { return active; }

    public void setName(String name) { this.name = name; }
    public void setState(String state) { this.state = state; }
    public void setActive(boolean active) { this.active = active; }
}

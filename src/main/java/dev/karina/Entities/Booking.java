package dev.karina.Entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@Data
public class Booking {
    private String firstname;
    private String lastname;
    private Number totalprice;
    private Boolean depositpaid;
    private BookingDates bookingdates;
    private String additionalneeds;
}
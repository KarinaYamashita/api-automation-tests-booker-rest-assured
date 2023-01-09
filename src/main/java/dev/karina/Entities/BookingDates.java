package dev.karina.Entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@Data
public class BookingDates {
   String checkin;
   String checkout;
}
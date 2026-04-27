package com.theatermgnt.theatermgnt.booking.mapper;

import java.math.BigDecimal;
import java.util.List;

import org.mapstruct.Mapper;

import com.theatermgnt.theatermgnt.booking.dto.response.BookingSummaryResponse;
import com.theatermgnt.theatermgnt.booking.entity.Booking;
import com.theatermgnt.theatermgnt.bookingCombo.dto.response.ComboSummaryResponse;
import com.theatermgnt.theatermgnt.bookingCombo.entity.BookingCombo;
import com.theatermgnt.theatermgnt.movie.dto.response.MovieResponse;
import com.theatermgnt.theatermgnt.seat.dto.response.SeatResponse;

@Mapper(componentModel = "spring")
public interface BookingSummaryMapper {

    default BookingSummaryResponse toSummaryResponse(
            Booking booking, List<BookingCombo> combos, List<SeatResponse> seats, MovieResponse movie) {
        BookingSummaryResponse res = new BookingSummaryResponse();

        res.setBookingId(booking.getId().toString());
        res.setStatus(booking.getStatus());
        res.setExpiredAt(booking.getExpiredAt());

        res.setCombos(mapCombos(combos));
        res.setSeats(seats);

        BigDecimal comboSubtotal = calculateComboSubtotal(combos);
        res.setSubTotal(booking.getSubtotal());
        res.setSeatSubtotal(booking.getSubtotal().subtract(comboSubtotal));
        res.setComboSubtotal(comboSubtotal);
        res.setDiscountAmount(booking.getDiscount());
        res.setTotalAmount(booking.getTotalAmount());

        res.setStartTime(booking.getScreening().getStartTime());
        res.setMovie(movie);

        return res;
    }

    private List<ComboSummaryResponse> mapCombos(List<BookingCombo> combos) {
        if (combos == null || combos.isEmpty()) {
            return List.of();
        }

        return combos.stream()
                .map(combo -> {
                    ComboSummaryResponse cs = new ComboSummaryResponse();
                    cs.setComboId(combo.getComboId());
                    cs.setComboName(combo.getComboName());
                    cs.setQuantity(combo.getQuantity());
                    cs.setUnitPrice(combo.getUnitPrice());
                    cs.setSubtotal(combo.getSubtotal());
                    return cs;
                })
                .toList();
    }

    private BigDecimal calculateComboSubtotal(List<BookingCombo> combos) {
        if (combos == null || combos.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return combos.stream().map(BookingCombo::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

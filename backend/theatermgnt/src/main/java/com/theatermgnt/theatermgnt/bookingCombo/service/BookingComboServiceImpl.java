package com.theatermgnt.theatermgnt.bookingCombo.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.theatermgnt.theatermgnt.booking.dto.response.BookingPricingResponse;
import com.theatermgnt.theatermgnt.booking.entity.Booking;
import com.theatermgnt.theatermgnt.booking.enums.BookingStatus;
import com.theatermgnt.theatermgnt.booking.mapper.BookingPricingMapper;
import com.theatermgnt.theatermgnt.booking.repository.BookingRepository;
import com.theatermgnt.theatermgnt.bookingCombo.dto.request.ComboItemRequest;
import com.theatermgnt.theatermgnt.bookingCombo.dto.request.UpdateBookingCombosRequest;
import com.theatermgnt.theatermgnt.bookingCombo.dto.response.ComboCheckInResponse;
import com.theatermgnt.theatermgnt.bookingCombo.entity.BookingCombo;
import com.theatermgnt.theatermgnt.bookingCombo.repository.BookingComboRepository;
import com.theatermgnt.theatermgnt.combo.dto.response.ComboItemResponse;
import com.theatermgnt.theatermgnt.combo.dto.response.ComboResponse;
import com.theatermgnt.theatermgnt.combo.entity.Combo;
import com.theatermgnt.theatermgnt.combo.mapper.ComboItemMapper;
import com.theatermgnt.theatermgnt.combo.mapper.ComboMapper;
import com.theatermgnt.theatermgnt.combo.repository.ComboItemRepository;
import com.theatermgnt.theatermgnt.combo.repository.ComboRepository;
import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.common.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class BookingComboServiceImpl implements BookingComboService {
    private final BookingComboRepository bookingComboRepository;
    private final BookingRepository bookingRepository;
    private final ComboRepository comboRepository;
    private final ComboItemRepository comboItemRepository;
    private final ComboMapper comboMapper;
    private final BookingPricingMapper bookingPricingMapper;
    private final ComboItemMapper comboItemMapper;

    @Override
    public BookingPricingResponse updateCombos(UUID bookingId, UpdateBookingCombosRequest request) {
        Booking booking = getValidPendingBooking(bookingId);

        BigDecimal oldComboSubtotal = bookingComboRepository.sumSubtotalByBookingId(bookingId.toString());
        booking.setSubtotal(booking.getSubtotal().subtract(oldComboSubtotal));

        // Xóa tất cả các BookingCombo hiện có cho bookingId
        bookingComboRepository.deleteByBookingId(bookingId.toString());
        BigDecimal comboSubtotal = BigDecimal.ZERO;

        // Tạo mới các BookingCombo từ request
        for (ComboItemRequest item : request.getCombos()) {

            Combo combo = comboRepository
                    .findById(item.getComboId())
                    .orElseThrow(() -> new IllegalStateException("Combo not found"));
            if (combo.getDeleted() == true) {
                throw new AppException(ErrorCode.COMBO_NOT_EXISTED);
            }

            BigDecimal unitPrice = combo.getPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

            BookingCombo bc = new BookingCombo(
                    null,
                    bookingId.toString(),
                    item.getComboId(),
                    combo.getName(),
                    item.getQuantity(),
                    item.getQuantity(),
                    unitPrice,
                    subtotal);

            bookingComboRepository.save(bc);
            comboSubtotal = comboSubtotal.add(subtotal);
        }

        // Update booking pricing
        booking.setSubtotal(booking.getSubtotal().add(comboSubtotal));
        booking.setTotalAmount(booking.getSubtotal().subtract(booking.getDiscount()));

        return bookingPricingMapper.toPricingResponse(booking);
    }

    private Booking getValidPendingBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Booking is not editable");
        }

        if (booking.getExpiredAt().isBefore(Instant.now())) {
            throw new IllegalStateException("Booking expired");
        }

        return booking;
    }

    @Override
    public List<ComboCheckInResponse> getCombos(UUID bookingId) {
        List<ComboCheckInResponse> responses = new ArrayList<>();
        List<BookingCombo> combos = bookingComboRepository.findByBookingId(bookingId.toString());

        for (BookingCombo combo : combos) {
            Optional<Combo> cb = comboRepository.findById(combo.getComboId());
            if (cb.isPresent()) {
                ComboResponse cr = comboMapper.toComboResponse(cb.get());
                List<ComboItemResponse> items = comboItemRepository.findByComboId(combo.getComboId()).stream()
                        .map(comboItemMapper::toComboItemResponse)
                        .toList();
                ComboCheckInResponse response =
                        new ComboCheckInResponse(combo.getId(), combo.getQuantity(), combo.getRemain(), cr, items);
                responses.add(response);
            }
        }
        return responses;
    }
}

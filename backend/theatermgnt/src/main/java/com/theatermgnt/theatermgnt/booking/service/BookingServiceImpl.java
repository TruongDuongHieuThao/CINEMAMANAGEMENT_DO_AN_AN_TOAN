package com.theatermgnt.theatermgnt.booking.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.theatermgnt.theatermgnt.account.entity.Account;
import com.theatermgnt.theatermgnt.account.repository.AccountRepository;
import com.theatermgnt.theatermgnt.authentication.enums.AccountType;
import com.theatermgnt.theatermgnt.booking.dto.request.CreateBookingRequest;
import com.theatermgnt.theatermgnt.booking.dto.request.DiscountPointRequest;
import com.theatermgnt.theatermgnt.booking.dto.response.BookingListItemResponse;
import com.theatermgnt.theatermgnt.booking.dto.response.BookingListResponse;
import com.theatermgnt.theatermgnt.booking.dto.response.BookingSummaryResponse;
import com.theatermgnt.theatermgnt.booking.dto.response.CreateBookingResponse;
import com.theatermgnt.theatermgnt.booking.entity.Booking;
import com.theatermgnt.theatermgnt.booking.enums.BookingStatus;
import com.theatermgnt.theatermgnt.booking.mapper.BookingMapper;
import com.theatermgnt.theatermgnt.booking.mapper.BookingSummaryMapper;
import com.theatermgnt.theatermgnt.booking.repository.BookingRepository;
import com.theatermgnt.theatermgnt.bookingCombo.entity.BookingCombo;
import com.theatermgnt.theatermgnt.bookingCombo.repository.BookingComboRepository;
import com.theatermgnt.theatermgnt.common.entity.BaseEntity;
import com.theatermgnt.theatermgnt.common.enums.DayType;
import com.theatermgnt.theatermgnt.common.enums.MovieStatus;
import com.theatermgnt.theatermgnt.common.enums.TimeSlot;
import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.common.exception.ErrorCode;
import com.theatermgnt.theatermgnt.customer.entity.Customer;
import com.theatermgnt.theatermgnt.customer.event.CustomerCreatedEvent;
import com.theatermgnt.theatermgnt.customer.repository.CustomerRepository;
import com.theatermgnt.theatermgnt.customer.service.CustomerService;
import com.theatermgnt.theatermgnt.movie.dto.response.MovieResponse;
import com.theatermgnt.theatermgnt.movie.service.MovieService;
import com.theatermgnt.theatermgnt.priceConfig.entity.PriceConfig;
import com.theatermgnt.theatermgnt.priceConfig.repository.PriceConfigRepository;
import com.theatermgnt.theatermgnt.screening.entity.Screening;
import com.theatermgnt.theatermgnt.screening.repository.ScreeningRepository;
import com.theatermgnt.theatermgnt.screeningSeat.entity.ScreeningSeat;
import com.theatermgnt.theatermgnt.screeningSeat.enums.ScreeningSeatStatus;
import com.theatermgnt.theatermgnt.screeningSeat.repository.ScreeningSeatRepository;
import com.theatermgnt.theatermgnt.seat.entity.Seat;
import com.theatermgnt.theatermgnt.seat.mapper.SeatMapper;
import com.theatermgnt.theatermgnt.ticket.service.TicketService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final AccountRepository accountRepository;
    private final BookingComboRepository bookingComboRepository;
    private final ScreeningSeatRepository screeningSeatRepository;
    private final ScreeningRepository screeningRepository;
    private final CustomerRepository customerRepository;
    private final PriceConfigRepository priceConfigRepository;
    private final BookingMapper bookingMapper;
    private final BookingSummaryMapper bookingSummaryMapper;
    private final SeatMapper seatMapper;
    private final PasswordEncoder passwordEncoder;
    private final MovieService movieService;
    private final CustomerService customerService;
    private final DiscountService discountService;
    private final TicketService ticketService;
    private final ApplicationEventPublisher eventPublisher;
    private static final Duration HOLD_DURATION = Duration.ofMinutes(10);

    @Override
    public CreateBookingResponse createBooking(CreateBookingRequest request) {
        if (request.getScreeningSeatIds().isEmpty()) {
            throw new IllegalArgumentException("No seats selected for booking");
        }
        if (request.getScreeningSeatIds().size() > 8) {
            throw new AppException(ErrorCode.BOOKING_EXCEED_SEAT_LIMIT);
        }
        Screening screening = screeningRepository
                .findById(request.getScreeningId())
                .orElseThrow(() -> new AppException(ErrorCode.SCREENING_NOT_EXISTED));
        if (screening.getMovie().getStatus() == MovieStatus.archived) {
            throw new AppException(ErrorCode.MOVIE_ALREADY_ENDED);
        }

        validateScreeningSeat(screening, request.getScreeningSeatIds());

        Instant now = Instant.now();
        Instant expiredAt = now.plus(HOLD_DURATION);

        int lockedCount = screeningSeatRepository.lockSeats(request.getScreeningSeatIds(), expiredAt);

        if (lockedCount != request.getScreeningSeatIds().size()) {
            throw new AppException(ErrorCode.SCREENING_SEATS_NOT_AVAILABLE);
        }

        List<ScreeningSeat> seats = screeningSeatRepository.findAllById(request.getScreeningSeatIds());
        Customer customer = resolveCustomer(request);

        // 3. Tạo booking
        Booking booking = new Booking();
        booking.setCustomer(customer);
        booking.setScreening(screening);
        booking.setStatus(BookingStatus.PENDING);
        booking.setCreatedAt(now);
        booking.setExpiredAt(expiredAt);
        BigDecimal seatSubtotal = calculateSeatSubtotal(seats);
        booking.setSubtotal(seatSubtotal);
        booking.setDiscount(BigDecimal.ZERO);
        booking.setTotalAmount(seatSubtotal);

        bookingRepository.saveAndFlush(booking);

        seats.forEach(seat -> seat.setBooking(booking.getId().toString()));
        screeningSeatRepository.saveAll(seats);

        return bookingMapper.toCreateBookingResponse(booking);
    }

    private Customer resolveCustomer(CreateBookingRequest request) {
        if (request.getCustomerId() != null) {
            return customerRepository
                    .findById(request.getCustomerId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        }

        populateNamesFromCustomerName(request);

        if (request.getCustomerId() == null
                && request.getCustomerName() == null
                && request.getEmail() == null
                && request.getFirstName() == null
                && request.getLastName() == null) {
            return null;
        }

        // Kiểm tra Account tồn tại
        Optional<Account> existingAccount = accountRepository.findByEmail(request.getEmail());
        if (existingAccount.isPresent()) {
            return customerRepository
                    .findByAccountId(existingAccount.get().getId())
                    .orElseGet(() -> createCustomerWithAccount(existingAccount.get(), request));
        }

        String rawPassword = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        Account newAccount = new Account();
        newAccount.setPassword(passwordEncoder.encode(rawPassword));
        newAccount.setEmail(request.getEmail());
        newAccount.setAccountType(AccountType.CUSTOMER);
        newAccount.setIsActive(true);

        accountRepository.save(newAccount);
        Customer savedCustomer = createCustomerWithAccount(newAccount, request);

        eventPublisher.publishEvent(CustomerCreatedEvent.builder()
                .customerId(savedCustomer.getId())
                .rawPassword(rawPassword)
                .build());

        return savedCustomer;
    }

    private void populateNamesFromCustomerName(CreateBookingRequest request) {
        if (hasText(request.getFirstName()) || hasText(request.getLastName())) {
            return;
        }
        if (!hasText(request.getCustomerName())) {
            return;
        }

        String[] parts = request.getCustomerName().trim().split("\\s+");
        if (parts.length == 1) {
            request.setFirstName(parts[0]);
            request.setLastName("");
            return;
        }

        String firstName = parts[parts.length - 1];
        String lastName = String.join(" ", Arrays.copyOf(parts, parts.length - 1));
        request.setFirstName(firstName);
        request.setLastName(lastName);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private Customer createCustomerWithAccount(Account account, CreateBookingRequest request) {
        Optional<Customer> customer = customerRepository.findByAccountId(account.getId());
        if (customer.isPresent()) {
            return customer.get();
        }
        Customer newCustomer = new Customer();
        newCustomer.setAccount(account);
        newCustomer.setFirstName(request.getFirstName());
        newCustomer.setLastName(request.getLastName());

        return customerRepository.save(newCustomer);
    }

    private BigDecimal calculateSeatSubtotal(List<ScreeningSeat> seats) {
        BigDecimal subTotal = BigDecimal.ZERO;
        for (ScreeningSeat seat : seats) {
            Seat s = seat.getSeat();
            Screening screening = seat.getScreening();
            TimeSlot timeSlot = TimeSlot.from(screening.getStartTime().toLocalTime());
            DayType dayType = DayType.from(screening.getStartTime().toLocalDate());
            PriceConfig priceConfig = priceConfigRepository.getPriceBySeatTypeIdAndDayTypeAndTimeSlot(
                    s.getSeatType().getId(), dayType, timeSlot);
            BigDecimal price;
            if (priceConfig == null || priceConfig.getPrice() == null) {
                price = seat.getSeat().getSeatType().getBasePriceModifier();
            } else {
                price = priceConfig.getPrice();
            }
            subTotal = subTotal.add(price);
        }
        return subTotal;
    }

    @Override
    public BookingSummaryResponse getBookingSummary(UUID bookingId) {
        Booking booking = bookingRepository
                .findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_EXISTED));

        List<ScreeningSeat> screeningSeats = screeningSeatRepository.findByBooking(bookingId.toString());
        List<BookingCombo> combo = bookingComboRepository.findByBookingId(bookingId.toString());
        MovieResponse movieResponse =
                movieService.getMovieById(booking.getScreening().getMovie().getId());

        return bookingSummaryMapper.toSummaryResponse(
                booking,
                combo,
                (screeningSeats.stream().map(ScreeningSeat::getSeat).toList())
                        .stream().map(seatMapper::toSeatResponse).toList(),
                movieResponse);
    }

    @Override
    public BookingSummaryResponse redeemPoints(UUID bookingId, DiscountPointRequest pointsToRedeem) {
        Booking booking = bookingRepository
                .findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_EXISTED));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Only pending bookings can redeem points");
        }

        BigDecimal maxDiscountByPercent = booking.getTotalAmount().multiply(new BigDecimal("0.5"));
        if (BigDecimal.valueOf(pointsToRedeem.getPointsToRedeem()).compareTo(maxDiscountByPercent) > 0) {
            throw new IllegalArgumentException("Cannot redeem more than 50% of total amount");
        }

        if (pointsToRedeem.getPointsToRedeem()
                > customerService
                        .getLoyaltyPoints(booking.getCustomer().getId())
                        .getLoyaltyPoints()) {
            throw new AppException(ErrorCode.INSUFFICIENT_LOYALTY_POINTS);
        }

        booking = discountService.applyDiscounts(booking, pointsToRedeem.getPointsToRedeem());

        bookingRepository.save(booking);

        List<ScreeningSeat> screeningSeats = screeningSeatRepository.findByBooking(bookingId.toString());
        List<BookingCombo> combo = bookingComboRepository.findByBookingId(bookingId.toString());
        MovieResponse movieResponse =
                movieService.getMovieById(booking.getScreening().getMovie().getId());

        return bookingSummaryMapper.toSummaryResponse(
                booking,
                combo,
                (screeningSeats.stream().map(ScreeningSeat::getSeat).toList())
                        .stream().map(seatMapper::toSeatResponse).toList(),
                movieResponse);
    }

    @Override
    public void cancelBooking(UUID bookingId) {
        Booking booking = bookingRepository
                .findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_EXISTED));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Only pending bookings can be cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.saveAndFlush(booking);

        screeningSeatRepository.releaseSeatsByBooking(bookingId.toString());
    }

    @Override
    public void confirmBookingPayment(String bookingId) {
        log.info("Confirming booking payment for: {}", bookingId);

        Booking booking = bookingRepository
                .findById(UUID.fromString(bookingId))
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_EXISTED));

        // Update booking status to PAID
        booking.setStatus(BookingStatus.PAID);
        bookingRepository.save(booking);

        screeningSeatRepository.markSeatsAsSoldByBooking(bookingId);

        // Create tickets for all bookings (both customer and staff guest bookings)
        ticketService.createTickets(UUID.fromString(bookingId));
        log.info("Tickets created for booking {}", bookingId);

        if (booking.getCustomer() != null) {
            int pointsEarned = discountService.calculateEarnedPoints(booking.getTotalAmount());
            int pointDiscounted = discountService.caculateDiscountPoints(booking.getDiscount());
            customerService.addLoyaltyPoints(booking.getCustomer().getId(), pointsEarned - pointDiscounted);
        }

        log.info("Booking {} confirmed", bookingId);
    }

    @Override
    public void refundBooking(String bookingId) {
        log.info("Refunding booking: {}", bookingId);

        Booking booking = bookingRepository
                .findById(UUID.fromString(bookingId))
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_EXISTED));

        // Update booking status to REFUNDED
        booking.setStatus(BookingStatus.REFUNDED);
        bookingRepository.save(booking);

        // Return loyalty points if customer exists
        if (booking.getCustomer() != null) {
            int pointsEarned = discountService.calculateEarnedPoints(booking.getTotalAmount());
            int pointDiscounted = discountService.caculateDiscountPoints(booking.getDiscount());
            // Subtract points that were added during confirmation
            customerService.addLoyaltyPoints(booking.getCustomer().getId(), -(pointsEarned - pointDiscounted));
            log.info(
                    "Loyalty points reversed for customer {} in booking {}",
                    booking.getCustomer().getId(),
                    bookingId);
        }

        // Release seats back to available
        screeningSeatRepository.releaseSeatsByBooking(bookingId);

        log.info("Booking {} refunded", bookingId);
    }

    private void validateScreeningSeat(Screening screening, List<String> screeningSeatIds) {
        List<ScreeningSeat> seats = screeningSeatRepository.findByScreeningId(screening.getId());

        Map<String, ScreeningSeat> seatMap = seats.stream().collect(Collectors.toMap(BaseEntity::getId, s -> s));
        List<ScreeningSeat> selectedSeats = new ArrayList<>();
        for (String seatId : screeningSeatIds) {
            ScreeningSeat seat = seatMap.get(seatId);
            if (seat == null) {
                throw new AppException(ErrorCode.SCREENING_SEAT_NOT_EXISTED);
            }
            selectedSeats.add(seat);
        }

        // gom theo hàng
        Map<String, List<ScreeningSeat>> seatsByRow =
                seats.stream().collect(Collectors.groupingBy(s -> s.getSeat().getRowChair()));

        // những hàng có ghế chọn
        Set<String> affectedRows =
                selectedSeats.stream().map(s -> s.getSeat().getRowChair()).collect(Collectors.toSet());

        for (String row : affectedRows) {
            List<ScreeningSeat> rowSeats = seatsByRow.get(row);
            rowSeats.sort(Comparator.comparingInt(s -> s.getSeat().getSeatNumber()));
            checkOrphanSeatInRow(rowSeats, screeningSeatIds);
        }
    }

    private void checkOrphanSeatInRow(List<ScreeningSeat> rowSeats, List<String> selectedSeatIds) {
        rowSeats.sort(Comparator.comparingInt(s -> s.getSeat().getSeatNumber()));

        int size = rowSeats.size();
        int[] statusMap = new int[size];
        List<Integer> selectedIndices = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            ScreeningSeat s = rowSeats.get(i);
            if (selectedSeatIds.contains(s.getId())) {
                statusMap[i] = 2; // Selected (checked)
                selectedIndices.add(i);
            } else if (s.getStatus() != ScreeningSeatStatus.AVAILABLE) {
                statusMap[i] = 1; // Occupied (disable)
            } else {
                statusMap[i] = 0; // Available (active)
            }
        }

        int checkCount = 0;
        int checkLeft = 0;
        int checkRight = 0;
        int checkEmpty = 0;

        for (int currentIndex : selectedIndices) {

            // Check giữa 2 ghế đặt
            if (getStatus(statusMap, currentIndex - 1) == 0 && getStatus(statusMap, currentIndex - 2) == 2) {
                throw new AppException(ErrorCode.ORPHAN_SEAT_VIOLATION);
            }
            if (getStatus(statusMap, currentIndex + 1) == 0 && getStatus(statusMap, currentIndex + 2) == 2) {
                throw new AppException(ErrorCode.ORPHAN_SEAT_VIOLATION);
            }

            // Check có orphan bên phải
            if (getStatus(statusMap, currentIndex + 1) == 0 && getStatus(statusMap, currentIndex + 2) == 1) {
                checkCount++;
                checkRight++;
            }

            // Check có orphan bên trái
            if (getStatus(statusMap, currentIndex - 1) == 0 && getStatus(statusMap, currentIndex - 2) == 1) {
                checkCount++;
                checkLeft++;
            }

            // Check Khoảng trống an toàn (Safe Gap / Check Empty) ---

            // Bên phải
            if (getStatus(statusMap, currentIndex + 1) == 0 && getStatus(statusMap, currentIndex + 2) == 0) {
                checkEmpty++;
            }

            // Bên trái
            if (getStatus(statusMap, currentIndex - 1) == 0 && getStatus(statusMap, currentIndex - 2) == 0) {
                checkEmpty++;
            }
        }
        if (checkCount >= 2) {
            throw new AppException(ErrorCode.ORPHAN_SEAT_VIOLATION);
        }
        if (checkEmpty > 0 && (checkLeft > 0 || checkRight > 0)) {
            throw new AppException(ErrorCode.ORPHAN_SEAT_VIOLATION);
        }
    }

    private int getStatus(int[] map, int index) {
        if (index < 0 || index >= map.length) {
            return 1; // ra khỏi dãy ghế
        }
        return map[index];
    }

    @Override
    public BookingListResponse getBookings(
            BookingStatus status,
            String customerSearch,
            String emailSearch,
            String movieSearch,
            String cinemaId,
            Pageable pageable) {
        Page<Booking> page =
                bookingRepository.findBookings(status, customerSearch, emailSearch, movieSearch, cinemaId, pageable);

        List<BookingListItemResponse> items =
                page.getContent().stream().map(this::mapToBookingListItem).collect(Collectors.toList());

        return BookingListResponse.builder()
                .bookings(items)
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .build();
    }

    private BookingListItemResponse mapToBookingListItem(Booking booking) {
        return BookingListItemResponse.builder()
                .id(booking.getId())
                .bookingCode("BK-" + booking.getId().toString().substring(0, 8).toUpperCase())
                .customerId(
                        booking.getCustomer() != null
                                ? UUID.fromString(booking.getCustomer().getId())
                                : null)
                .customerName(
                        booking.getCustomer() != null
                                ? (booking.getCustomer().getFirstName() + " "
                                                + booking.getCustomer().getLastName())
                                        .trim()
                                : "Guest")
                .email(
                        booking.getCustomer() != null
                                ? booking.getCustomer().getAccount().getEmail()
                                : "")
                .phone(booking.getCustomer() != null ? booking.getCustomer().getPhoneNumber() : "")
                .movieTitle(booking.getScreening().getMovie().getTitle())
                .roomName(booking.getScreening().getRoom().getName())
                .screeningTime(booking.getScreening().getStartTime().toString())
                .seatCount((int)
                        screeningSeatRepository.countByBooking(booking.getId().toString()))
                .totalAmount(booking.getTotalAmount())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .expiredAt(booking.getExpiredAt())
                .cinemaName(booking.getScreening().getRoom().getCinema().getName())
                .cinemaId(booking.getScreening().getRoom().getCinema().getId())
                .build();
    }
}

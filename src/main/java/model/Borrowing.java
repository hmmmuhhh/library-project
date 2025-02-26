package model;

import java.time.LocalDate;

public record Borrowing(String bookCode, int memberId, LocalDate borrowDate, LocalDate returnDate) {
}
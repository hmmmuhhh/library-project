package model;

import java.time.LocalDate;

public class Borrowing {
    private final String bookCode; // Foreign key to books(code)
    private final int memberId; // Foreign key to members(id)
    private final LocalDate borrowDate;
    private LocalDate returnDate; // Nullable

    public Borrowing(String bookCode, int memberId, LocalDate borrowDate, LocalDate returnDate) {
        this.bookCode = bookCode;
        this.memberId = memberId;
        this.borrowDate = borrowDate;
        this.returnDate = returnDate;
    }

    public String getBookCode() {
        return bookCode;
    }

    public int getMemberId() {
        return memberId;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }
}
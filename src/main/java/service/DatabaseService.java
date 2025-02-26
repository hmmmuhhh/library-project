package service;

import util.DatabaseUtil;
import model.Book;
import model.Member;
import model.Borrowing;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DatabaseService {
    private static DatabaseService instance;

    public DatabaseService() {
    }

    public static synchronized DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }

    public List<Book> getAllBooks() throws SQLException {
        List<Book> books = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM books");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                books.add(new Book(
                        rs.getString("code"),
                        rs.getString("title"),
                        rs.getString("author")
                ));
            }
        }
        return books;
    }

    public void addBook(String code, String title, String author) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO books (code, title, author) VALUES (?, ?, ?)")) {

            stmt.setString(1, code);
            stmt.setString(2, title);
            stmt.setString(3, author);
            stmt.executeUpdate();
        }
    }

    public void updateBook(String code, String title, String author) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE books SET title = ?, author = ? WHERE code = ?")) {

            stmt.setString(1, title);
            stmt.setString(2, author);
            stmt.setString(3, code);
            stmt.executeUpdate();
        }
    }

    public void deleteBook(String code) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM books WHERE code = ?")) {

            stmt.setString(1, code);
            int rowsDeleted = stmt.executeUpdate();

            if (rowsDeleted == 0) {
                throw new SQLException("No book found with code: " + code);
            }
        }
    }

    public List<Member> getAllMembers() throws SQLException {
        List<Member> members = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM members");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                members.add(new Member(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getDate("join_date").toLocalDate()
                ));
            }
        }
        return members;
    }

    public void addMember(String name, String email) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO members (name, email, join_date) VALUES (?, ?, ?)")) {

            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setDate(3, java.sql.Date.valueOf(LocalDate.now()));
            stmt.executeUpdate();
        }
    }

    public void updateMember(int id, String name, String email) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE members SET name = ?, email = ? WHERE id = ?")) {

            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setInt(3, id);
            stmt.executeUpdate();
        }
    }

    public void deleteMember(int id) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM members WHERE id = ?")) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public List<Borrowing> getAllBorrowings() throws SQLException {
        List<Borrowing> borrowings = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM borrowings");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                borrowings.add(new Borrowing(
                        rs.getString("book_code"),
                        rs.getInt("member_id"),
                        rs.getDate("borrow_date").toLocalDate(),
                        rs.getDate("return_date") != null ? rs.getDate("return_date").toLocalDate() : null
                ));
            }
        }
        return borrowings;
    }

    public void borrowBook(String bookCode, int memberId) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO borrowings (book_code, member_id, borrow_date) VALUES (?, ?, ?)")) {

            stmt.setString(1, bookCode);
            stmt.setInt(2, memberId);
            stmt.setDate(3, java.sql.Date.valueOf(LocalDate.now()));
            stmt.executeUpdate();
        }
    }

    public int returnBook(String bookCode) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE borrowings SET return_date = ? WHERE book_code = ? AND return_date IS NULL")) {

            stmt.setDate(1, java.sql.Date.valueOf(LocalDate.now()));
            stmt.setString(2, bookCode);
            return stmt.executeUpdate();
        }
    }

    public boolean memberExists(int memberId) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT 1 FROM members WHERE id = ?")) {
            stmt.setInt(1, memberId);
            return stmt.executeQuery().next();
        }
    }

    public boolean bookExists(String bookCode) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT 1 FROM books WHERE code = ?")) {
            stmt.setString(1, bookCode);
            return !stmt.executeQuery().next();
        }
    }

    public boolean isBookAlreadyBorrowed(String bookCode) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT 1 FROM borrowings WHERE book_code = ? AND return_date IS NULL")) {
            stmt.setString(1, bookCode);
            return stmt.executeQuery().next();
        }
    }
}
package model;

import java.time.LocalDate;

public class Member {
    private int id;
    private String name;
    private String email;
    private final LocalDate joinDate;

    // creating a new member (without ID)
    public Member(String name, String email, LocalDate joinDate) {
        this.name = name;
        this.email = email;
        this.joinDate = joinDate;
    }

    // retrieving a member from the database (with ID)
    public Member(int id, String name, String email, LocalDate joinDate) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.joinDate = joinDate;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getJoinDate() {
        return joinDate;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
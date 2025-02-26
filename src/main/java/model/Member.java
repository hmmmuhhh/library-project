package model;

import java.time.LocalDate;

public class Member {
    private int id;
    private final String name;
    private final String email;
    private final LocalDate joinDate;

    public Member(String name, String email, LocalDate joinDate) {
        this.name = name;
        this.email = email;
        this.joinDate = joinDate;
    }

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
}
package com.example.runnerz.user;

// only the fields we care about; extra JSON fields (address, company...) are ignored
public record User(Integer id,
                   String name,
                   String username,
                   String email,
                   String phone,
                   String website
) {
}

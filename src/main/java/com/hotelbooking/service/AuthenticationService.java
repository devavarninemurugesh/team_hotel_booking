package com.hotelbooking.service;

import com.hotelbooking.exception.BusinessRuleException;
import com.hotelbooking.model.*;
import com.hotelbooking.repository.InMemoryDataStore;
import com.hotelbooking.util.Ids;
import com.hotelbooking.util.PasswordHasher;

import java.util.Optional;

public final class AuthenticationService {
    private final InMemoryDataStore store;
    public AuthenticationService(InMemoryDataStore store) { this.store = store; }

    public Guest registerGuest(String name, String email, String phone, IdProofType proofType,
                               String proofNumber, String password) {
        validatePassword(password);
        Guest guest = new Guest(Ids.newId("USR"), name, email, phone, proofType, proofNumber);
        store.addUser(guest, hashPassword(password));
        return guest;
    }

    public Admin createAdmin(String name, String email, String phone, String password) {
        validatePassword(password);
        Admin admin = new Admin(Ids.newId("USR"), name, email, phone);
        store.addUser(admin, hashPassword(password));
        return admin;
    }

    public HotelManager createHotelManager(String name, String email, String phone, String password) {
        validatePassword(password);
        HotelManager manager = new HotelManager(Ids.newId("USR"), name, email, phone);
        store.addUser(manager, hashPassword(password));
        return manager;
    }

    public Optional<User> authenticate(String email, String password) {
        if (email == null || password == null) return Optional.empty();
        char[] passwordChars = password.toCharArray();
        try {
            return store.findUserByEmail(email.trim()).filter(user -> store.findCredentialHash(user.getId())
                    .map(hash -> PasswordHasher.verify(passwordChars, hash)).orElse(false));
        } finally {
            java.util.Arrays.fill(passwordChars, '\0');
        }
    }

    private static void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new BusinessRuleException("Password must contain at least 8 characters.");
        }
    }

    private static String hashPassword(String password) {
        char[] passwordChars = password.toCharArray();
        try {
            return PasswordHasher.hash(passwordChars);
        } finally {
            java.util.Arrays.fill(passwordChars, '\0');
        }
    }
}

package com.hotelbooking.model;

public final class Guest extends User {
    private final IdProofType idProofType;
    private final String idProofNumber;

    public Guest(String id, String name, String email, String phone,
                 IdProofType idProofType, String idProofNumber) {
        super(id, name, email, phone);
        this.idProofType = java.util.Objects.requireNonNull(idProofType, "ID proof type");
        this.idProofNumber = required(idProofNumber, "ID proof number");
    }

    public IdProofType getIdProofType() { return idProofType; }
    public String getIdProofNumber() { return idProofNumber; }
    @Override public UserRole getRole() { return UserRole.GUEST; }
}

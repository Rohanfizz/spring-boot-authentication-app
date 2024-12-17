package com.example.authenticationapp.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "user_info")
public class User {

  @Id
  private String id;

  private String firstName;
  private String lastName;

  @Column(unique = true)
  private String email;

  private String password;
  private Date createdAt;
  private Date updatedAt;
  private Date passwordUpdatedAt;
  private boolean isActive;

  @PrePersist
  public void preHandle() {
    this.password = hashString(this.password);
    this.createdAt = Calendar.getInstance().getTime();
    this.updatedAt = Calendar.getInstance().getTime();
    UUID uuid = UUID.randomUUID();
    this.id = uuid.toString();

    this.isActive = true;
  }

  public boolean isIsActive() {
    return this.isActive;
  }

  public boolean getIsActive() {
    return this.isActive;
  }

  public void setIsActive(boolean isActive) {
    this.isActive = isActive;
  }

  public static String hashString(String input) {
    try {
      // Create a MessageDigest instance for SHA-256
      MessageDigest digest = MessageDigest.getInstance("SHA-256");

      // Perform the hashing
      byte[] hashBytes = digest.digest(input.getBytes());

      // Convert the byte array to a hex string
      StringBuilder hexString = new StringBuilder();
      for (byte b : hashBytes) {
        hexString.append(String.format("%02x", b));
      }

      return hexString.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("Error initializing SHA-256 algorithm", e);
    }
  }

  public String getId() {
    return this.id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getFirstName() {
    return this.firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return this.lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getEmail() {
    return this.email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return this.password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public Date getCreatedAt() {
    return this.createdAt;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  public Date getUpdatedAt() {
    return this.updatedAt;
  }

  public void setUpdatedAt(Date updatedAt) {
    this.updatedAt = updatedAt;
  }

  public Date getPasswordUpdatedAt() {
    return this.passwordUpdatedAt;
  }

  public void setPasswordUpdatedAt(Date passwordUpdatedAt) {
    this.passwordUpdatedAt = passwordUpdatedAt;
  }
}

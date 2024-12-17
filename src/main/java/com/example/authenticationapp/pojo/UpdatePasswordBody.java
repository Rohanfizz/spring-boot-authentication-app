package com.example.authenticationapp.pojo;

public class UpdatePasswordBody {

  private String resetToken;
  private String email;
  private String newPassword;

  public String getResetToken() {
    return this.resetToken;
  }

  public void setResetToken(String resetToken) {
    this.resetToken = resetToken;
  }

  public String getEmail() {
    return this.email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getNewPassword() {
    return this.newPassword;
  }

  public void setNewPassword(String newPassword) {
    this.newPassword = newPassword;
  }

}

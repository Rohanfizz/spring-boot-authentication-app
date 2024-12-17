package com.example.authenticationapp.controllers;

import com.example.authenticationapp.Repositories.UserRepo;
import com.example.authenticationapp.models.User;
import com.example.authenticationapp.pojo.ForgotPasswordBody;
import com.example.authenticationapp.pojo.UpdatePasswordBody;
import com.example.authenticationapp.pojo.UserLoginDetails;
import com.example.authenticationapp.pojo.UserSignupDetails;
import com.example.authenticationapp.services.EmailService;
import com.example.authenticationapp.services.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

  @Autowired
  UserRepo userRepository;

  @Autowired
  EmailService emailService;

  @Autowired
  JwtService jwtService;

  @CrossOrigin(origins = "http://127.0.0.1:3000")
  @RequestMapping(value = "/signup", method = RequestMethod.POST)
  public Map<String, String> signupController(
    @RequestBody UserSignupDetails request
  ) {
    User newUser = new User();
    // Save user to database
    newUser.setFirstName(request.getFirstName());
    newUser.setLastName(request.getLastName());
    newUser.setEmail(request.getEmail());
    newUser.setPassword(request.getPassword());
    userRepository.save(newUser);
    final String token = jwtService.generateToken(newUser.getId());
    return Collections.singletonMap("token", token);
  }

  @RequestMapping(value = "/login", method = RequestMethod.GET)
  public Map<String, String> loginController(
    @RequestBody UserLoginDetails request
  ) {
    String email = request.getEmail();
    String password = request.getPassword();
    String hashedPassword = User.hashString(password);
    // Find user with email id, and match the hashed password
    User user = userRepository.findByEmail(email);

    if (user == null) {
      return Collections.singletonMap(
        "Error",
        "Username or password is wrong!"
      );
    }

    if (!user.getPassword().equals(hashedPassword)) {
      return Collections.singletonMap(
        "Error",
        "Username or password is wrong!"
      );
    }

    final String token = jwtService.generateToken(user.getId());
    return Collections.singletonMap("token", token);
  }

  // is token present?
  // is token expired?
  // extract userId from token
  // check if user exists
  @RequestMapping(value = "/refreshToken", method = RequestMethod.GET)
  public Map<String, String> refreshToken(
    @RequestHeader("Authorization") String token,
    HttpServletResponse response
  ) throws IOException {
    if (token == null || token.isEmpty() || !token.startsWith("Bearer ")) {
      response.sendError(401);
    }
    // HW: Check if this token was generated before password was update
    String userId = jwtService.validateToken(token.substring(7)); // If token is not expired, we will get userId embedded inside the token
    if (userId == null) {
      response.sendError(401);
    }

    // Check in db if user exists or not
    User user = userRepository.findById(userId).get();
    if (user == null) {
      response.sendError(401);
    }

    final String newToken = jwtService.generateToken(userId);
    return Collections.singletonMap("token", newToken);
  }

  // 1. Forgot password api
  // 	-> email
  // --------------------------------------
  // -> FindUserByEmailId
  // -> passwordResetToken
  // --> hash-> store in db
  // --> passwordResetExpires-> 10
  // -> send passwordResetToken,userId to mail
  // = Check link in your email
  @RequestMapping(value = "/forgotPassword", method = RequestMethod.POST)
  public String forgotPassword(
    @RequestBody ForgotPasswordBody body,
    HttpServletResponse response
  ) throws IOException {
    String email = body.getEmail();
    if (email == null || email.isEmpty() || !email.contains("@")) {
      response.sendError(400);
    }

    User user = userRepository.findByEmail(email);
    if (user == null) {
      return "Check your email!";
    }

    UUID uuid = UUID.randomUUID();
    String passwordResetToken = uuid.toString();

    // TODO Send password reset token to user in their email.
    emailService.sendEmail(
      email,
      "Your Password reset Token!",
      passwordResetToken
    );

    String hashedToken = jwtService.hashString(passwordResetToken);
    user.setPasswordResetToken(hashedToken);
    user.setPasswordResetExpires(
      new Date(System.currentTimeMillis() + 1000 * 60 * 2)
    );
    userRepository.save(user);

    return "Check your email! ";
  }

  @RequestMapping(value = "/updatePassword", method = RequestMethod.PATCH)
  public String updatePassword(
    @RequestBody UpdatePasswordBody body,
    HttpServletResponse response
  ) throws IOException {
    String resestToken = body.getResetToken();
    String newPassword = body.getNewPassword();
    String email = body.getEmail();
    if (resestToken == null || newPassword == null || email == null) {
      response.sendError(400);
    }
    User user = userRepository.findByEmail(email);
    if (user == null) {
      response.sendError(400);
    }
    String hashPasswordResetTokenInDB = user.getPasswordResetToken();
    Date passwordResetExpiryTimeInDB = user.getPasswordResetExpires();
    Date currentTime = new Date();
    // Is time expired?
    if (currentTime.after(passwordResetExpiryTimeInDB)) {
      response.sendError(400);
    }
    // Is reset Token valid?
    if (
      !jwtService.hashString(resestToken).equals(hashPasswordResetTokenInDB)
    ) {
      response.sendError(400);
    }
    user.setPassword(jwtService.hashString(newPassword));
    user.setPasswordUpdatedAt(currentTime);
    userRepository.save(user);
    return "Password Updated Successfully!";
  }
}

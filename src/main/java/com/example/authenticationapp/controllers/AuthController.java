package com.example.authenticationapp.controllers;

import com.example.authenticationapp.Repositories.UserRepo;
import com.example.authenticationapp.models.User;
import com.example.authenticationapp.pojo.UserLoginDetails;
import com.example.authenticationapp.pojo.UserSignupDetails;
import com.example.authenticationapp.services.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
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

  @RequestMapping(value = "/refreshToken", method = RequestMethod.GET)
  public String refreshToken(
    @RequestHeader("Authorization") String token,
    HttpServletResponse response
  ) throws java.io.IOException {
    if (token == null || token.isEmpty() || !token.startsWith("Bearer ")) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Token");
    }

    String userEmail = jwtService.validateToken(token);
    //validate if user is in the db

    // generate new token and return

    return userEmail;
  }

  @RequestMapping(
    value = "/forgotPassword/{userEmail}",
    method = RequestMethod.POST
  )
  public String forgotPassword(
    @RequestParam("userEmail") String email,
    HttpServletResponse response
  ) throws IOException {
    if (email == null || email.isEmpty() || !email.contains("@")) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Email");
    }
    // generate passworreset token
    UUID uuid = new UUID.randomUUID();
    String passwordResetToken = uuid.toString();
    User user = userRepository.findByEmail(email);
    // user.setPasswordResetToken(passwordResetToken);
    // user.setPasswordResetExpiresAt(System.currentTimeMillis() + 10 * 60 * 1000);
    return "Check your email for password reset token";
  }
  /*@Test
Step 1: Verify the token
  // Step 1.1: Is passwordResetExpiresAt done?
  // Step 1.2: Hash token provided by user and then compare with the one in DB
  // Step 2: Update the password
  // Step 3: Reset the passwordResetToken in Db. Why? SO that passwordResetToken is only used 1 time by user
  // Step 4: Generate a JWT token for the client and respond
    
} */
}

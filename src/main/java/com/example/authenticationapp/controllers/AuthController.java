package com.example.authenticationapp.controllers;

import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.example.authenticationapp.Repositories.UserRepo;
import com.example.authenticationapp.models.User;
import com.example.authenticationapp.pojo.UserLoginDetails;
import com.example.authenticationapp.pojo.UserSignupDetails;
import com.example.authenticationapp.services.JwtService;

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
}

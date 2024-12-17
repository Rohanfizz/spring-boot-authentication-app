package com.example.authenticationapp.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.authenticationapp.models.User;

@Repository
public interface UserRepo extends JpaRepository<User, String> {
  User findByEmail(String email);
}

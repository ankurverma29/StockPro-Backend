package com.stockpro.service;

import java.util.List;

import com.stockpro.dtos.AuthResponse;
import com.stockpro.dtos.AdminUpdateUserRequest;
import com.stockpro.dtos.LoginRequest;
import com.stockpro.dtos.RegisterRequest;
import com.stockpro.dtos.UpdateProfileRequest;
import com.stockpro.dtos.UserResponseDTO;

public interface AuthService {

    AuthResponse register(RegisterRequest registerRequest);

    AuthResponse login(LoginRequest loginRequest);

    AuthResponse logout();

    boolean validateToken(String token);

    AuthResponse refreshToken(String refreshToken);

    UserResponseDTO getUserById(Long userId);

    UserResponseDTO updateProfile(String email, UpdateProfileRequest updateUser);

    void changePassword(String email, String oldPassword, String newPassword);

    UserResponseDTO adminCreateUser(RegisterRequest request);

    UserResponseDTO updateUser(Long userId, AdminUpdateUserRequest request);

    List<UserResponseDTO> getAllUsers();

    void deactivateUser(Long userId);

    AuthResponse registerUser(String email, String otp);

    String initiateForgetPassword(String email);

    String verifyOtp(String email, String otp);

    String resetPassword(String email, String newPassword);

    UserResponseDTO getUserByEmail(String email);

    String verifyEmailUpdate(String email, String otp);
}

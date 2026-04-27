package com.stockpro.controller;

import java.security.Principal;
import java.util.List;

import com.stockpro.dtos.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.stockpro.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth Management", description = "Authentication, registration, profile and user management APIs.")
public class UserController {
	
	private final UserService userService;

	@PostMapping("/register-request")
	@Operation(summary = "Step 1: Request registration by providing details (sends OTP)")
	public ResponseEntity<ApiResponse<String>> registerRequest(@RequestBody RegisterRequest registerRequest) {
		String result = userService.registerRequest(registerRequest);
		return ResponseEntity.ok(ApiResponse.success(result, "OTP sent successfully"));
	}

	@PostMapping("/register")
	@Operation(summary = "Step 2: Complete registration by verifying OTP")
	public ResponseEntity<ApiResponse<AuthResponse>> registerUser(@RequestParam String email, @RequestParam String otp) {
		AuthResponse response = userService.registerUser(email, otp);
		return ResponseEntity.ok(ApiResponse.success(response, "Registration successful"));
	}

	@PostMapping("/login")
	@Operation(summary = "Login with email and password")
	public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody LoginRequest loginRequest) {
		AuthResponse response = userService.loginUser(loginRequest);
		return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
	}

	@PostMapping("/refresh")
	@Operation(summary = "Refresh access token using refresh token")
	public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@RequestBody RefreshTokenRequest request) {
		AuthResponse response = userService.refreshToken(request.getRefreshToken());
		return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully"));
	}

	@PostMapping("/forgot-password")
	@Operation(summary = "Step 1: Initiate forgot password (sends OTP)")
	public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestParam String email) {
		String result = userService.initiateForgetPassword(email);
		return ResponseEntity.ok(ApiResponse.success(result, "OTP sent successfully"));
	}

	@PostMapping("/verify-otp")
	@Operation(summary = "Step 2: Verify OTP for password reset")
	public ResponseEntity<ApiResponse<String>> verifyOtp(@RequestParam String email, @RequestParam String otp) {
		String result = userService.verifyOtp(email, otp);
		return ResponseEntity.ok(ApiResponse.success(result, "OTP verified successfully"));
	}

	@PostMapping("/reset-password")
	@Operation(summary = "Step 3: Reset password using verified OTP session")
	public ResponseEntity<ApiResponse<String>> resetPassword(@RequestParam String email, @RequestParam String newPassword) {
		String result = userService.resetPassword(email, newPassword);
		return ResponseEntity.ok(ApiResponse.success(result, "Password reset successful"));
	}

	@GetMapping("/profile")
	@SecurityRequirement(name = "bearerAuth")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Get current user profile")
	public ResponseEntity<ApiResponse<UserResponseDTO>> getProfile(Principal principal) {
		UserResponseDTO response = userService.getUserByEmail(principal.getName());
		return ResponseEntity.ok(ApiResponse.success(response, "Profile retrieved successfully"));
	}

	@PutMapping("/profile")
	@SecurityRequirement(name = "bearerAuth")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Update current user profile")
	public ResponseEntity<ApiResponse<UserResponseDTO>> updateProfile(Principal principal, @RequestBody UpdateProfileRequest updateUser) {
		UserResponseDTO response = userService.updateProfile(principal.getName(), updateUser);
		return ResponseEntity.ok(ApiResponse.success(response, "Profile updated successfully. If email was changed, please verify via OTP."));
	}

	@PostMapping("/verify-email-update")
	@SecurityRequirement(name = "bearerAuth")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Verify email update via OTP")
	public ResponseEntity<ApiResponse<String>> verifyEmailUpdate(Principal principal, @RequestParam String otp) {
		String result = userService.verifyEmailUpdate(principal.getName(), otp);
		return ResponseEntity.ok(ApiResponse.success(result, "Email updated successfully"));
	}

	@PostMapping("/change-password")
	@SecurityRequirement(name = "bearerAuth")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Change password for authenticated user")
	public ResponseEntity<ApiResponse<String>> changePassword(Principal principal, @RequestParam String oldPassword, @RequestParam String newPassword) {
		userService.changePassword(principal.getName(), oldPassword, newPassword);
		return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully"));
	}

	@PostMapping("/admin/create-user")
	@SecurityRequirement(name = "bearerAuth")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Admin: Create a new user directly")
	public ResponseEntity<ApiResponse<UserResponseDTO>> adminCreateUser(@RequestBody RegisterRequest request) {
		UserResponseDTO response = userService.adminCreateUser(request);
		return ResponseEntity.ok(ApiResponse.success(response, "User created successfully by admin"));
	}

	@GetMapping("/admin/users")
	@SecurityRequirement(name = "bearerAuth")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Admin: Get all users")
	public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getAllUsers() {
		List<UserResponseDTO> response = userService.getAllUsers();
		return ResponseEntity.ok(ApiResponse.success(response, "All users retrieved successfully"));
	}

	@PutMapping("/admin/users/{id}/deactivate")
	@SecurityRequirement(name = "bearerAuth")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Admin: Deactivate a user")
	public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable Long id) {
		userService.deactivateUser(id);
		return ResponseEntity.ok(ApiResponse.success(null, "User deactivated successfully"));
	}
}
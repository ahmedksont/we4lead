package com.we4lead.backend.controller;

import com.we4lead.backend.dto.UserProfileResponse;
import com.we4lead.backend.dto.UserUpdateRequest;
import com.we4lead.backend.entity.User;
import com.we4lead.backend.service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/users/me")
public class UserController {

    private final UserService userService;
    private final String uploadDir = "uploads";
    private final List<String> allowedTypes = Arrays.asList("image/jpeg", "image/png");
    private final long maxFileSize = 10 * 1024 * 1024; // 5 MB

    public UserController(UserService userService) {
        this.userService = userService;
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();
    }
    @GetMapping("/profile")
    public UserProfileResponse getFullProfile(@AuthenticationPrincipal Jwt jwt) {
        User user = userService.syncUser(jwt);
        String photoUrl = user.getPhotoPath() != null ? "/users/me/photo" : null;

        return buildProfileResponse(user, photoUrl);
    }

    @PutMapping
    public UserProfileResponse updateProfile(@AuthenticationPrincipal Jwt jwt,
                                             @RequestBody UserUpdateRequest request) {
        User user = userService.updateUser(jwt, request);
        String photoUrl = user.getPhotoPath() != null ? "/users/me/photo" : null;

        return buildProfileResponse(user, photoUrl);
    }

    @PostMapping("/photo")
    public UserProfileResponse uploadPhoto(@AuthenticationPrincipal Jwt jwt,
                                           @RequestParam("file") MultipartFile file) throws IOException {
        if (!allowedTypes.contains(file.getContentType())) {
            throw new RuntimeException("Invalid file type. Only JPEG and PNG allowed.");
        }
        if (file.getSize() > maxFileSize) {
            throw new RuntimeException("File too large. Max size is 5 MB.");
        }

        User user = userService.syncUser(jwt);

        if (user.getPhotoPath() != null) {
            Files.deleteIfExists(Paths.get(user.getPhotoPath()));
        }

        String filename = user.getId() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir, filename);
        Files.write(filePath, file.getBytes());
        user.setPhotoPath(filePath.toString());
        userService.saveUser(user);

        return buildProfileResponse(user, "/users/me/photo");
    }
    @GetMapping("/photo")
    public ResponseEntity<byte[]> getPhoto(@AuthenticationPrincipal Jwt jwt) throws IOException {
        User user = userService.syncUser(jwt);

        if (user.getPhotoPath() == null) {
            return ResponseEntity.notFound().build();
        }

        Path path = Paths.get(user.getPhotoPath());
        byte[] photoBytes = Files.readAllBytes(path);
        MediaType mediaType = path.toString().endsWith(".png") ? MediaType.IMAGE_PNG : MediaType.IMAGE_JPEG;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);

        return ResponseEntity.ok().headers(headers).body(photoBytes);
    }
    private UserProfileResponse buildProfileResponse(User user, String photoUrl) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getNom(),
                user.getPrenom(),
                user.getTelephone(),
                user.getRole().name(),
                photoUrl
        );
    }
}

package com.example.demoqwer.controller;

import com.example.demoqwer.model.User;
import com.example.demoqwer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class UserController {

    private final UserRepository userRepository;

    @Value("${upload.path}")
    private String uploadPath;

    @Autowired
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam(value = "file", required = false) MultipartFile file,
            Model model) {

        if (userRepository.findByUsername(username).isPresent()) {
            model.addAttribute("error", "Username is already taken.");
            return "register";
        }

        String photoPath = null;

        if (file != null && !file.isEmpty()) {
            try {
                Path uploadDir = Paths.get(uploadPath).toAbsolutePath();

                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                }

                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path filePath = uploadDir.resolve(fileName);
                file.transferTo(filePath.toFile());

                photoPath = filePath.toString();
            } catch (IOException e) {
                e.printStackTrace();
                model.addAttribute("error", "Error uploading file.");
                return "register";
            }
        }

        try {
            User user = new User();
            user.setUsername(username);
            user.setPassword(password);
            user.setPhotoPath(photoPath);

            userRepository.save(user);
            return "redirect:/login";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Registration failed.");
            return "register";
        }
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }
}

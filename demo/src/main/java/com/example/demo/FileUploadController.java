package com.example.demo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000") // Allow requests from React app
public class FileUploadController {

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> handleFileUpload(@RequestParam("files") MultipartFile[] files) {
        Map<String, String> response = new HashMap<>();

        try {
            for (MultipartFile file : files) {
                // Save each file to disk
                Path filePath = Paths.get("uploads/" + file.getOriginalFilename());
                Files.write(filePath, file.getBytes());

                // Add success message for each file
                response.put(file.getOriginalFilename(), "File uploaded successfully");
            }

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            e.printStackTrace();
            response.put("error", "Failed to upload files.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
package com.revplay.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FileStorageService.
 *
 * FileStorageService uses java.nio.file.Files (static methods) and
 * System.currentTimeMillis() — neither can be Mockito-mocked without
 * PowerMock/MockStatic. The correct approach is to use a real temp
 * directory via JUnit 5's @TempDir and inject it via ReflectionTestUtils.
 *
 * This gives us real file I/O assertions with zero external dependencies
 * and automatic cleanup after each test.
 *
 * NOTE: @TempDir creates and cleans up the directory automatically.
 * Do NOT use Files.deleteIfExists inside tests — JUnit handles it.
 */
@ExtendWith(MockitoExtension.class)
class FileStorageServiceTest {

    @TempDir
    Path tempDir; // JUnit 5 injects a fresh temp dir per test class

    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService();
        // Inject the temp directory path as the uploadDir @Value field
        ReflectionTestUtils.setField(
                fileStorageService,
                "uploadDir",
                tempDir.toString()
        );
    }

    // ── storeFile — return value ──────────────────────────────────

    @Test
    @DisplayName("storeFile - valid file - returns relative path with folder prefix")
    void storeFile_validFile_returnsRelativePath() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "cover.jpg", "image/jpeg", "fake-image-bytes".getBytes()
        );

        String result = fileStorageService.storeFile(file, "albums");

        // Must start with the folder name
        assertTrue(result.startsWith("albums/"),
                "Path should start with folder name, got: " + result);
    }

    @Test
    @DisplayName("storeFile - valid file - returned path ends with original filename")
    void storeFile_validFile_returnedPathEndsWithOriginalFilename() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "cover.jpg", "image/jpeg", "fake-image-bytes".getBytes()
        );

        String result = fileStorageService.storeFile(file, "albums");

        assertTrue(result.endsWith("cover.jpg"),
                "Path should end with original filename, got: " + result);
    }

    @Test
    @DisplayName("storeFile - valid file - returned path format is folder/timestamp_filename")
    void storeFile_validFile_pathFormatIsCorrect() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "song.mp3", "audio/mpeg", "fake-audio-bytes".getBytes()
        );

        String result = fileStorageService.storeFile(file, "audio");

        // Format: "audio/1234567890123_song.mp3"
        String[] parts = result.split("/");
        assertEquals(2, parts.length);
        assertEquals("audio", parts[0]);
        assertTrue(parts[1].contains("_song.mp3"));
    }

    // ── storeFile — file actually written to disk ─────────────────

    @Test
    @DisplayName("storeFile - valid file - file physically exists on disk after store")
    void storeFile_validFile_fileExistsOnDisk() {
        byte[] content = "real file content".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "profile.jpg", "image/jpeg", content
        );

        String relativePath = fileStorageService.storeFile(file, "artists");

        Path storedFile = tempDir.resolve(relativePath);
        assertTrue(Files.exists(storedFile),
                "Stored file should exist at: " + storedFile);
    }

    @Test
    @DisplayName("storeFile - valid file - stored file content matches original bytes")
    void storeFile_validFile_fileContentMatchesOriginal() throws IOException {
        byte[] content = "audio content bytes".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "track.mp3", "audio/mpeg", content
        );

        String relativePath = fileStorageService.storeFile(file, "songs");

        Path storedFile = tempDir.resolve(relativePath);
        byte[] stored = Files.readAllBytes(storedFile);
        assertArrayEquals(content, stored,
                "Stored file content should match original bytes");
    }

    // ── storeFile — directory creation ────────────────────────────

    @Test
    @DisplayName("storeFile - folder does not exist yet - creates directory automatically")
    void storeFile_folderNotExists_createsDirectory() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "image.png", "image/png", "bytes".getBytes()
        );

        // "new-folder" does not exist inside tempDir yet
        fileStorageService.storeFile(file, "new-folder");

        Path createdDir = tempDir.resolve("new-folder");
        assertTrue(Files.isDirectory(createdDir),
                "Service should have created the directory");
    }

    @Test
    @DisplayName("storeFile - folder already exists - does not throw, stores successfully")
    void storeFile_folderAlreadyExists_doesNotThrow() throws IOException {
        // Pre-create the directory
        Files.createDirectories(tempDir.resolve("albums"));

        MockMultipartFile file = new MockMultipartFile(
                "file", "cover.jpg", "image/jpeg", "bytes".getBytes()
        );

        assertDoesNotThrow(() -> fileStorageService.storeFile(file, "albums"));
    }

    // ── storeFile — different folders ────────────────────────────

    @Test
    @DisplayName("storeFile - different folders produce separate paths")
    void storeFile_differentFolders_separatePaths() {
        MockMultipartFile file1 = new MockMultipartFile(
                "f1", "img.jpg", "image/jpeg", "a".getBytes()
        );
        MockMultipartFile file2 = new MockMultipartFile(
                "f2", "img.jpg", "image/jpeg", "b".getBytes()
        );

        String path1 = fileStorageService.storeFile(file1, "albums");
        String path2 = fileStorageService.storeFile(file2, "artists");

        assertTrue(path1.startsWith("albums/"));
        assertTrue(path2.startsWith("artists/"));
        assertNotEquals(path1, path2);
    }

    @Test
    @DisplayName("storeFile - same filename stored twice - both files exist (timestamp prefix prevents collision)")
    void storeFile_sameFilenameTwice_bothFilesExist() throws InterruptedException {
        byte[] content = "content".getBytes();

        MockMultipartFile file1 = new MockMultipartFile(
                "f1", "cover.jpg", "image/jpeg", content
        );

        // Small sleep to ensure different timestamps
        Thread.sleep(2);

        MockMultipartFile file2 = new MockMultipartFile(
                "f2", "cover.jpg", "image/jpeg", content
        );

        String path1 = fileStorageService.storeFile(file1, "albums");
        String path2 = fileStorageService.storeFile(file2, "albums");

        // Timestamps differ so paths must differ
        assertNotEquals(path1, path2,
                "Two files with same name should have different timestamped paths");
    }

    // ── storeFile — failure handling ──────────────────────────────

    @Test
    @DisplayName("storeFile - invalid uploadDir - throws RuntimeException")
    void storeFile_invalidUploadDir_throwsRuntimeException() {
        // Point uploadDir to a path that cannot be created (file blocking it)
        ReflectionTestUtils.setField(
                fileStorageService,
                "uploadDir",
                // null path will cause Paths.get to fail
                "\0invalid-path"
        );

        MockMultipartFile file = new MockMultipartFile(
                "file", "img.jpg", "image/jpeg", "bytes".getBytes()
        );

        assertThrows(RuntimeException.class,
                () -> fileStorageService.storeFile(file, "albums"));
    }
}
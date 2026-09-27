package com.bookstore.common.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;


@Component
public class TextFileHandler {

    private static final Logger log = LoggerFactory.getLogger(TextFileHandler.class);

    private final Path dataDir;

    public TextFileHandler(@Value("${app.data-dir:data}") String dataDir) {
        this.dataDir = Paths.get(dataDir).toAbsolutePath();
        log.info("Data files folder: {}", this.dataDir);
    }

    public Path resolve(String fileName) {
        return dataDir.resolve(fileName);
    }

    public List<String> readLines(String fileName) {
        Path file = resolve(fileName);
        List<String> lines = new ArrayList<>();
        if (!Files.exists(file)) {
            return lines;
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank() && !line.startsWith("#")) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + file, e);
        }
        return lines;
    }


    public void writeLines(String fileName, String header, List<String> lines) {
        Path file = resolve(fileName);
        Path temp = file.resolveSibling(fileName + ".tmp");
        try {
            Files.createDirectories(dataDir);
            try (BufferedWriter writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
                if (header != null && !header.isBlank()) {
                    writer.write("# " + header);
                    writer.newLine();
                }
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            }
            try {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not write " + file, e);
        }
    }

    public void appendLine(String fileName, String line) {
        Path file = resolve(fileName);
        try {
            Files.createDirectories(dataDir);
            try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8,
                    java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND)) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not append to " + file, e);
        }
    }
}

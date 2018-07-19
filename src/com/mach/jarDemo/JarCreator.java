package com.mach.jarDemo;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.Cleanup;

public class JarCreator {
    private final static Logger LOG = Logger.getLogger(JarCreator.class);
    private final static String TARGET = "target";
    private final static String DEMO_EXTRACTED = "resources/demoTextExtracted.json";
    private final static String DEMO = "resources/demoText.json";

    private final String name;

    public JarCreator(String name) {
        this.name = name;
    }

    public Path create() {
        try {
            Path jsonPath = createTempFile();
            Path jarPath = Paths.get(TARGET , name);
            Files.createDirectories(jarPath.getParent());
            @Cleanup
            FileOutputStream fStream = new FileOutputStream(jarPath.toFile());
            @Cleanup
            JarOutputStream out = new JarOutputStream(fStream, new Manifest());

            JarEntry je = new JarEntry(jsonPath.toFile().getName());
            // add archive entry
            out.putNextEntry(je);

            // write file to archive
            @Cleanup
            BufferedReader br = Files.newBufferedReader(jsonPath);
            String line = "";
            while ((line = br.readLine()) != null) {
                out.write(line.getBytes());
            }
            return jarPath;
        } catch (IOException e) {
            LOG.error("Could not create the text file to put in the jar.. exiting", e);
        }
        return null;
    }

    private Path createTempFile()
            throws IOException {
        String filePath = DEMO;
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        String content = gson.toJson(new Person("John", "Doe", "19/06/1994", "0467245677"));
        Path path = Files.write(Paths.get(filePath), content.getBytes(), StandardOpenOption.CREATE);
        return path;
    }

    public static void main(String[] args) {
        JarCreator jc = new JarCreator("sample.jar");
        Path jarPath = jc.create();

        try (JarFile jarFile = new JarFile(jarPath.toFile())) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().equals("demoText.json")) {
                    @Cleanup
                    BufferedReader br = new BufferedReader(new InputStreamReader(jarFile.getInputStream(entry)));
                    @Cleanup
                    FileOutputStream fos = new FileOutputStream(new File(DEMO_EXTRACTED));
                    String line = "";
                    while ((line = br.readLine()) != null) {
                        fos.write(line.getBytes());
                    }
                }
            }
            assertTrue(Files.exists(Paths.get(DEMO_EXTRACTED)));
            LOG.info("Successfully created jar, added a json file to it, extracted the json file and wrote to a specified location");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}

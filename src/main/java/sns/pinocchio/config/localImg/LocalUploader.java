package sns.pinocchio.config.localImg;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Profile("!test")
@Component
@RequiredArgsConstructor
public class LocalUploader {

    public String uploadFile(MultipartFile file, String dirName) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String uniqueFileName = UUID.randomUUID() + "_" + originalFilename;

        Path uploadPath = Paths.get("src/main/resources/static/" + dirName);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(uniqueFileName);

        Files.write(filePath, file.getBytes());

        String fileUrl = "src/main/static/" + dirName + "/" + uniqueFileName;

        return fileUrl;
    }
}

package com.alibou.book.file;

import com.alibou.book.book.Book;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {
    @Value("${application.file.upload.photos-output-path}")
    private String fileUploadPath;
//    private Logger log =  Logger.getLogger("FileStorageService");
    public String saveFile(
           @Nonnull MultipartFile sourceFile,
           @Nonnull  Integer userId){
        final String fileUploadSubPath = "users" + File.separator + userId;
        return uploadFile(sourceFile,fileUploadPath);
    }

    private String uploadFile(
            @Nonnull MultipartFile sourceFile,
            @Nonnull String fileUploadPath) {
        final String finalUploadPath = fileUploadPath + File.separator + fileUploadPath;
        File targetFolder = new File(finalUploadPath);
        if(!targetFolder.exists()){
            boolean folderCreated = targetFolder.mkdir();
            if(!folderCreated){
               log.warn("Failed to create the target folder");
               return null;
            }
        }
        final String fileExtension = getFileExtension(sourceFile.getOriginalFilename());
        String targetFilePath = finalUploadPath + File.separator
                + System.currentTimeMillis() + "." + fileExtension;
        Path targetPath = Paths.get(targetFilePath);
        try{
            Files.write(targetPath,sourceFile.getBytes());
            log.info("File saved to " + targetFilePath);
            return targetFilePath;
        }catch (IOException e){
            log.error("File was not saved",e);
        }
        return null;
    }

    private String getFileExtension(String originalFilename) {
        if(originalFilename == null || originalFilename == ""){
            return "";
        }
        int lastDotIndex = originalFilename.lastIndexOf(".");
        if(lastDotIndex == -1){
            return "";
        }
        return originalFilename.substring(lastDotIndex+1).toLowerCase();
    }
}

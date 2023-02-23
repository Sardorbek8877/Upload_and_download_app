package uz.bek.appuploaddownload.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.ParameterResolutionDelegate;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import uz.bek.appuploaddownload.entity.Attachment;
import uz.bek.appuploaddownload.entity.AttachmentContent;
import uz.bek.appuploaddownload.repository.AttachmentContentRepository;
import uz.bek.appuploaddownload.repository.AttachmentRepository;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/attachment")
public class AttachmentController {

    @Autowired
    AttachmentRepository attachmentRepository;
    @Autowired
    AttachmentContentRepository attachmentContentRepository;

    private static final String uploadDirectory="yuklanganlar";

    //UPLOAD FILE TO DATABASE
    @PostMapping("/upload")
    public String uploadFile(MultipartHttpServletRequest request) throws IOException {

        //INFO FROM FILE
        Iterator<String> fileNames = request.getFileNames();
        MultipartFile file = request.getFile(fileNames.next());
        if (file != null){
            String originalFilename = file.getOriginalFilename();
            long size = file.getSize();
            String contentType = file.getContentType();
            Attachment attachment = new Attachment();
            attachment.setFileOriginalName(originalFilename);
            attachment.setSize(size);
            attachment.setContentType(contentType);
            Attachment savedAttachment = attachmentRepository.save(attachment);

            //SAVE CONTENT(BYTE[]) FROM FILE
            AttachmentContent attachmentContent = new AttachmentContent();
            attachmentContent.setMainContent(file.getBytes());
            attachmentContent.setAttachment(savedAttachment);
            attachmentContentRepository.save(attachmentContent);
            return "File saved. ID is " + savedAttachment.getId();
        }
        return "File not found";
    }

    //UPLOAD FILE TO SYSTEM
    @PostMapping("/uploadSystem")
    public String uploadFileToFileSystem(MultipartHttpServletRequest request) throws IOException {
        Iterator<String> fileNames = request.getFileNames();
        MultipartFile file = request.getFile(fileNames.next());
        if (file != null){
            String originalName = file.getOriginalFilename();

            Attachment attachment = new Attachment();
            attachment.setFileOriginalName(originalName);
            attachment.setSize(file.getSize());
            attachment.setContentType(file.getContentType());

            String[] split = originalName.split("//.");
            String name = UUID.randomUUID().toString() + "." + split[split.length-1];
            attachment.setName(name);
            attachmentRepository.save(attachment);
            Path path = Paths.get(uploadDirectory + "/" + name);
            Files.copy(file.getInputStream(), path);
            return "Fayl saqlandi, id: " + attachment.getId();
        }
        return "Saqlanmadi";
    }

    //DOWNLOAD FILE
    @GetMapping("/getFile/{id}")
    public void getFile(@PathVariable Integer id, HttpServletResponse response) throws IOException {
        Optional<Attachment> optionalAttachment = attachmentRepository.findById(id);
        if (optionalAttachment.isPresent()){
            Attachment attachment = optionalAttachment.get();

            Optional<AttachmentContent> contentOptional= attachmentContentRepository.findByAttachmentId(id);
            if (contentOptional.isPresent()){
                AttachmentContent attachmentContent = contentOptional.get();

                response.setHeader("Content-Disposition",
                        "attachment; filename=\"" + attachment.getFileOriginalName() + "\"");

                response.setContentType(attachment.getContentType());

                FileCopyUtils.copy(attachmentContent.getMainContent(), response.getOutputStream());
            }
        }
    }

    //GET FILES FROM SYSTEM
    @GetMapping("/getFileFromSystem/{id}")
    public void getFileFromSystem(@PathVariable Integer id, HttpServletResponse response) throws IOException {
        Optional<Attachment> optionalAttachment = attachmentRepository.findById(id);
        if (optionalAttachment.isPresent()){
            Attachment attachment = optionalAttachment.get();
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + attachment.getFileOriginalName() + "\"");

            response.setContentType(attachment.getContentType());

            FileInputStream fileInputStream = new FileInputStream(uploadDirectory + "/" + attachment.getName());
            FileCopyUtils.copy(fileInputStream, response.getOutputStream());

        }
    }

}




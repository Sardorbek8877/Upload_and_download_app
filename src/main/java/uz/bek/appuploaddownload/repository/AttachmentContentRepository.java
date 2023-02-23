package uz.bek.appuploaddownload.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.bek.appuploaddownload.entity.AttachmentContent;

import java.util.Optional;

public interface AttachmentContentRepository extends JpaRepository<AttachmentContent, Integer> {


    Optional<AttachmentContent> findByAttachmentId(Integer integer);
}

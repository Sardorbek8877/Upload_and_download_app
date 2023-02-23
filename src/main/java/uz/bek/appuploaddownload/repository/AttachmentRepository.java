package uz.bek.appuploaddownload.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.bek.appuploaddownload.entity.Attachment;

public interface AttachmentRepository extends JpaRepository<Attachment, Integer> {

}

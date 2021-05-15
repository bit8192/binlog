package cn.bincker.web.blog.base.repository;

import cn.bincker.web.blog.base.entity.UploadFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IUploadFileRepository extends JpaRepository<UploadFile, Long> {
}

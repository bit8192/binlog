package cn.bincker.web.blog.base.repository;

import cn.bincker.web.blog.base.entity.UploadFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface IUploadFileRepository extends JpaRepository<UploadFile, Long> {
}

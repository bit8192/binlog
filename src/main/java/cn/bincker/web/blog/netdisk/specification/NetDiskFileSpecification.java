package cn.bincker.web.blog.netdisk.specification;

import cn.bincker.web.blog.netdisk.entity.NetDiskFile;
import org.springframework.data.jpa.domain.Specification;

public class NetDiskFileSpecification {
    /**
     * 只查询根目录文件, 即父级为null
     */
    public static Specification<NetDiskFile> selectRoot(){
        return (root, query, criteriaBuilder) -> criteriaBuilder.isNull(root.get("parent"));
    }

    public static Specification<NetDiskFile> possessorId(Long id){
        return (root, query, builder) -> builder.equal(root.get("possessor").get("id"), id);
    }

    public static Specification<NetDiskFile> parentId(Long id){
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("parent").get("id"), id);
    }

    public static Specification<NetDiskFile> isDirectory(boolean isDirectory){
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("isDirectory"), isDirectory);
    }

    public static Specification<NetDiskFile> mediaTypeLike(String mediaType){
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("mediaType"), "%" + mediaType + "%");
    }

    public static Specification<NetDiskFile> suffix(String suffix){
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("suffix"), "%" + suffix.toLowerCase() + "%");
    }
}

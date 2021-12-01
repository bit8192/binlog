package cn.bincker.web.blog.netdisk.repository;

import cn.bincker.web.blog.base.vo.EntityLongValueVo;
import cn.bincker.web.blog.netdisk.entity.NetDiskFile;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface INetDiskFileRepository extends JpaRepository<NetDiskFile, Long>, JpaSpecificationExecutor<NetDiskFile> {

    @Query("""
    from NetDiskFile f
    where f.parent.id = :id
    """)
    List<NetDiskFile> findByParent(@Param("id") Long id);

    List<NetDiskFile> findAllByPossessorId(Long id, Sort sort);

    @Query("""
    from NetDiskFile f
    where
        f.parent.id = :id
    order by
        f.isDirectory desc,
        f.createdDate
    """)
    List<NetDiskFile> listVoByParentId(@Param("id") Long id);

    @Query("""
    select readableUserList.id
    from NetDiskFile f
    join f.readableUserList readableUserList
    where f.id = :id
    """)
    List<Long> getReadableUserIds(@Param("id") Long id);

    @Query("""
    select writableUserList.id
    from NetDiskFile f
    join f.writableUserList writableUserList
    where f.id = :id
    """)
    List<Long> getWritableUserIds(@Param("id") Long id);

    @Query("""
    select
        f.id as id,
        (select count(children.id) from NetDiskFile children where children.parent.id = f.id) as value
    from NetDiskFile f
    where f.id in (:ids)
    """)
    List<EntityLongValueVo> findAllChildrenNum(List<Long> ids);

    Optional<NetDiskFile> findByPath(String path);
}

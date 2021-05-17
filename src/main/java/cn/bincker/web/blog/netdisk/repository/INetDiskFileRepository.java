package cn.bincker.web.blog.netdisk.repository;

import cn.bincker.web.blog.netdisk.entity.NetDiskFile;
import cn.bincker.web.blog.netdisk.service.vo.NetDiskFileVo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface INetDiskFileRepository extends JpaRepository<NetDiskFile, Long> {
    String NET_DISK_FILE_VO_FIELDS = """
            new cn.bincker.web.blog.netdisk.service.vo.NetDiskFileVo(
                f,
                (select count(f1.id) from NetDiskFile f1 where f1.parent.id = f.id)
            )
            """;

    @Query("""
    from NetDiskFile f
    where f.parent.id = :id
    """)
    List<NetDiskFile> findByParent(@Param("id") Long id);

    @Query("""
    select
    """ + NET_DISK_FILE_VO_FIELDS + """
    from NetDiskFile f
    left join f.uploadFile uploadFile
    left join f.possessor possessor
    left join f.createdUser createdUser
    left join f.lastModifiedUser lastModifiedUser
    where
        f.parent.id is null
        and f.possessor.id = :uid
    """)
    List<NetDiskFileVo> listUserRootVo(@Param("uid") Long uid);

    @Query("""
    select
    """ + NET_DISK_FILE_VO_FIELDS + """
    from NetDiskFile f
    left join f.uploadFile uploadFile
    left join f.possessor possessor
    left join f.createdUser createdUser
    left join f.lastModifiedUser lastModifiedUser
    where
        f.parent.id = :id
    """)
    List<NetDiskFileVo> listVoByParentId(@Param("id") Long id);

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
    """ + NET_DISK_FILE_VO_FIELDS + """
    from NetDiskFile f
    join f.uploadFile uploadFile
    join f.possessor possessor
    join f.createdUser createdUser
    join f.lastModifiedUser lastModifiedUser
    where f.id = :id
    """)
    NetDiskFileVo getVo(@Param("id") Long id);
}

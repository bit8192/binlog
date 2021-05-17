package cn.bincker.web.blog.netdisk.repository;

import cn.bincker.web.blog.netdisk.entity.NetDiskFile;
import cn.bincker.web.blog.netdisk.vo.NetDiskFileListVo;
import cn.bincker.web.blog.netdisk.vo.NetDiskFileVo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface INetDiskFileRepository extends JpaRepository<NetDiskFile, Long> {
    String NET_DISK_FILE_VO_FIELDS = """
            new cn.bincker.web.blog.netdisk.vo.NetDiskFileVo(
                f,
                (select count(f1.id) from NetDiskFile f1 where f1.parent.id = f.id)
            )
            """;

    String NET_DISK_FILE_LIST_VO_FIELDS = """
            new cn.bincker.web.blog.netdisk.vo.NetDiskFileListVo(
                f.id,
                f.name,
                f.isDirectory,
                uploadFile.size,
                f.createdDate,
                f.lastModifiedDate
            )
            """;

    @Query("""
    from NetDiskFile f
    where f.parent.id = :id
    """)
    List<NetDiskFile> findByParent(@Param("id") Long id);

    @Query("""
    select
    """ + NET_DISK_FILE_LIST_VO_FIELDS + """
    from NetDiskFile f
    left join f.uploadFile uploadFile
    where
        f.parent.id is null
        and f.possessor.id = :uid
    """)
    List<NetDiskFileListVo> listUserRootVo(@Param("uid") Long uid);

    @Query("""
    select
    """ + NET_DISK_FILE_LIST_VO_FIELDS + """
    from NetDiskFile f
    left join f.uploadFile uploadFile
    where
        f.parent.id = :id
    """)
    List<NetDiskFileListVo> listVoByParentId(@Param("id") Long id);

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
    left join f.uploadFile uploadFile
    join f.possessor possessor
    join f.createdUser createdUser
    join f.lastModifiedUser lastModifiedUser
    where f.id = :id
    """)
    NetDiskFileVo getVo(@Param("id") Long id);

    @Query("""
    select
    """ + NET_DISK_FILE_LIST_VO_FIELDS + """
    from NetDiskFile f
    left join f.uploadFile uploadFile
    where f.id in (:ids)
    """)
    List<NetDiskFileListVo> findAllVoById(@Param("ids") Long[] ids);

    @Query("""
    select 
    """ + NET_DISK_FILE_VO_FIELDS + """
    from NetDiskFile f
    left join f.uploadFile
    join f.possessor
    join f.createdUser
    join f.lastModifiedUser
    left join f.readableUserList
    left join f.writableUserList
    where f.id = :id
    group by f.id
    """)
    Optional<NetDiskFileVo> findVoById(@Param("id") Long id);
}

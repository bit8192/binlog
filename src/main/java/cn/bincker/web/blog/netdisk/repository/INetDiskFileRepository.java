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
                f.id,
                f.name,
                f.isDirectory
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
    where
        f.parent.id is null
        and f.possessor.id = :uid
    """)
    List<NetDiskFileVo> listUserRootVo(@Param("uid") Long uid);

    @Query("""
    select
    """ + NET_DISK_FILE_VO_FIELDS + """
    from NetDiskFile f
    where 
        f.parent.id = :id
    """)
    List<NetDiskFileVo> listVoByParentId(@Param("id") Long id);
}

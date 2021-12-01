package cn.bincker.web.blog.expression.service.impl;

import cn.bincker.web.blog.base.config.UserAuditingListener;
import cn.bincker.web.blog.base.config.SystemFileProperties;
import cn.bincker.web.blog.base.exception.BadRequestException;
import cn.bincker.web.blog.base.exception.NotFoundException;
import cn.bincker.web.blog.base.exception.SystemException;
import cn.bincker.web.blog.base.exception.UnauthorizedException;
import cn.bincker.web.blog.base.service.ISystemFileFactory;
import cn.bincker.web.blog.base.vo.BaseUserVo;
import cn.bincker.web.blog.base.vo.ValueVo;
import cn.bincker.web.blog.expression.dto.ExpressionDto;
import cn.bincker.web.blog.expression.entity.Expression;
import cn.bincker.web.blog.expression.entity.ExpressionAgree;
import cn.bincker.web.blog.expression.entity.ExpressionTag;
import cn.bincker.web.blog.expression.repository.IExpressionAgreeRepository;
import cn.bincker.web.blog.expression.repository.IExpressionRepository;
import cn.bincker.web.blog.expression.repository.IExpressionTagRepository;
import cn.bincker.web.blog.expression.service.IExpressionService;
import cn.bincker.web.blog.expression.specification.ExpressionSpecification;
import cn.bincker.web.blog.expression.vo.ExpressionTagVo;
import cn.bincker.web.blog.expression.vo.ExpressionVo;
import cn.bincker.web.blog.material.constant.SynchronizedPrefixConstant;
import cn.bincker.web.blog.utils.DigestUtils;
import cn.bincker.web.blog.utils.FileUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExpressionServiceImpl implements IExpressionService {
    public static final long EXPRESSION_FILE_MAX_SIZE = 200 * 1024L;
    private final IExpressionRepository expressionRepository;
    private final IExpressionAgreeRepository expressionAgreeRepository;
    private final IExpressionTagRepository expressionTagRepository;
    private final ISystemFileFactory systemFileFactory;
    private final SystemFileProperties systemFileProperties;
    private final UserAuditingListener userAuditingListener;

    public ExpressionServiceImpl(IExpressionRepository expressionRepository, IExpressionAgreeRepository expressionAgreeRepository, IExpressionTagRepository expressionTagRepository, ISystemFileFactory systemFileFactory, SystemFileProperties systemFileProperties, UserAuditingListener userAuditingListener) {
        this.expressionRepository = expressionRepository;
        this.expressionAgreeRepository = expressionAgreeRepository;
        this.expressionTagRepository = expressionTagRepository;
        this.systemFileFactory = systemFileFactory;
        this.systemFileProperties = systemFileProperties;
        this.userAuditingListener = userAuditingListener;
    }

    @Override
    public Page<ExpressionVo> page(String keyword, Collection<Long> tagIds, Pageable pageable) {
        Specification<Expression> query = null;
        if(StringUtils.hasText(keyword)){
            query = ExpressionSpecification.titleLike(keyword);
        }
        if(!tagIds.isEmpty()){
            if(query == null){
                query = ExpressionSpecification.tagIdIn(tagIds);
            }else{
                query = query.and(ExpressionSpecification.tagIdIn(tagIds));
            }
        }

        var page = expressionRepository.findAll(query, pageable).map(ExpressionVo::new);

        var currentUserOptional = userAuditingListener.getCurrentAuditor();
        if(currentUserOptional.isPresent()){
            var agreedIdSet = expressionAgreeRepository.findByCreatedUserAndExpressionIdIn(
                    currentUserOptional.get(),
                    page.getContent().stream().map(ExpressionVo::getId).collect(Collectors.toSet())
            ).stream().map(a->a.getExpression().getId()).collect(Collectors.toSet());
            page.forEach(e->e.setIsAgreed(agreedIdSet.contains(e.getId())));
        }

        return page;
    }

    @Override
    public Expression getByTitle(String title) {
        return expressionRepository.findByTitle(title).orElseThrow(NotFoundException::new);
    }

    @Override
    @Transactional
    public List<ExpressionVo> upload(MultiValueMap<String, MultipartFile> fileMap, Collection<ExpressionDto> expressionInfos) {
        var result = new ArrayList<ExpressionVo>(expressionInfos.size());
        var currentUser = userAuditingListener.getCurrentAuditor().orElseThrow(UnauthorizedException::new);
        var allTagMap = expressionTagRepository.findAllById(
                expressionInfos.stream().map(ExpressionDto::getTagIds)
                        .reduce(new HashSet<>(), (c, i)->{
                            c.addAll(i);
                            return c;
                        })
        ).stream().collect(Collectors.toUnmodifiableMap(ExpressionTag::getId, t->t));
        var expressionPath = systemFileFactory.fromPath(systemFileProperties.getExpressionStoreType(), systemFileProperties.getExpressionStoreLocation());
        if(!expressionPath.exists() && !expressionPath.mkdirs()) throw new SystemException("创建目录失败：path=" + expressionPath);
        for (ExpressionDto expressionInfo : expressionInfos) {
            var multipartFiles = fileMap.get(expressionInfo.getFileName());
            if(multipartFiles == null || multipartFiles.isEmpty())
                throw new BadRequestException("没有文件:" + expressionInfo.getFileName(), "无效请求");
            var multipartFile = multipartFiles.get(0);
            if(multipartFile.getSize() > EXPRESSION_FILE_MAX_SIZE) throw new BadRequestException("图片过大: size=" + multipartFile.getSize(),"表情不得大于" + EXPRESSION_FILE_MAX_SIZE/1024 + "kb");
            var systemFile = systemFileFactory.fromPath(systemFileProperties.getExpressionStoreType(), expressionPath.getPath(), expressionInfo.getTitle() + FileUtils.getFileSuffix(expressionInfo.getFileName()));
            if(systemFile.exists()) throw new BadRequestException("表情已存在:" + systemFile.getPath(), "表情已存在");
            var expression = new Expression();
            expression.setPath(systemFile.getPath());
            expression.setTags(expressionInfo.getTagIds().stream().map(allTagMap::get).collect(Collectors.toSet()));
            expression.setTitle(expressionInfo.getTitle());
            expression.setAgreedNum(0L);
            //先计算sha256，并判断是否存在
            try(var in = multipartFile.getInputStream()){
                expression.setSha256(DigestUtils.sha256Hex(in));
            } catch (IOException | NoSuchAlgorithmException e) {
                throw new SystemException(e);
            }
            //如果有相同的那么抛出异常提示
            var likeSha256Expression = expressionRepository.findBySha256(expression.getSha256());
            if(likeSha256Expression.isPresent()){
                throw new BadRequestException("表情sha256值重复: sha256=" + expression.getSha256(), "你上传的表情[" + expression.getTitle() + "]与[" + likeSha256Expression.get().getTitle() + "]重复");
            }
            //写出到文件
            try(var in = multipartFile.getInputStream(); var out = systemFile.getOutputStream()){
                in.transferTo(out);
            } catch (IOException e) {
                throw new SystemException(e);
            }
            expressionRepository.save(expression);
            var vo = new ExpressionVo(expression);
            vo.setTags(expression.getTags().stream().map(ExpressionTagVo::new).collect(Collectors.toSet()));
            vo.setCreatedUser(new BaseUserVo(currentUser));
            result.add(vo);
        }
        return result;
    }

    @Override
    public Expression getById(Long id) {
        return expressionRepository.findById(id).orElseThrow(NotFoundException::new);
    }

    @Override
    public ValueVo<Boolean> toggleAgree(Long id) {
        var target = expressionRepository.findById(id).orElseThrow(NotFoundException::new);
        var agreeOptional = expressionAgreeRepository.findByExpressionId(id);
        if(agreeOptional.isPresent()){
            expressionAgreeRepository.delete(agreeOptional.get());
        }else{
            var agree = new ExpressionAgree();
            agree.setExpression(target);
            expressionAgreeRepository.save(agree);
        }
        synchronized ((SynchronizedPrefixConstant.TOGGLE_EXPRESSION_AGREE + id).intern()){
            target = expressionRepository.findById(id).orElseThrow(SystemException::new);
            target.setAgreedNum(expressionAgreeRepository.countByExpression(target));
            expressionRepository.save(target);
        }
        return new ValueVo<>(agreeOptional.isEmpty());
    }
}

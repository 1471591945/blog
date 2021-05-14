package com.lzp.service;

import com.lzp.NotFoundException;
import com.lzp.dao.BlogRepository;
import com.lzp.po.Blog;
import com.lzp.po.Type;
import com.lzp.util.MarkdownUtils;
import com.lzp.util.MyBeanUtils;
import com.lzp.vo.BlogQuery;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.criteria.*;
import java.util.*;

/**
 * @Author LZP
 * @Date 2021/5/5 16:51
 * @Version 1.0
 */
@Service
public class BlogServiceImpl implements BlogService{

    @Autowired
    private BlogRepository blogRepository;

    @Override
    public Blog getBlog(Long id) {
        return blogRepository.findById(id).orElse(null);
    }

    @Transactional
    @Override
    public Blog getAndConvert(Long id) {
        Blog blog = blogRepository.findById(id).orElse(null);
        if (blog == null) {
            throw new NotFoundException("该博客不存在");
        }
        Blog b = new Blog();
        // 拷贝属性，这样避免影响数据库中的数据
        BeanUtils.copyProperties(blog, b);
        String content = b.getContent();
        b.setContent(MarkdownUtils.markdownToHtmlExtensions(content));
        // 实现浏览次数增加
        blogRepository.updateViews(id);
        return b;
    }

    @Override
    public Page<Blog> listBlog(Pageable pageable, BlogQuery blog) {
        return blogRepository.findAll(new Specification<Blog>() {
            @Override
            public Predicate toPredicate(Root<Blog> root, CriteriaQuery<?> cq, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                if (blog.getTitle() != null && !"".equals(blog.getTitle())) {
                    predicates.add(cb.like(root.<String>get("title"), "%" + blog.getTitle() + "%"));
                }
                if (blog.getTypeId() != null) {
                    predicates.add(cb.equal(root.<Type>get("type").get("id"), blog.getTypeId()));
                }
                if (blog.isRecommend()) {
                    predicates.add(cb.equal(root.<Boolean>get("recommend"), blog.isRecommend()));
                }
                cq.where(predicates.toArray(new Predicate[predicates.size()]));
                return null;
            }
        }, pageable);
    }

    @Override
    public Page<Blog> listBlog(Pageable pageable) {
        return blogRepository.findAll(pageable);
    }

    @Override
    public Page<Blog> listBlog(Long tagId, Pageable pageable) {
        return blogRepository.findAll(new Specification<Blog>() {
            @Override
            public Predicate toPredicate(Root<Blog> root, CriteriaQuery<?> cq, CriteriaBuilder cb) {
                Join join = root.join("tags");
                return cb.equal(join.get("id"), tagId);
            }
        }, pageable);
    }

    @Override
    public Page<Blog> listBlog(String query, Pageable pageable) {
        return blogRepository.findByQuery(query, pageable);
    }

    @Override
    public List<Blog> listRecommendBlogTop(Integer size) {
        // 按时间降序排
        Sort sort = Sort.by(Sort.Direction.DESC, "updateTime");
        Pageable pageable = PageRequest.of(0, size, sort);
        return blogRepository.findTop(pageable);
    }

    /**
     * 归档
     * @return
     */
    @Override
    public Map<String, List<Blog>> archiveBlog() {
        // 从数据库中查询出最近更新的所有年份
        List<String> years = blogRepository.findGroupYear();
        // 按时间递增排序
        Map<String, List<Blog>> map = new TreeMap<>();
        for (String year : years) {
            // 通过最近更新的年份去查询博客列表
            map.put(year, blogRepository.findByYear(year));
        }
        return map;
    }

    @Override
    public Long countBlog() {
        // count()方法返回的是blog表里面的所有记录数
        return blogRepository.count();
    }

    /**
     * 新增博客
     * @param blog
     * @return
     */
    @Transactional
    @Override
    public Blog saveBlog(Blog blog) {
        blog.setCreateTime(new Date());
        blog.setUpdateTime(new Date());
        // 浏览次数，一开始默认为0
        blog.setViews(0);
        return blogRepository.save(blog);
    }

    /**
     * 更新博客
     * @param id 待更新博客id
     * @param blog 博客对象
     * @return
     */
    @Transactional
    @Override
    public Blog updateBlog(Long id, Blog blog) {
        // 去数据库查参数id对应的博客信息，后面会用到
        Blog b = blogRepository.findById(id).orElse(null);
        if (b == null) {
            throw new NotFoundException("该博客不存在");
        }
        // 修改更新时间
        blog.setUpdateTime(new Date());
        // 修改时，博客创建时间和访问次数不能变，所以需要忽略这两个属性
        // 将前端修改好的blog对象中的属性复制到查询出来的b对象中
        // 过滤掉blog中属性值为空的属性
        BeanUtils.copyProperties(blog, b, MyBeanUtils.getNullPropertyNames(blog));
        return blogRepository.save(b);
    }

    @Transactional
    @Override
    public void deleteBlog(Long id) {
        blogRepository.deleteById(id);
    }
}

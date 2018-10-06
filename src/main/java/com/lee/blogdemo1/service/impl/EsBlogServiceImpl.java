package com.lee.blogdemo1.service.impl;

import com.lee.blogdemo1.VO.TagVO;
import com.lee.blogdemo1.entity.User;
import com.lee.blogdemo1.entity.es.EsBlog;
import com.lee.blogdemo1.repository.es.EsBlogRepository;
import com.lee.blogdemo1.service.EsBlogService;
import com.lee.blogdemo1.service.UserService;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.search.SearchParseException;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ResultsExtractor;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.search.aggregations.AggregationBuilders.terms;

@Service
public class EsBlogServiceImpl implements EsBlogService {

    @Autowired
    private EsBlogRepository esBlogRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private UserService userService;

    @Override
    public Page<EsBlog> listHotestEsBlogs(String keyword, Pageable pageable) throws SearchParseException {
        Sort sort = new Sort(Sort.Direction.DESC, "readSize", "commentSize", "voteSize", "createTime");
        //若BlogController在sort前抛异常则需要在此重新排序
        if (pageable.getSort().isUnsorted()) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        }

        return esBlogRepository.findByTitleContainingOrSummaryContainingOrContentContainingOrTagsContaining(
                keyword, keyword, keyword, keyword, pageable);
    }

    @Override
    public List<EsBlog> listTop5HotestEsBlogs() {
        return listHotestEsBlogs("", new PageRequest(0, 5)).getContent();
    }

    @Override
    public Page<EsBlog> listNewestEsBlogs(String keyword, Pageable pageable) throws SearchParseException {  //TODO 抛异常???
        Page<EsBlog> pages = null;
        //若BlogController在sort前抛异常则需要在此重新排序
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        if (pageable.getSort().isUnsorted()) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        }
        pages = esBlogRepository.findByTitleContainingOrSummaryContainingOrContentContainingOrTagsContaining(
                keyword, keyword, keyword, keyword, pageable);
        return pages;
    }

    @Override
    public List<EsBlog> listTop5NewestEsBlogs() {
        return listNewestEsBlogs("", new PageRequest(0, 5)).getContent();
    }

    @Override
    public Page<EsBlog> listEsBlogs(Pageable pageable) {
        return esBlogRepository.findAll(pageable);
    }

    @Override
    public List<TagVO> listTop30Tags() {
        List<TagVO> list = new ArrayList<>();// 存储排序后的首页标签对象

        // 构造查询条件对象，使用matchAllQuery()完成查询
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(matchAllQuery())
                .withSearchType(SearchType.QUERY_THEN_FETCH)  //默认的搜索方式
                .withIndices("blog")  //索引名称
                .withTypes("blog")    //type名称
                //构造聚合函数
                .addAggregation(terms("tags")  //聚合名称指定为"tags",term是代表完全匹配
                                .field("tags")     //指定esBlog类中需要聚合的字段"tags"
                                .order(Terms.Order.count(false)).size(30)) //聚合后根据根据文档数量排序(true:正序,false：倒序)并取前30条数据
                .build();

//        Aggregations aggregations = elasticsearchTemplate.query(searchQuery,
//                new ResultsExtractor<Aggregations>() {
//                    @Override
//                    public Aggregations extract(SearchResponse response) {
//                        return response.getAggregations();
//                    }
//                });
        // 进行聚合查询
        Aggregations aggregations = elasticsearchTemplate.query(searchQuery,response -> response.getAggregations());

        StringTerms modelTerms = (StringTerms) aggregations.asMap().get("tags"); //从聚合查询结果中取得名为"tags"的聚合
        // 升级到 Spring Boot 2.0.1 之后，使用新的方法
//		Iterator<Bucket> modelBucketIt = modelTerms.getBuckets().iterator();
//		while (modelBucketIt.hasNext()) {
//			Bucket actiontypeBucket = modelBucketIt.next();
//
//			list.add(new TagVO(actiontypeBucket.getKey().toString(), actiontypeBucket.getDocCount()));
//		}

        List<StringTerms.Bucket> modelBucketIt = modelTerms.getBuckets();   //Buckets(桶)满足特定条件的文档的集合
        for (StringTerms.Bucket actiontypeBucket : modelBucketIt) {
            //new TagVO(聚合字段的相应名称,相应聚合结果)
            list.add(new TagVO(actiontypeBucket.getKeyAsString(), actiontypeBucket.getDocCount()));
        }

        return list;
    }

    @Override
    public List<User> listTop12Users() {
        List<String> usernamelist = new ArrayList<>();// 存储排序后的用户账号

        // 查询条件
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(matchAllQuery())
                .withSearchType(SearchType.QUERY_THEN_FETCH).withIndices("blog").withTypes("blog")
                .addAggregation(terms("users").field("username")
                        .order(Terms.Order.count(false)).size(12)).build();
        // 聚合
        Aggregations aggregations = elasticsearchTemplate.query(searchQuery,response -> response.getAggregations());

        StringTerms modelTerms = (StringTerms) aggregations.asMap().get("users");

        // 升级到 Spring Boot 2.0.1 之后，使用新的方法
//		Iterator<Bucket> modelBucketIt = modelTerms.getBuckets().iterator();
//		while (modelBucketIt.hasNext()) {
//			Bucket actiontypeBucket = modelBucketIt.next();
//			String username = actiontypeBucket.getKey().toString();
//			usernamelist.add(username);
//		}

        List<StringTerms.Bucket> modelBucketIt = modelTerms.getBuckets();
        for (StringTerms.Bucket actiontypeBucket : modelBucketIt) {
            String username = actiontypeBucket.getKeyAsString();
            usernamelist.add(username);
        }

        // 根据用户名列表，查出他们的详细信息列表
        List<User> list = userService.listUsersByUsernames(usernamelist);

        // 按照 usernamelist 的顺序返回用户对象
        List<User> returnList = new ArrayList<>();

        for (String username : usernamelist) {
            for (User user : list) {
                if (username.equals(user.getUsername())) {
                    returnList.add(user);
                    break;
                }
            }
        }

        return returnList;
    }

    @Override
    public EsBlog getEsBlogByBlogId(Long id) {
        return esBlogRepository.findByBlogId(id);
    }

    @Transactional
    @Override
    public void removeByBlogId(Long id) {
        esBlogRepository.deleteByBlogId(id);
    }

    @Transactional
    @Override
    public EsBlog updateEsBlog(EsBlog esBlog) {
        return esBlogRepository.save(esBlog);
    }
}

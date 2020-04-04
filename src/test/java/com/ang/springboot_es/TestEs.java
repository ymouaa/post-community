package com.ang.springboot_es;


import com.alibaba.fastjson.JSONObject;
import com.ang.springboot_es.dao.DiscussPostMapper;
import com.ang.springboot_es.dao.DiscussPostRepository;
import com.ang.springboot_es.dao.UserMapper;
import com.ang.springboot_es.entity.DiscussPost;
import com.ang.springboot_es.entity.User;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = SpringbootEsApplication.class)
public class TestEs {


    @Autowired
    private DiscussPostMapper postMapper;

    @Autowired
    private DiscussPostRepository postRepository;


    @Autowired
    private UserMapper userMapper;

    @Test
    public void testMapper(){
        List<DiscussPost> list = postMapper.selectDiscussPost(149, 0, Integer.MAX_VALUE);
        for (DiscussPost post : list) {
            System.out.println(post);
        }

    }

    @Test
    public void testEs(){
        DiscussPost post = postMapper.selectDiscussPostById(120);
        postRepository.save(post);


    }


    @Test
    public void testSearch(){
        Optional<DiscussPost> byId = postRepository.findById(120);
        DiscussPost post = byId.get();
        System.out.println(post);
    }


    @Autowired
    private ElasticsearchTemplate template;
    @Test
    public void search(){
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("互联网","title","content"))
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0,10))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>")
                ).build();
        template.queryForPage(searchQuery, DiscussPost.class, new SearchResultMapper() {
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {
                T t = JSONObject.parseObject(response.toString(), clazz);

                return null;
            }
        });
    }



    /**
     * 构造搜索条件
     * 高亮，分页....
     */
    @Test
    public void test(){
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬","title","content"))
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0,10))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>")
                ).build();
        Page<DiscussPost> page = postRepository.search(searchQuery);
        for (DiscussPost post : page) {
            System.out.println(post);
        }
    }
    //排序
    // type status score
    //type 置顶
    //status 加精
    // 上面合算到score
    // create_time'

    // 分页  不然一次数据太多 page size
    // 高亮  那些字段    什么标签
    // new HighlightBuilder.Field("title","<em>","</em>")
    //.new HighlightBuilder.Field("content","<em>","</em>")
    // ,build();


}

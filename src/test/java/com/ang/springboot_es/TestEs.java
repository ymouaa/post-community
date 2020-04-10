package com.ang.springboot_es;


import com.ang.springboot_es.dao.DiscussPostMapper;
import com.ang.springboot_es.dao.repository.DiscussPostRepository;
import com.ang.springboot_es.dao.UserMapper;
import com.ang.springboot_es.entity.DiscussPost;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
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
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Date;
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
    public void testMapper() {
        List<DiscussPost> list = postMapper.selectDiscussPost(149, 0, Integer.MAX_VALUE);
        for (DiscussPost post : list) {
            System.out.println(post);
        }

    }

    @Test
    public void testEs() {
        DiscussPost post = postMapper.selectDiscussPostById(120);
        postRepository.save(post);


    }


    @Test
    public void testSearch() {
        Optional<DiscussPost> byId = postRepository.findById(120);
        DiscussPost post = byId.get();
        System.out.println(post);
    }




    @Test
    public void search() {
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("互联网", "title", "content"))
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0, 10))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>")
                ).build();
        template.queryForPage(searchQuery, DiscussPost.class, new SearchResultMapper() {
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {
                System.out.println(response.toString());
                System.out.println("==============");
                return null;
            }
        });
    }


    /**
     * 构造搜索条件
     * 高亮，分页....
     */
    @Test
    public void test() {
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0, 10))
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


    @Test
    public void testFindAll() {
        Iterable<DiscussPost> all = postRepository.findAll();
        for (DiscussPost post : all) {
            System.out.println(post);
        }
    }


    @Autowired
    private ElasticsearchTemplate template;

    @Test
    public void testHigh() {
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("互联网", "title", "content"))
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0, 10))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();
        Page<DiscussPost> page = template.queryForPage(searchQuery, DiscussPost.class, new SearchResultMapper() {


            @SuppressWarnings("Duplicates")
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {

                SearchHits hits = response.getHits();
                String s = response.toString();
                System.out.println(s);
                if (hits.getTotalHits() <= 0) {
                    return null;
                }

                List<DiscussPost> list = new ArrayList<>();

                for (SearchHit hit : hits) {
                    DiscussPost post = new DiscussPost();

                    // 原始的title 这里不会有高亮的标签，即使匹配
                    // 所以先获取，后面判断是否高亮 有就覆盖
                    String title = hit.getSourceAsMap().get("title").toString();
                    post.setTitle(title);

                    // 原始的content
                    String content = hit.getSourceAsMap().get("content").toString();
                    post.setContent(content);

                    String id = hit.getSourceAsMap().get("id").toString();
                    post.setId(Integer.valueOf(id));

                    String userId = hit.getSourceAsMap().get("userId").toString();
                    post.setUserId(Integer.valueOf(userId));

                    String time = hit.getSourceAsMap().get("createTime").toString();
                    post.setCreateTime(new Date(Long.valueOf(time)));

                    String score = hit.getSourceAsMap().get("score").toString();
                    post.setScore(Double.valueOf(score));

                    String type = hit.getSourceAsMap().get("type").toString();
                    post.setType(Integer.valueOf(type));

                    String status = hit.getSourceAsMap().get("status").toString();
                    post.setStatus(Integer.valueOf(status));

                    //含有高亮显示的结果
                    HighlightField titleField = hit.getHighlightFields().get("title");
                    if (titleField != null) {
                        post.setTitle(titleField.getFragments()[0].toString());
                    }

                    HighlightField contentField = hit.getHighlightFields().get("content");
                    if (contentField != null) {
                        post.setContent(contentField.getFragments()[0].toString());
                    }

                    list.add(post);
                }

                return new AggregatedPageImpl(list, pageable
                        , hits.getTotalHits(), response.getAggregations(),
                        response.getScrollId(), hits.getMaxScore());
            }
        });

        for (DiscussPost post : page) {
            System.out.println(post);
        }


    }


}

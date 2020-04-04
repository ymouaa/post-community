package com.ang.springboot_es.dao;

import com.ang.springboot_es.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost,Integer>{

}

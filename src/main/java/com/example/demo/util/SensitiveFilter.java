package com.example.demo.util;


import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);


    private static final String REPLACEMENT = "***";


    private TrieNode root = new TrieNode();

    @PostConstruct
    public void init() {
        try (
                InputStream in = this.getClass().getClassLoader().getResourceAsStream("sensitive.txt");
                BufferedReader br = new BufferedReader(new InputStreamReader(in))
        ) {
            String word;
            while ((word = br.readLine()) != null) {
                insert(word);
            }
        } catch (IOException e) {
            logger.error("加载敏感词配置文件失败" + e.getMessage());
        }
    }

    public void insert(String word) {
        TrieNode node = root;
        for (int i = 0; i < word.length(); i++) {
            TrieNode temp = node.get(word.charAt(i));
            if (temp == null) {
                temp = new TrieNode();
                node.add(word.charAt(i), temp);
            }
            node = temp;
        }
        node.setKeywrod(true);
    }

    private class TrieNode {
        private boolean isKeyword = false;
        private Map<Character, TrieNode> map = new HashMap<>();

        public boolean isKeyword() {
            return isKeyword;
        }

        public void setKeywrod(boolean keywrod) {
            isKeyword = keywrod;
        }

        public void add(Character c, TrieNode node) {
            map.put(c, node);
        }

        public TrieNode get(Character c) {
            return map.get(c);
        }
    }

    /**
     * 过滤敏感词
     *
     * @param text 待过滤的文本
     * @return 过滤后的文本
     */
    public String filter(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }

        // 指针1
        TrieNode tempNode = root;
        // 指针2
        int begin = 0;
        // 指针3
        int position = 0;
        // 结果
        StringBuilder sb = new StringBuilder();

        while (position < text.length()) {
            char c = text.charAt(position);

            // 跳过符号
            if (isSymbol(c)) {
                // 若指针1处于根节点,将此符号计入结果,让指针2向下走一步
                if (tempNode == root) {
                    sb.append(c);
                    begin++;
                }
                // 无论符号在开头或中间,指针3都向下走一步
                position++;
                continue;
            }

            // 检查下级节点
            tempNode = tempNode.get(c);
            if (tempNode == null) {
                // 以begin开头的字符串不是敏感词
                sb.append(text.charAt(begin));
                // 进入下一个位置
                position = ++begin;
                // 重新指向根节点
                tempNode = root;
            } else if (tempNode.isKeyword()) {
                // 发现敏感词,将begin~position字符串替换掉
                sb.append(REPLACEMENT);
                // 进入下一个位置
                begin = ++position;
                // 重新指向根节点
                tempNode = root;
            } else {
                // 检查下一个字符
                position++;
            }
        }

        // 将最后一批字符计入结果
        sb.append(text.substring(begin));

        return sb.toString();
    }

    // 判断是否为符号
    private boolean isSymbol(Character c) {
        // 0x2E80~0x9FFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

}
package com.ang.springboot_es.entity;

public class Page {

    //当前页码
    private int current = 1;

    // 数据库中的偏移
    private int offset;

    //每页显示的帖子数
    private int limit = 10;

    //查询路径 用于复用分页链接
    private String path;

    //数据总数
    private int rows;

    //最左
    private int from;

    //最右
    private int to;

    //总页数
    private int total;


    public void setRows(int rows) {
        this.rows = rows;
    }

    public void setCurrent(int current) {
        if (current < 1) {
            this.current=1;
            return;
        }
        if(current>total){
            this.current=total;
        }
        this.current = current;
    }

    public int getCurrent() {
        return current;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getLimit() {
        return this.limit;
    }

    // 0 1 2 |3 4 5| 6 7
    //
    //
    public int getOffset() {
        System.out.println("执行offset");
        System.out.println("current:"+current);
        offset = (current - 1) * limit;
        System.out.println("offset:"+offset);
        return offset;
    }

    public int getFrom() {
        from = current - 2;
        if (from > 1) {
            return from;
        }
        from = 1;
        return from;
    }


    public int getTo() {
        to = current + 2;
        //total
        if (to > getTotal()) {
            to = total;
            return to;
        }
        return to;
    }

    public int getTotal() {
        total = rows / limit;
        if (rows % limit == 0) {
            return total;
        }
        total += 1;
        return total;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

}

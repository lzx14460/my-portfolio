package com.example.system.common;

import lombok.Data;
import java.util.List;

/**
 * 分页结果
 */
/**
 * 统一分页结果封装
 */
@Data
public class PageResult<T> {
    private int pageNum;
    private int pageSize;
    private int total;
    private int totalPages;
    private List<T> list;

    public PageResult() {}

    public PageResult(int pageNum, int pageSize, int total, List<T> list) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.list = list;
        this.totalPages = (total + pageSize - 1) / pageSize;
    }
}
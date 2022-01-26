package com.zzj.vo.request;

public class PageVO {
    private int page;
    private int pageSize = 6;

    public int getPage() {
        return page;
    }

    public PageVO setPage(int page) {
        this.page = page;
        return this;
    }

    public int getPageSize() {
        return pageSize;
    }

    public PageVO setPageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }
}

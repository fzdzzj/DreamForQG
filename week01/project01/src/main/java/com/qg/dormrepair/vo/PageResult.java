// vo/PageResult.java
package com.qg.dormrepair.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 分页结果 VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    private List<T> list;        // 数据列表
    private Long total;          // 总记录数
    private Integer pageNum;     // 当前页码
    private Integer pageSize;    // 每页大小

    // 计算总页数
    public Integer getTotalPages() {
        return (int) Math.ceil((double) total / pageSize);
    }
}
package com.qg.dormrepair.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果 VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "通用分页响应结果")
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;
    @Schema(description = "当前页数据列表")
    private List<T> list;

    @Schema(description = "总记录数", example = "100")
    private Long total;

    @Schema(description = "当前页码", example = "1")
    private Integer pageNum;

    @Schema(description = "每页数量", example = "10")
    private Integer pageSize;

    // 计算总页数
    @Schema(description = "总页数", example = "10")
    public Integer getTotalPages() {
        if (total == null || pageSize == null || pageSize <= 0) {
            return 0;
        }
        return (int) Math.ceil((double) total / pageSize);
    }
}
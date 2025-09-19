package com.literature.assistant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.literature.assistant.entity.Literature;
import org.apache.ibatis.annotations.Param;

public interface LiteratureMapper extends BaseMapper<Literature> {
    
    /**
     * 分页查询文献列表
     */
    IPage<Literature> selectLiteraturePage(Page<Literature> page, 
                                          @Param("category") String category,
                                          @Param("description") String description,
                                          @Param("readingGuide") String readingGuide,
                                          @Param("tags") String tags);
}
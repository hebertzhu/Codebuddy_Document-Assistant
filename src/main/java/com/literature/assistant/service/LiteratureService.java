package com.literature.assistant.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.literature.assistant.entity.Literature;
import org.springframework.web.multipart.MultipartFile;

public interface LiteratureService extends IService<Literature> {
    
    /**
     * 分页查询文献列表
     */
    IPage<Literature> getLiteraturePage(int page, int size, String category, 
                                       String description, String readingGuide, String tags);
    
    /**
     * 上传文献文件
     */
    Literature uploadLiterature(MultipartFile file, String apiKey);
    
    /**
     * 批量导入文献
     */
    void batchImportLiterature(MultipartFile[] files, String apiKey);
    
    /**
     * 根据ID下载文献文件
     */
    byte[] downloadLiterature(Long id);
    
    /**
     * 根据ID获取文献详情
     */
    Literature getLiteratureDetail(Long id);
}
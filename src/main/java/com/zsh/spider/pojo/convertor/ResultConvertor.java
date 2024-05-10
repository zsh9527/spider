package com.zsh.spider.pojo.convertor;

import com.zsh.spider.pojo.dto.SearchEngineResultDTO;
import com.zsh.spider.pojo.dto.SpiderDTO;
import com.zsh.spider.pojo.po.EngineResultPO;
import com.zsh.spider.pojo.po.SpiderResultPO;
import com.zsh.spider.pojo.vo.SpiderResultVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * Result相关转换器
 */
@Mapper
public interface ResultConvertor {

    ResultConvertor INSTANCT = Mappers.getMapper(ResultConvertor.class);

    /**
     * SearchEngineResultDTO to EngineResultPO
     */
    @Mapping(target = "id", ignore = true)
    EngineResultPO toEngineResultPO(SearchEngineResultDTO dto, Long taskId);


    /**
     * SpiderResultPO to SpiderResultVO
     */
    @Mapping(target = "smallContent", ignore = true)
    @Mapping(target = "matchContent", ignore = true)
    @Mapping(target = "childUrl", ignore = true)
    SpiderResultVO toSpiderResultVO(SpiderResultPO po);

    /**
     * SpiderDTO to SpiderResultPO
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "content", ignore = true)
    @Mapping(target = "title", ignore = true)
    @Mapping(target = "similarScore", ignore = true)
    @Mapping(target = "childUrl", ignore = true)
    @Mapping(target = "matchContent", ignore = true)
    @Mapping(target = "createDate", ignore = true)
    SpiderResultPO toSpiderResultPO(SpiderDTO dto, Long taskId);

}

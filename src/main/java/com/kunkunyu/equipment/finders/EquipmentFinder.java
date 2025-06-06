package com.kunkunyu.equipment.finders;

import com.kunkunyu.equipment.Equipment;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListResult;
import com.kunkunyu.equipment.vo.EquipmentGroupVo;
import com.kunkunyu.equipment.vo.EquipmentVo;


/**
 * A finder for {@link Equipment}.
 *
 */
public interface EquipmentFinder {
    
    /**
     * List all equipment.
     *
     * @return a flux of equipment vo
     */
    Flux<EquipmentVo> listAll();
    
    /**
     * List equipment by page.
     *
     * @param page page number
     * @param size page size
     * @return a mono of list result
     */
    Mono<ListResult<EquipmentVo>> list(Integer page, Integer size);
    
    /**
     * List equipment by page and group.
     *
     * @param page  page number
     * @param size  page size
     * @param group group name
     * @return a mono of list result
     */
    Mono<ListResult<EquipmentVo>> list(Integer page, Integer size, String group);
    
    /**
     * List equipment by group.
     *
     * @param group group name
     * @return a flux of equipment vo
     */
    Flux<EquipmentVo> listBy(String group);
    
    /**
     * List all groups.
     *
     * @return a flux of equipment group vo
     */
    Flux<EquipmentGroupVo> groupBy();
}

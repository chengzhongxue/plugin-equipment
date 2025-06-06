package com.kunkunyu.equipment.service;

import reactor.core.publisher.Mono;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.router.IListRequest.QueryListRequest;
import com.kunkunyu.equipment.EquipmentGroup;

/**
 * A service for {@link EquipmentGroup}.
 *
 */
public interface EquipmentGroupService {
    
    /**
     * List equipment groups.
     *
     * @param request request
     * @return a mono of list result
     */
    Mono<ListResult<EquipmentGroup>> listEquipmentGroup(QueryListRequest request);
    
    /**
     * Create a equipment group.
     *
     * @param name name
     * @return a mono of equipment group
     */
    Mono<EquipmentGroup> deleteEquipmentGroup(String name);
    
}

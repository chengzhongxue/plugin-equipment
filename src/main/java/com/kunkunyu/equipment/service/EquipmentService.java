package com.kunkunyu.equipment.service;

import reactor.core.publisher.Mono;
import run.halo.app.extension.ListResult;
import com.kunkunyu.equipment.Equipment;
import com.kunkunyu.equipment.EquipmentQuery;

/**
 * A service for {@link Equipment}.
 *
 */
public interface EquipmentService {
    
    /**
     * List equipment.
     *
     * @param query query
     * @return a mono of list result
     */
    Mono<ListResult<Equipment>> listEquipment(EquipmentQuery query);
}

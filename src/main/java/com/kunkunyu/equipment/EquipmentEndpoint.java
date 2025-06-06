package com.kunkunyu.equipment;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.ListResult;
import com.kunkunyu.equipment.service.EquipmentService;

/**
 * A custom endpoint for {@link Equipment}.
 *
 */
@Component
@RequiredArgsConstructor
public class EquipmentEndpoint implements CustomEndpoint {

    private final EquipmentService equipmentService;

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        final var tag = "console.api.equipment.kunkunyu.com/v1alpha1/Equipment";
        return route()
            .GET("equipments", this::listEquipment,
                builder -> {
                    builder.operationId("ListEquipments")
                        .description("List equipment.")
                        .tag(tag)
                        .response(responseBuilder().implementation(
                            ListResult.generateGenericClass(Equipment.class)));

                    EquipmentQuery.buildParameters(builder);
                }
            )
            .build();
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion("console.api.equipment.kunkunyu.com/v1alpha1");
    }

    private Mono<ServerResponse> listEquipment(ServerRequest serverRequest) {
        EquipmentQuery query = new EquipmentQuery(serverRequest.exchange());
        return equipmentService.listEquipment(query)
            .flatMap(equipments -> ServerResponse.ok().bodyValue(equipments));
    }

}

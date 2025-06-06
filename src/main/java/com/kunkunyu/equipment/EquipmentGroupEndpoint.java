package com.kunkunyu.equipment;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.ListResult;
import com.kunkunyu.equipment.service.EquipmentGroupService;

/**
 * A custom endpoint for {@link Equipment}.
 */
@Component
@RequiredArgsConstructor
public class EquipmentGroupEndpoint implements CustomEndpoint {

    private final EquipmentGroupService equipmentGroupService;

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        return route()
            .GET("equipmentgroups", this::listEquipmentGroup,
                builder -> {
                    builder.operationId("ListEquipments")
                        .description("List equipment.")
                        .response(responseBuilder().implementation(
                            ListResult.generateGenericClass(EquipmentGroup.class))
                        );
                    EquipmentQuery.buildParameters(builder);
                }
            )
            .DELETE("equipmentgroups/{name}", this::deleteEquipmentGroup,
                builder -> builder.operationId("DeleteEquipmentGroup")
                    .description("Delete equipment group.")
                    .parameter(parameterBuilder()
                        .name("name")
                        .in(ParameterIn.PATH)
                        .description("Equipment group name")
                        .implementation(String.class)
                        .required(true)
                    )
                    .response(responseBuilder().implementation(EquipmentGroup.class))
            )
            .build();
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion("console.api.equipment.kunkunyu.com/v1alpha1");
    }

    private Mono<ServerResponse> deleteEquipmentGroup(ServerRequest serverRequest) {
        String name = serverRequest.pathVariable("name");
        if (StringUtils.isBlank(name)) {
            throw new ServerWebInputException("Equipment group name must not be blank.");
        }
        return equipmentGroupService.deleteEquipmentGroup(name)
            .flatMap(equipmentGroup -> ServerResponse.ok().bodyValue(equipmentGroup));
    }

    private Mono<ServerResponse> listEquipmentGroup(ServerRequest serverRequest) {
        var request = new EquipmentQuery(serverRequest.exchange());
        return equipmentGroupService.listEquipmentGroup(request)
            .flatMap(equipmentGroups -> ServerResponse.ok().bodyValue(equipmentGroups));
    }

}

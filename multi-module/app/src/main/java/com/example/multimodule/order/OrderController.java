package com.example.multimodule.order;

import com.example.multimodule.order.api.OrderApi;
import com.example.multimodule.order.request.OrderRequest;
import com.example.multimodule.order.response.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderApi orderApi;

    @PostMapping("/order-port")
    public OrderResponse orderPort(@RequestBody OrderRequest request){

        orderApi.orderWithPort(request.userId(), request.productName(), request.count());

        return OrderResponse.create("Port 방식 주문 완료");
    }


    @PostMapping("/order-event")
    public OrderResponse orderEvent(@RequestBody OrderRequest request){

        orderApi.orderWithEvent(request.userId(), request.productName(), request.count());

        return OrderResponse.create("이벤트 방식 주문 완료");
    }

}


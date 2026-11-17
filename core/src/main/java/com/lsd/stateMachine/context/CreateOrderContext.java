package com.lsd.stateMachine.context;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by lsd
 * 2021-05-10 16:55
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CreateOrderContext<T> {
    private T estimatePriceInfo; //促销信息
}

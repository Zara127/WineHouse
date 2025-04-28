package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单定时任务类
 */
@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理超时订单
     */
    @Scheduled(cron = "0 * * * * ?")//每分钟执行一次
    public void processTimeoutOrder(){
        log.info("定时处理超时订单:{}", LocalDateTime.now());
        //查询超时订单select * from orders where status = ？ and order_time < (查询时间-15分钟)?
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTime(Orders.PENDING_PAYMENT,time);

        if (ordersList != null && ordersList.size() > 0){//更新订单状态，取消超时订单
            for (Orders orders : ordersList){
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("订单超时，自动取消");
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);
            }
        }
    }


    /**
     * 处理处于派送状态的订单
     */
    @Scheduled(cron = "0 0 1 * * ?") // 每天凌晨1点执行一次
    public void processDeliveryOrder(){
        log.info("定时处理处于派送状态的订单:{}", LocalDateTime.now());
        //查询处于派送状态的订单，并且下单时间在60分钟之前
        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTime(Orders.DELIVERY_IN_PROGRESS,time);
        if (ordersList != null && ordersList.size() > 0){//更新订单状态，完成订单
            for (Orders orders : ordersList){
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            }
        }

    }


}

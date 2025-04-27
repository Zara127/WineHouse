package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.handler.GlobalExceptionHandler;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.sky.service.ShoppingCartService;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper  setmealMapper;

    /**
     *  添加购物车
     * @param shoppingCartDTO
     */
    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        //判断当前加购商品（菜品/套餐）是否已经存在 [动态sql查询shoppingCart表]
        //将shoppingCartDTO转换为ShoppingCart对象，作为参数传入mapper层的list方法
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());//当前登录用户id

        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);//查询结果

        //如果已经存在，则数量加1[update]
        if(list != null && list.size() > 0){
            ShoppingCart cart = list.get(0);//取出查询结果中的第一个元素
            cart.setNumber(cart.getNumber() + 1);
            shoppingCartMapper.updateNumberById(cart);
        }

        //如果不存在，则添加到购物车[insert]
        else{
            //[需要根据添加的商品是菜品还是套餐，补充cart对象的其他属性值]
            //判断添加的商品具体是菜品还是套餐[根据商品的菜品id、套餐id是否为空进行判断]
            Long dishId = shoppingCartDTO.getDishId();
            Long setmealId = shoppingCartDTO.getSetmealId();
            if(dishId != null){ //本次添加的为菜品
                Dish dish = dishMapper.getById(dishId);//根据菜品id查询菜品信息
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());

            }
            else{//本次添加的为套餐
                Setmeal setmeal = setmealMapper.getById(setmealId);
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());

            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);

        }

    }


    /**
     * 减少购物车商品
     * @param shoppingCartDTO
     */
    public void subShoppingCart(ShoppingCartDTO shoppingCartDTO){
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        //获取当前登录用户的id
        shoppingCart.setUserId(BaseContext.getCurrentId());
        //获取当前登录用户的购物车信息
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        //判断购物车是否为空
        if(list != null && list.size() > 0){//若不为空，则判断购物车中商品的数量
            ShoppingCart cart = list.get(0);
            if(cart.getNumber() == 1){//如果数量为1，则删除该商品
                shoppingCartMapper.deleteById(cart.getId());
            }
            else{//如果数量大于1，则数量减1
                cart.setNumber(cart.getNumber() - 1);
                shoppingCartMapper.updateNumberById(cart);
            }

        }
        else{//如果购物车为空，则抛出异常
            throw new RuntimeException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
    }
    @Override
    public List<ShoppingCart> showShoppingCart() {
        //获取当前登录用户id
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder().userId(userId).build();
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        return list;
    }


    /**
     * 清空购物车
     */
    public void cleaShoppingCart(){
        //获取当前登录用户id
        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.deleteByUserId(userId);
    }




}

package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

//自定义切面，实现公共字段自动填充处理逻辑
@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    //定义切入点，即对哪些类的哪些方法进行拦截
    //mapper下的所有类所有方法，同时需要满足添加了AutoFill注解
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut(){

     }

     //前置通知，在通知中为公共字段进行赋值
    @Before("autoFillPointCut()")
     public void autoFill(JoinPoint joinPoint){
         log.info("公共字段自动填充");
        //获取当前被拦截方法的数据库操作类型：INSERT、UPDATE；
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();//获取当前被拦截方法的签名对象
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);//通过反射获取当前被拦截方法的AutoFill注解对象
        OperationType operationType = autoFill.value();//通过对象调用value方法获取当前被拦截方法的数据库操作类型(枚举类型)

        // 获取当前被拦截方法的参数：操作实体对象；
        Object[] args = joinPoint.getArgs();//获取当前被拦截方法的所有参数
        if (args == null && args.length == 0) {
            return;
        }
        Object entity = args[0];//获取当前被拦截方法的第一个参数，即操作实体对象

        // 准备赋值的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        // 根据当前不同的数据库操作类型，为对应的实体对象的公共字段赋值
        if (operationType == OperationType.INSERT){//为四个公共字段赋值
            try {
                //通过反射获取当前被拦截方法的四个公共字段对应的set方法
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                // 通过反射为实体对象的四个公共字段赋值
                setCreateTime.invoke(entity,now);
                setUpdateTime.invoke(entity,now);
                setCreateUser.invoke(entity,currentId);
                setUpdateUser.invoke(entity,currentId);
            }catch (Exception e){
                e.printStackTrace();
            }


        }else if (operationType == OperationType.UPDATE){//为两个公共字段赋值
            try {
                //通过反射获取当前被拦截方法的2个公共字段对应的set方法
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                // 通过反射为实体对象的2个公共字段赋值
                setUpdateTime.invoke(entity,now);
                setUpdateUser.invoke(entity,currentId);
            }catch (Exception e){
                e.printStackTrace();
            }

        }



     }

}

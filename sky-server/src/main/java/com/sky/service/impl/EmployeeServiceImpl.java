package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对【数据库中密码存储为加密形式，需要将传入的请求参数password进行md5加密再进行比对】
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 新增员工
     * @param employeeDTO
     */
    public void save(EmployeeDTO employeeDTO) {
        //将DTO对象转为实体对象，传给mapper层
        Employee employee = new Employee();
        //对象属性拷贝
        BeanUtils.copyProperties(employeeDTO, employee);
        //设置DTO中未封装到的属性值
        //设置默认密码
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
        //设置账号状态
        employee.setStatus(StatusConstant.ENABLE);
//        [AOP实现公共字段自动填充]
//        //设置创建时间、更新时间
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//        // Add  设置当前记录创建人id和修改人id【通过利用ThreadLocal在校验JWT令牌处获取】
//        employee.setCreateUser(BaseContext.getCurrentId());
//        employee.setUpdateUser(BaseContext.getCurrentId());
        //调用mapper层执行sql语句实现数据插入
        employeeMapper.insert(employee);
    }

    /**
     * 员工分页查询
     * @param employeePageQueryDTO
     */
    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {//请求参数中包含页码page、每页记录数pageSize
        //开始分页查询[将请求参数中的页码、每页记录数传入]
        // PageHelper插件底层是基于MyBatis的拦截器实现的。首先利用ThreadLocal来存储、传递分页参数(page\pageSize)，
        // 然后在MyBatis拦截器中获取到分页参数，在后面执行的sql语句中进行动态拼接，将limit关键字拼接进去，实现分页查询。
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());
        Page<Employee> page = employeeMapper.pageQuery(employeePageQueryDTO);//page是一个ArrayList，里面封装了分页查询的结果
        long total = page.getTotal();//总记录数
        List<Employee> records = page.getResult();//查询结果
        return new PageResult(total, records);//将分页查询的结果封装到PageResult中
    }


    /**
     * 启用禁用员工
     * @param status
     * @param id
     * @return
     */
    public void startOrStop(Integer status, Long id) {
        //正常是根据id使用update更新status即可，但为了语句的通用性，即使用update方法可以更改更多属性，
        //将mapper层的update传入参数由status、id更改为整个employee实体类对象

        //创建实体类对象 方式1
//        Employee employee = new Employee();
//        employee.setStatus(status);
//        employee.setId(id);

        //创建实体类对象 方式2
        Employee employee = Employee.builder().status(status).id(id).build();

        employeeMapper.update(employee);
    }



    /**
     * 根据id查询回显员工信息
     * @param id
     * @return
     */
    public Employee getById(Long id){
        Employee employee = employeeMapper.getById(id);
        employee.setPassword("******");//进一步加强安全性
        return employee;
    }


    /**
     * 编辑员工信息
     * @param employeeDTO
     * @return
     */
    public void update(EmployeeDTO employeeDTO) {

        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);
//        [AOP实现公共字段自动填充]
//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setUpdateUser(BaseContext.getCurrentId());//ThreadLocal
        employeeMapper.update(employee);
    }
    }

package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 新增菜品和对应口味,事务一致性
     *
     * @param dishDTO
     */
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
//        向菜品表添加一条数据,吧distDTO中有的属性给到dish呀
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.insert(dish);
//        向口味表插入n条数据
        Long dishId = dish.getId();

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && flavors.size() > 0){
//            向口味表插入n条数据,批量插入
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });
        dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 菜品查询
     * @param dishPageQueryDTO
     * @return
     */
    @Transactional
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
//        这里为了适应展示到前端的数据，用vo比较好
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);

        //比较固定的
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 菜品的批量删除
     *
     * @param ids 业务规则，在serviceImp具体实现
     */
    @Override
    public void deleteBatch(List<Long> ids) {
//        菜品是否可以删除，是否启售中的？？
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if (dish.getStatus() == StatusConstant.ENABLE) {
//                当前菜品位于售卖中，不能删除
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

//        菜品是否可以删除，是否关联套餐？？
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if (setmealIds != null && setmealIds.size() > 0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

//        删除菜品表中菜品数据
//        for (Long id : ids) {
//            dishMapper.deleteById(id);
////            删除口味表
//            dishFlavorMapper.deleteById(id);

//            sql优化 批量删除 delete from dish where id in (?,?,?)
//            sql优化 delete from dish_flavor where dishid in (?,?,?)
//        }
        dishMapper.deleteByIds(ids);
        dishFlavorMapper.deleteByIds(ids);
    }

    /**
     * 根据id查询对应的菜品和口味数据
     *
     * @param id
     * @return
     */
    @Override
    public DishVO getByIdWithFlavor(Long id) {
//        根据id查出菜品,分开两次查询
        Dish dish = dishMapper.getById(id);
//        根据id查出口味
        List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(id);
        DishVO dishVO = new DishVO();
//      把查出来的数据封装到VO包
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(dishFlavors);
        return dishVO;
    }

    /**
     * 根据id修改对应的菜品和口味数据
     *
     * @param dishDTO
     * @return
     */
    @Override
    public void updateWithFlavor(DishDTO dishDTO) {
//    修改菜品信息
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);
//    先删除然后插入
        dishFlavorMapper.getByDishId(dishDTO.getId());
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
//            向口味表插入n条数据,批量插入
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishDTO.getId());
            });
            dishFlavorMapper.insertBatch(flavors);
        }
    }


}

package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {
    void saveWithFlavor(DishDTO dishDTO);

    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 菜品的批量删除
     * @param ids
     */
    void deleteBatch(List<Long> ids);


    /**
     *  根据id查询对应的菜品和口味数据
      * @param id
     * @return
     */
    DishVO getByIdWithFlavor(Long id);

    /**
     *  根据id修改对应的菜品和口味数据
     * @param dishDTO
     * @return
     */
    void updateWithFlavor(DishDTO dishDTO);
}

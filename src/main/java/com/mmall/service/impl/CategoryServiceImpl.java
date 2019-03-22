package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICategoryService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;

@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {
    private Logger         logger = LoggerFactory.getLogger(CategoryServiceImpl.class);
    @Autowired
    private CategoryMapper categoryMapper;

    public ServerResponse addCategory(String categoryName, Integer parentId) {
        if (parentId == null || StringUtils.isBlank(categoryName)) {
            return ServerResponse.createBySuccess("参数错误");
        }
        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true);
        int result = categoryMapper.insert(category);
        if (result > 0) {
            return ServerResponse.createBySuccess("品类添加成功");
        }
        return ServerResponse.createByErrorMessage("品类添加失败");
    }


    public ServerResponse setCategoryName(String categoryName, Integer categoryId) {
        if (categoryId == null || StringUtils.isBlank(categoryName)) {
            return ServerResponse.createBySuccess("参数错误");
        }
        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);
        int result = categoryMapper.updateByPrimaryKeySelective(category);
        if (result > 0) {
            return ServerResponse.createBySuccess("品类名称更新成功");
        }
        return ServerResponse.createByErrorMessage("品类名称更新失败");
    }

    public ServerResponse<List<Category>> getChildrenParallelCategory(Integer categoryId) {
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        if (CollectionUtils.isEmpty(categoryList)) {
            logger.info("未找到当前品类的子品类");
        }
        return ServerResponse.createBySuccess(categoryList);
    }

    public ServerResponse<List<Integer>> selectCategoryAndChildrenById(Integer categoryId) {
        Set<Category> categorySet = Sets.newHashSet();
        findeChildCategory(categorySet, categoryId);
        List<Integer> categoryList = Lists.newArrayList();
        if (categoryId != null) {
            for (Category c : categorySet) {
                categoryList.add(c.getId());
            }
        }
        return ServerResponse.createBySuccess(categoryList);
    }

    private Set<Category> findeChildCategory(Set<Category> categorySet,Integer categoryId) {
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if (category != null) {
            categorySet.add(category);
        }
        //查找子节点（递归算法必须要有一个中止的条件）
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        for (Category c : categoryList) {
            findeChildCategory(categorySet, c.getId());
        }
        return categorySet;
    }
}

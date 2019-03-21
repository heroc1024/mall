package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Category;
import com.mmall.pojo.User;
import com.mmall.service.ICategoryService;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/manage/category")
public class CateoryManageController {
    @Autowired
    private IUserService     iUserService;
    @Autowired
    private ICategoryService iCategoryService;

    @RequestMapping(value = "/add_category.do")
    @ResponseBody
    public ServerResponse addCategory(String categoryName, @RequestParam(value = "parentId", defaultValue = "0") Integer parentId, HttpSession session) {

        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }
        //核对管理员身份
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //增加分类
            return iCategoryService.addCategory(categoryName, parentId);
        } else {
            return ServerResponse.createByErrorMessage("没有权限");
        }
    }

    @RequestMapping("/set_category_name.do")
    @ResponseBody
    public ServerResponse setCategoryName(String categoryName, Integer categoryId, HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }
        //核对管理员身份
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //修改品类名称
            return iCategoryService.setCategoryName(categoryName, categoryId);
        } else {
            return ServerResponse.createByErrorMessage("没有权限");
        }
    }


    @RequestMapping(value = "/get_category.do",method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<List<Category>> getChildrenparallelCategory(@RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId, HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }
        //核对管理员身份
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //查询子节点category信息，保持平级，不递归
            return iCategoryService.getChildrenParallelCategory(categoryId);
        } else {
            return ServerResponse.createByErrorMessage("没有权限");
        }
    }

    @RequestMapping("/get_deep_category.do")
    @ResponseBody
    public ServerResponse<List<Category>> getCategoryAndDeepChildrenCategory(@RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId, HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }
        //核对管理员身份
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //查询当前节点及递归子节点
            return iCategoryService.selectCategoryAndChildrenById(categoryId);
        } else {
            return ServerResponse.createByErrorMessage("没有权限");
        }
    }

}

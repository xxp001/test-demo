package com.supervision.common.util;

/*
 * @Project:SupervisionSystem
 * @Description:response message and tips
 * @Author:TjSanshao
 * @Create:2019-04-05 15:09
 *
 **/
public class ResponseMessage {

    public static final String SYSTEM_ERROR = "系统内部错误！请联系管理员或者稍后重试！";

    public static final String REQUEST_ERROR = "非法请求！请考虑使用其他请求！";

    public static final String OPERATION_FAIL = "操作失败！请稍后重试！";

    public static final String LOGIN_FAIL = "未登录！不可操作！";

    public static final String PERMISSION_ERROR = "没有操作权限！";

    public static final String RECORD_REPEAT = "请求的操作已存在！";

    public static final String PROJECT_ID_CANNOT_BE_NULL = "请选择项目！";

    public static final String PROJECT_NAME_CANNOT_BE_NULL = "项目名称不能为空！";

    public static final String PROJECT_CODE_CANNOT_BE_NULL = "项目代码不能为空！";

    public static final String PROJECT_TYPE_CANNOT_BE_NULL = "项目类型不能为空！";

    public static final String PROJECT_DIRECTOR_CANNOT_BE_NULL = "必须指定项目总监！";

    public static final String PROJECT_DELEGATE_CANNOT_BE_NULL = "必须指定总监代表！";

    public static final String DEPARTMENT_CANNOT_BE_NULL = "必须指定执行部门！";

    public static final String PROJECT_MOMENT_ID_CANNOT_BE_NULL = "请选择项目阶段！";

    public static final String PROJECT_NOT_EXISTS = "项目不存在！请重新选择！";

    public static final String PROJECT_NOT_STARTED = "项目尚未启动！";

    public static final String PROJECT_IN_FINAL_CHECK = "项目已进入终验状态！";

    public static final String PROJECT_FINISHED = "项目已完成！";

    public static final String NO_MOMENTS_RUNNING = "该项目没有在进行的阶段！";

    public static final String USER_ID_CANNOT_BE_NULL = "请指定人选！";

    public static final String DAILY_ID_CANNOT_BE_NULL = "请选择日志！";

    public static final String DOCUMENT_ID_CANNOT_BE_NULL = "请选择文档！";

    public static final String UPLOAD_FILE_CANNOT_BE_NULL = "必须选择上传文件！";

    public static final String BOOK_ID_CANNOT_BE_NULL = "必须指定修改目标！";

    public static final String BOOK_NAME_CANNOT_BE_NULL = "必须指定名称！";

    public static final String BOOK_CLASS_CANNOT_BE_NULL = "必须指定分类！";

}

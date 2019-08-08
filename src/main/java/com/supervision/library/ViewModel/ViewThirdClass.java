package com.supervision.library.ViewModel;

import com.supervision.library.model.SecondClass;
import lombok.Data;

/*
 * @Project:SupervisionSystem
 * @Description:view model
 * @Author:TjSanshao
 * @Create:2019-05-11 14:08
 *
 **/
@Data
public class ViewThirdClass {
    private Integer id;

    private String className;

    private String classDesc;

    private String keywords;

    private Integer parentClassId;
    private SecondClass parentClass;
}

package com.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * user:lufei
 * DATE:2021/10/28
 **/

/**
 * 2级分类Vo
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Catelog2Vo {
    private String catelog1Id;     //1级分类父ID
    private List<Catelog3Vo> catelog3List;     //3级分类列表
    private String id;
    private String name;

    /**
     * 3级分类Vo
     */
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class Catelog3Vo {
        private String Catelog2Id;     //2级分类父ID
        private String id;
        private String name;
    }
}

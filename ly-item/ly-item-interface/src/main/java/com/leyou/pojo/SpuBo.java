package com.leyou.pojo;

import lombok.*;

import java.util.List;

@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SpuBo extends Spu {
    String cname;  //分类名称
    String bname; //品牌名称
    List<Sku> skus;  //sku列表
    SpuDetail spuDetail; //商品详情
}

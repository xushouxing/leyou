package com.leyou.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tb_spu_detail")
public class SpuDetail {
    @Id
    @GeneratedValue(strategy =GenerationType.IDENTITY)
    private Long spuId;        //对应的SPU的id
    private String description;//商品描述
    private String specialSpec;//商品特殊规格的名称及可选值模板
    private String genericSpec;//通用规格参数
    private String packingList;//包装清单
    private String afterService;//售后服务
}

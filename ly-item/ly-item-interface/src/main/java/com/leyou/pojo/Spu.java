package com.leyou.pojo;

import lombok.*;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
@Data
@AllArgsConstructor
@Setter
@Getter
@NoArgsConstructor
@Table(name = "tb_spu")
public class Spu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long brandId;
    private Long cid1;
    private Long cid2;
    private Long cid3;
    private String title;
    private String subTitle;
    private Boolean saleable;
    private Boolean valid;
    private Date createTime;
    private Date lastUpdateTime;
}

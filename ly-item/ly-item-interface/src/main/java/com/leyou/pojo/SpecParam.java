package com.leyou.pojo;

import lombok.*;

import javax.persistence.*;
@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tb_spec_param")
public class SpecParam {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long cid;
    private String name;
    private Long groupId;
    @Column(name = "`numeric`")
    private Boolean numeric;
    private String unit;
    private Boolean searching;
    private Boolean generic;
    private String segments;
}

package com.leyou.pojo;

import lombok.*;

import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.List;

@Table(name = "tb_spec_group")
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SpecGroup {
    private Long id;
    private Long cid;
    private String name;
    @Transient
    private List<SpecParam> params;
}
